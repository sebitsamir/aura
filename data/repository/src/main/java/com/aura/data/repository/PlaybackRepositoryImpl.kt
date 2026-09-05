package com.aura.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.concurrent.futures.await
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aura.core.common.util.Logger
import com.aura.core.model.QueueItem
import com.aura.core.model.Song
import com.aura.core.playback.PlaybackCommand
import com.aura.core.playback.PlaybackServiceContract
import com.aura.core.playback.PlaybackUiState
import com.aura.core.playback.toMediaItem
import com.aura.core.playback.toRepeatMode
import com.aura.domain.playback.PlaybackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackRepositoryImpl @Inject constructor(
   @ApplicationContext private val context: Context,
) : PlaybackRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectionMutex = Mutex()

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    override val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private var mediaController: MediaController? = null
    private var listener: Player.Listener? = null

    override suspend fun connect() {
        connectionMutex.withLock {
            if (mediaController != null) return

            val sessionToken = SessionToken(
                context,
                ComponentName(context.packageName, PlaybackServiceContract.SERVICE_CLASS_NAME),
            )

            runCatching {
                val controller = MediaController.Builder(context, sessionToken).buildAsync().await()
                mediaController = controller
                attachListener(controller)
                refreshState(controller)
                _playbackState.update { it.copy(isConnected = true, errorMessage = null) }
                Logger.i(TAG, "MediaController connected")
            }.onFailure { error ->
                Logger.e(TAG, "Failed to connect MediaController", error)
                _playbackState.update {
                    it.copy(isConnected = false, errorMessage = error.message ?: "Playback connection failed")
                }
            }
        }
    }

    override suspend fun disconnect() {
        connectionMutex.withLock {
            mediaController?.let { controller ->
                listener?.let(controller::removeListener)
                controller.release()
            }
            mediaController = null
            listener = null
            _playbackState.update { it.copy(isConnected = false) }
        }
    }

    override suspend fun refresh() {
        mediaController?.let(::refreshState)
    }

    override suspend fun send(command: PlaybackCommand) {
        if (mediaController == null) {
            connect()
        }
        val controller = mediaController ?: return

        when (command) {
            is PlaybackCommand.PlayQueue -> {
                val mediaItems = command.songs.map { it.toMediaItem() }
                if (mediaItems.isEmpty()) return
                val startIndex = command.startIndex.coerceIn(0, mediaItems.lastIndex)
                controller.setMediaItems(mediaItems, startIndex, 0L)
                controller.prepare()
                controller.play()
            }
            PlaybackCommand.Play -> controller.play()
            PlaybackCommand.Pause -> controller.pause()
            PlaybackCommand.TogglePlayPause -> {
                if (controller.isPlaying) controller.pause() else controller.play()
            }
            PlaybackCommand.Next -> controller.seekToNextMediaItem()
            PlaybackCommand.Previous -> controller.seekToPreviousMediaItem()
            is PlaybackCommand.SeekTo -> controller.seekTo(command.positionMs)
            PlaybackCommand.ToggleShuffle -> controller.shuffleModeEnabled = !controller.shuffleModeEnabled
            PlaybackCommand.CycleRepeatMode -> {
                controller.repeatMode = when (controller.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }
            is PlaybackCommand.RemoveFromQueue -> {
                val index = command.queueId.toInt()
                if (index in 0 until controller.mediaItemCount) {
                    controller.removeMediaItem(index)
                }
            }
            is PlaybackCommand.MoveQueueItem -> {
                if (command.fromIndex in 0 until controller.mediaItemCount &&
                    command.toIndex in 0 until controller.mediaItemCount
                ) {
                    controller.moveMediaItem(command.fromIndex, command.toIndex)
                }
            }
        }
        refreshState(controller)
    }

    private fun attachListener(controller: MediaController) {
        val playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = refreshState(controller)
            override fun onPlaybackStateChanged(playbackState: Int) = refreshState(controller)
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) =
                refreshState(controller)
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
            ) = refreshState(controller)
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = refreshState(controller)
            override fun onRepeatModeChanged(repeatMode: Int) = refreshState(controller)
        }
        listener = playerListener
        controller.addListener(playerListener)
        scope.launch { controller.currentTimeline }
    }

    private fun refreshState(controller: MediaController) {
        val currentMediaItem = controller.currentMediaItem
        val currentSong = currentMediaItem?.toSong(controller)

        val queueItems = buildList {
            for (index in 0 until controller.mediaItemCount) {
                val item = controller.getMediaItemAt(index)
                val song = item.toSong(controller) ?: continue
                add(QueueItem(song = song, queueId = index.toLong()))
            }
        }

        _playbackState.update {
            it.copy(
                currentSong = currentSong,
                queue = queueItems,
                currentIndex = controller.currentMediaItemIndex,
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition.coerceAtLeast(0L),
                durationMs = controller.duration.coerceAtLeast(0L),
                bufferedPositionMs = controller.bufferedPosition.coerceAtLeast(0L),
                repeatMode = controller.repeatMode.toRepeatMode(),
                shuffleEnabled = controller.shuffleModeEnabled,
                queueRevision = controller.mediaItemCount,
            )
        }
    }

    private fun androidx.media3.common.MediaItem.toSong(controller: Player): Song? {
        val songId = mediaId.toLongOrNull() ?: return null
        val uri = localConfiguration?.uri ?: return null
        return Song(
            id = songId,
            mediaStoreId = songId,
            title = mediaMetadata.title?.toString().orEmpty(),
            artist = mediaMetadata.artist?.toString().orEmpty(),
            album = mediaMetadata.albumTitle?.toString().orEmpty(),
            durationMs = mediaMetadata.durationMs ?: controller.duration.coerceAtLeast(0L),
            contentUri = uri,
        )
    }

    private companion object {
        const val TAG = "PlaybackRepository"
    }
}
