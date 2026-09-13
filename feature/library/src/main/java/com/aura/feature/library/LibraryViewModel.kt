package com.aura.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.permissions.AudioPermissionPolicy
import com.aura.domain.playback.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val audioPermissionPolicy: AudioPermissionPolicy
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(isLoading = true))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
    }

    fun onTabSelected(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasAudioPermission = granted) }
        if (granted) loadLibrary()
    }

    fun retry() {
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        val granted = audioPermissionPolicy.hasAudioPermission()
        _uiState.update { it.copy(hasAudioPermission = granted) }
        if (granted) loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                LibraryData(
                    songs = libraryRepository.getSongs(),
                    albums = libraryRepository.getAlbums(),
                    artists = libraryRepository.getArtists(),
                    genres = libraryRepository.getGenres(),
                    folders = libraryRepository.getFolders()
                )
            }.onSuccess { library ->
                _uiState.update {
                    it.copy(
                        songs = library.songs,
                        albums = library.albums,
                        artists = library.artists,
                        genres = library.genres,
                        folders = library.folders,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load your music library."
                    )
                }
            }
        }
    }

    private data class LibraryData(
        val songs: List<com.aura.core.model.Song>,
        val albums: List<com.aura.core.model.Album>,
        val artists: List<com.aura.core.model.Artist>,
        val genres: List<com.aura.core.model.Genre>,
        val folders: List<com.aura.core.model.Folder>
    )
}
