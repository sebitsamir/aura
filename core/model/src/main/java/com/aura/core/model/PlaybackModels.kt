package com.aura.core.model

enum class RepeatMode {
    OFF,
    ONE,
    ALL,
}

data class QueueItem(
    val song: Song,
    val queueId: Long,
)

data class PlaybackSnapshot(
    val songIds: List<Long>,
    val currentIndex: Int,
    val positionMs: Long,
    val queueRevision: Int,
    val repeatMode: RepeatMode,
    val shuffleEnabled: Boolean,
    val wasPlaying: Boolean,
    val timestampMs: Long,
)
