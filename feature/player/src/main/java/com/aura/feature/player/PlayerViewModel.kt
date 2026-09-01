package com.aura.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.common.util.Logger
import com.aura.core.permissions.AudioPermissionPolicy
import com.aura.core.playback.PlaybackCommand
import com.aura.domain.playback.GetLocalSongsUseCase
import com.aura.domain.playback.ObservePlaybackStateUseCase
import com.aura.domain.playback.PlaySongsUseCase
import com.aura.domain.playback.PlaybackRepository
import com.aura.domain.playback.SendPlaybackCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getLocalSongsUseCase: GetLocalSongsUseCase,
    private val playSongsUseCase: PlaySongsUseCase,
    private val sendPlaybackCommandUseCase: SendPlaybackCommandUseCase,
    private val playbackRepository: PlaybackRepository,
    observePlaybackStateUseCase: ObservePlaybackStateUseCase,
    private val audioPermissionPolicy: AudioPermissionPolicy,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerScreenUiState())
    val uiState: StateFlow<PlayerScreenUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    init {
        refreshPermissionState()
        viewModelScope.launch {
            playbackRepository.connect()
        }
        viewModelScope.launch {
            observePlaybackStateUseCase().collect { playback ->
                _uiState.update { it.copy(playback = playback) }
                if (playback.isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }
        }
    }

    fun onEvent(event: PlayerScreenEvent) {
        when (event) {
            PlayerScreenEvent.RequestPermission -> refreshPermissionState()
            is PlayerScreenEvent.PlaySong -> playSongAt(event.index)
            PlayerScreenEvent.TogglePlayPause -> sendCommand(PlaybackCommand.TogglePlayPause)
            PlayerScreenEvent.Next -> sendCommand(PlaybackCommand.Next)
            PlayerScreenEvent.Previous -> sendCommand(PlaybackCommand.Previous)
            PlayerScreenEvent.ToggleShuffle -> sendCommand(PlaybackCommand.ToggleShuffle)
            PlayerScreenEvent.CycleRepeatMode -> sendCommand(PlaybackCommand.CycleRepeatMode)
            is PlayerScreenEvent.SeekTo -> sendCommand(PlaybackCommand.SeekTo(event.positionMs))
            PlayerScreenEvent.RefreshLibrary -> loadLibrary()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasAudioPermission = granted) }
        if (granted) {
            loadLibrary()
        }
    }

    private fun refreshPermissionState() {
        val granted = audioPermissionPolicy.hasAudioPermission()
        _uiState.update { it.copy(hasAudioPermission = granted) }
        if (granted) {
            loadLibrary()
        }
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLibrary = true, libraryError = null) }
            runCatching {
                getLocalSongsUseCase()
            }.onSuccess { songs ->
                _uiState.update {
                    it.copy(
                        songs = songs,
                        isLoadingLibrary = false,
                        libraryError = if (songs.isEmpty()) "No local music found on this device." else null,
                    )
                }
            }.onFailure { error ->
                Logger.e(TAG, "Failed to load library", error)
                _uiState.update {
                    it.copy(
                        isLoadingLibrary = false,
                        libraryError = error.message ?: "Unable to read local music.",
                    )
                }
            }
        }
    }

    private fun playSongAt(index: Int) {
        val songs = _uiState.value.songs
        if (index !in songs.indices) return
        viewModelScope.launch {
            playSongsUseCase(songs, index)
        }
    }

    private fun sendCommand(command: PlaybackCommand) {
        viewModelScope.launch {
            sendPlaybackCommandUseCase(command)
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (isActive) {
                playbackRepository.refresh()
                delay(PROGRESS_TICK_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private companion object {
        const val TAG = "PlayerViewModel"
        const val PROGRESS_TICK_MS = 250L
    }
}
