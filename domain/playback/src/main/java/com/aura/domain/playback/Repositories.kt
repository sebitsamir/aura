package com.aura.domain.playback

import com.aura.core.model.Album
import com.aura.core.model.Artist
import com.aura.core.model.Genre
import com.aura.core.model.Song
import com.aura.core.playback.PlaybackCommand
import com.aura.core.playback.PlaybackUiState
import kotlinx.coroutines.flow.StateFlow

// Repository for library data.
// Provides songs, albums, artists, and genres from the Room database.
interface LibraryRepository {

    suspend fun getSongs(): List<Song>

    suspend fun getSongsByIds(ids: List<Long>): List<Song>

    suspend fun getAlbums(): List<Album>

    suspend fun getArtists(): List<Artist>

    suspend fun getGenres(): List<Genre>

    suspend fun getSongsByAlbum(mediaStoreAlbumId: Long): List<Song>

    suspend fun getSongsByArtist(artistUuid: String): List<Song>

    suspend fun getSongsByGenre(genreUuid: String): List<Song>

    suspend fun getAlbumsByArtist(artistUuid: String): List<Album>
}

// Repository for playback state and commands.
interface PlaybackRepository {

    val playbackState: StateFlow<PlaybackUiState>

    suspend fun connect()

    suspend fun disconnect()

    suspend fun refresh()

    suspend fun send(command: PlaybackCommand)
}