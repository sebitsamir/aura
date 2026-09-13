package com.aura.feature.library

import com.aura.core.model.Song
import com.aura.core.model.Album
import com.aura.core.model.Artist
import com.aura.core.model.Genre
import com.aura.core.model.Folder

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val hasAudioPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: LibraryTab = LibraryTab.Songs
)

enum class LibraryTab {
    Songs, Albums, Artists, Genres, Folders, Playlists
}
