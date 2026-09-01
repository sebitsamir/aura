package com.aura.core.playback

import com.aura.core.model.QueueItem
import com.aura.core.model.RepeatMode
import com.aura.core.model.Song

data class PlaybackUiState(
    val currentSong: Song? = null,
    val queue: List<QueueItem> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val queueRevision: Int = 0,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PlaybackCommand {
    data class PlayQueue(val songs: List<Song>, val startIndex: Int = 0) : PlaybackCommand
    data object Play : PlaybackCommand
    data object Pause : PlaybackCommand
    data object TogglePlayPause : PlaybackCommand
    data object Next : PlaybackCommand
    data object Previous : PlaybackCommand
    data class SeekTo(val positionMs: Long) : PlaybackCommand
    data object ToggleShuffle : PlaybackCommand
    data object CycleRepeatMode : PlaybackCommand
    data class RemoveFromQueue(val queueId: Long) : PlaybackCommand
    data class MoveQueueItem(val fromIndex: Int, val toIndex: Int) : PlaybackCommand
}
