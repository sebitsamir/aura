package com.aura.service.playback

import com.aura.core.database.library.PlaybackEventDao
import com.aura.core.database.library.PlaybackEventEntity
import com.aura.core.database.library.PlaybackEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackEventRecorder @Inject constructor(
    private val playbackEventDao: PlaybackEventDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentSessionId: String = UUID.randomUUID().toString()
    
    private val QUALIFIED_PLAY_THRESHOLD_MS = 30_000L
    private val QUALIFIED_PLAY_PERCENTAGE = 0.5f

    private var currentTrackId: String? = null
    private var currentMediaStoreId: Long = 0L
    private var currentDuration: Long = 0L
    private var hasRecordedQualifiedPlay: Boolean = false
    private var hasRecordedCompletion: Boolean = false

    fun onPlayerEvent(event: PlayerEvent) {
        when (event) {
            is PlayerEvent.TrackChanged -> handleTrackChanged(event)
            is PlayerEvent.PlaybackStateChanged -> handlePlaybackStateChanged(event)
            is PlayerEvent.PositionDiscontinuity -> handleSeek(event)
        }
    }

    private fun handleTrackChanged(event: PlayerEvent.TrackChanged) {
        if (currentTrackId != null && !hasRecordedCompletion) {
            recordEvent(PlaybackEventType.SKIPPED, event.positionMs)
        }
        
        currentTrackId = event.trackUuid
        currentMediaStoreId = event.mediaStoreId
        currentDuration = event.durationMs
        hasRecordedQualifiedPlay = false
        hasRecordedCompletion = false
        
        if (event.isPlaying) {
            recordEvent(PlaybackEventType.PLAY_STARTED, event.positionMs)
        }
    }

    private fun handlePlaybackStateChanged(event: PlayerEvent.PlaybackStateChanged) {
        if (currentTrackId == null) return
        
        if (event.isPlaying) {
            if (!hasRecordedQualifiedPlay) {
                val qualifiedThreshold = minOf(
                    QUALIFIED_PLAY_THRESHOLD_MS,
                    (currentDuration * QUALIFIED_PLAY_PERCENTAGE).toLong()
                )
                if (event.positionMs >= qualifiedThreshold) {
                    recordEvent(PlaybackEventType.QUALIFIED_PLAY, event.positionMs)
                    hasRecordedQualifiedPlay = true
                }
            }
        } else {
            recordEvent(PlaybackEventType.PAUSED, event.positionMs)
        }
        
        if (currentDuration > 0 && event.positionMs >= currentDuration - 2000 && !hasRecordedCompletion) {
            recordEvent(PlaybackEventType.COMPLETED, event.positionMs)
            hasRecordedCompletion = true
        }
    }

    private fun handleSeek(event: PlayerEvent.PositionDiscontinuity) {
        if (currentTrackId != null) {
            recordEvent(PlaybackEventType.SEEKED, event.positionMs)
        }
    }

    private fun recordEvent(eventType: Int, positionMs: Long) {
        val trackId = currentTrackId ?: return
        scope.launch {
            playbackEventDao.insertEvent(
                PlaybackEventEntity(
                    eventId = UUID.randomUUID().toString(),
                    trackUuid = trackId,
                    mediaStoreId = currentMediaStoreId,
                    eventType = eventType,
                    timestamp = System.currentTimeMillis(),
                    positionMs = positionMs,
                    durationMs = currentDuration,
                    sessionId = currentSessionId
                )
            )
        }
    }
}

sealed class PlayerEvent {
    data class TrackChanged(
        val trackUuid: String,
        val mediaStoreId: Long,
        val durationMs: Long,
        val positionMs: Long,
        val isPlaying: Boolean
    ) : PlayerEvent()
    
    data class PlaybackStateChanged(
        val isPlaying: Boolean,
        val positionMs: Long
    ) : PlayerEvent()
    
    data class PositionDiscontinuity(
        val positionMs: Long
    ) : PlayerEvent()
}