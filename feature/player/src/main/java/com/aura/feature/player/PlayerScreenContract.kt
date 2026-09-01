package com.aura.feature.player

import com.aura.core.model.RepeatMode
import com.aura.core.model.Song
import com.aura.core.playback.PlaybackCommand
import com.aura.core.playback.PlaybackUiState

data class PlayerScreenUiState(
    val songs: List<Song> = emptyList(),
    val playback: PlaybackUiState = PlaybackUiState(),
    val isLoadingLibrary: Boolean = false,
    val hasAudioPermission: Boolean = false,
    val libraryError: String? = null,
)

sealed interface PlayerScreenEvent {
    data object RequestPermission : PlayerScreenEvent
    data class PlaySong(val index: Int) : PlayerScreenEvent
    data object TogglePlayPause : PlayerScreenEvent
    data object Next : PlayerScreenEvent
    data object Previous : PlayerScreenEvent
    data object ToggleShuffle : PlayerScreenEvent
    data object CycleRepeatMode : PlayerScreenEvent
    data class SeekTo(val positionMs: Long) : PlayerScreenEvent
    data object RefreshLibrary : PlayerScreenEvent
}

fun RepeatMode.label(): String = when (this) {
    RepeatMode.OFF -> "Repeat off"
    RepeatMode.ONE -> "Repeat one"
    RepeatMode.ALL -> "Repeat all"
}
