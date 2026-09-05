package com.aura.service.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.aura.core.common.util.Logger
import com.aura.core.datastore.PlaybackRecoveryStore
import com.aura.core.media.LocalMusicDataSource
import com.aura.core.model.PlaybackSnapshot
import com.aura.core.playback.toMediaItem
import com.aura.core.playback.toPlayerRepeatMode
import com.aura.core.playback.toRepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localMusicDataSource: LocalMusicDataSource,
    private val recoveryStore: PlaybackRecoveryStore,
) {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    fun createMediaSession(service: PlaybackService): MediaSession {
        val exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .also { player = it }

        exoPlayer.addListener(
            object : Player.Listener {

                override fun onIsPlayingChanged(
                    isPlaying: Boolean,
                ) = persistSnapshot(isPlaying)

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) = persistSnapshot(exoPlayer.isPlaying)

                override fun onPlaybackStateChanged(
                    playbackState: Int,
                ) {
                    if (playbackState == Player.STATE_ENDED) {
                        persistSnapshot(false)
                    }
                }

                override fun onShuffleModeEnabledChanged(
                    shuffleModeEnabled: Boolean,
                ) = persistSnapshot(exoPlayer.isPlaying)

                override fun onRepeatModeChanged(
                    repeatMode: Int,
                ) = persistSnapshot(exoPlayer.isPlaying)
            },
        )

        val session =
            MediaSession.Builder(service, exoPlayer)
                .build()
                .also { mediaSession = it }

        scope.launch {
            restorePlayback(exoPlayer)
        }

        Logger.i(TAG, "PlayerManager initialized")

        return session
    }

    fun getPlayer(): ExoPlayer? = player

    fun release() {
        persistSnapshot(isPlaying = false)

        mediaSession?.release()
        mediaSession = null

        player?.release()
        player = null
    }

    private fun persistSnapshot(
        isPlaying: Boolean,
    ) {
        val player = player ?: return

        if (player.mediaItemCount == 0) {
            scope.launch {
                recoveryStore.clear()
            }
            return
        }

        val songIds = buildList {
            for (index in 0 until player.mediaItemCount) {
                val id =
                    player.getMediaItemAt(index)
                        .mediaId
                        .toLongOrNull()
                        ?: continue

                add(id)
            }
        }

        val snapshot = PlaybackSnapshot(
            songIds = songIds,
            currentIndex =
                player.currentMediaItemIndex.coerceAtLeast(0),
            positionMs =
                player.currentPosition.coerceAtLeast(0L),
            queueRevision = player.mediaItemCount,
            repeatMode =
                player.repeatMode.toRepeatMode(),
            shuffleEnabled =
                player.shuffleModeEnabled,
            wasPlaying = isPlaying,
            timestampMs =
                System.currentTimeMillis(),
        )

        scope.launch {
            recoveryStore.save(snapshot)
        }
    }

    private suspend fun restorePlayback(
        player: ExoPlayer,
    ) {
        val snapshot =
            recoveryStore.load() ?: return

        val songs =
            localMusicDataSource.findSongsByIds(
                snapshot.songIds,
            )

        if (songs.isEmpty()) {
            recoveryStore.clear()
            return
        }

        val songsById =
            songs.associateBy { it.id }

        val orderedSongs =
            snapshot.songIds.mapNotNull {
                songsById[it]
            }

        if (orderedSongs.isEmpty()) {
            recoveryStore.clear()
            return
        }

        player.setMediaItems(
            orderedSongs.map {
                it.toMediaItem()
            },
        )

        player.repeatMode =
            snapshot.repeatMode.toPlayerRepeatMode()

        player.shuffleModeEnabled =
            snapshot.shuffleEnabled

        val safeIndex =
            snapshot.currentIndex.coerceIn(
                0,
                orderedSongs.lastIndex,
            )

        player.seekTo(
            safeIndex,
            snapshot.positionMs.coerceAtLeast(0L),
        )

        player.prepare()

        if (snapshot.wasPlaying) {
            player.play()
        }

        Logger.i(
            TAG,
            "Restored playback queue with ${orderedSongs.size} songs",
        )
    }

    private companion object {
        const val TAG = "PlayerManager"
    }
}