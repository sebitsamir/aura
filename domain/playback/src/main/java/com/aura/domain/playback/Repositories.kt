package com.aura.domain.playback

import com.aura.core.model.Song
import com.aura.core.playback.PlaybackCommand
import com.aura.core.playback.PlaybackUiState
import kotlinx.coroutines.flow.StateFlow

interface LibraryRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getSongsByIds(ids: List<Long>): List<Song>
}

interface PlaybackRepository {
    val playbackState: StateFlow<PlaybackUiState>
    suspend fun connect()
    suspend fun disconnect()
    suspend fun refresh()
    suspend fun send(command: PlaybackCommand)
}
