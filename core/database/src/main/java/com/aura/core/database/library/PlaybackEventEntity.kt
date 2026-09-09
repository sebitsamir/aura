package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playback_events",
    indices = [
        Index(value = ["trackUuid"]),
        Index(value = ["eventType"]),
        Index(value = ["timestamp"])
    ]
)
data class PlaybackEventEntity(
    @PrimaryKey val eventId: String,
    val trackUuid: String,
    val mediaStoreId: Long,
    val eventType: Int,
    val timestamp: Long,
    val positionMs: Long,
    val durationMs: Long,
    val sessionId: String,
    val sourceSurface: String? = null
)

object PlaybackEventType {
    const val PLAY_REQUESTED = 0
    const val PLAY_STARTED = 1
    const val QUALIFIED_PLAY = 2
    const val PAUSED = 3
    const val RESUMED = 4
    const val SEEKED = 5
    const val SKIPPED = 6
    const val COMPLETED = 7
    const val STOPPED = 8
    const val FAILED = 9
}