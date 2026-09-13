package com.aura.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.model.Song
import com.aura.core.playback.PlaybackUiState
import com.aura.domain.playback.LibraryRepository
import com.aura.domain.playback.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val recentlyPlayedFlow: Flow<List<Song>> = libraryRepository.getRecentlyPlayed(limit = 10)

    private val rediscoverFlow: Flow<List<Song>> = recentlyPlayedFlow.map { it.shuffled().take(5) }

    val uiState: StateFlow<HomeUiState> = combine(
        recentlyPlayedFlow,
        rediscoverFlow,
        playbackRepository.playbackState
    ) { recent, rediscover, playback ->
        val heroSong = playback.currentSong ?: recent.firstOrNull()
        HomeUiState(
            greeting = getGreeting(),
            continueListening = heroSong,
            continueListeningProgress = if (playback.durationMs > 0) playback.positionMs.toFloat() / playback.durationMs.toFloat() else 0f,
            continueListeningTime = if (heroSong != null) {
                "${formatTime(playback.positionMs)} / ${formatTime(playback.durationMs)}"
            } else "",
            recentlyPlayed = recent,
            rediscover = rediscover,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
