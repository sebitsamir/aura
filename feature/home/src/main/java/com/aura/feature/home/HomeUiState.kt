package com.aura.feature.home

import com.aura.core.model.Song

data class HomeUiState(
    val greeting: String = "",
    val continueListening: Song? = null,
    val continueListeningProgress: Float = 0f,
    val continueListeningTime: String = "",
    val recentlyPlayed: List<Song> = emptyList(),
    val rediscover: List<Song> = emptyList(),
    val isLoading: Boolean = false
)
