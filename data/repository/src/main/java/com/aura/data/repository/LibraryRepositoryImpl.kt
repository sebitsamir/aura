package com.aura.data.repository

import android.net.Uri
import com.aura.core.database.AuraDatabase
import com.aura.core.database.library.ScanStatus
import com.aura.core.database.library.TrackEntity
import com.aura.core.media.LocalMusicDataSource
import com.aura.core.model.Album
import com.aura.core.model.Artist
import com.aura.core.model.Genre
import com.aura.core.model.Song
import com.aura.core.scanner.MediaStoreTrackScanner
import com.aura.domain.playback.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

// Library repository implementation.
// Reads from Room after a scan, with MediaStore fallback for recovery.
@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val database: AuraDatabase,
    private val scanner: MediaStoreTrackScanner,
    private val localMusicDataSource: LocalMusicDataSource,
) : LibraryRepository {

    override suspend fun getSongs(): List<Song> {
        ensureLibraryIsScanned()
        return database.trackDao()
            .getAvailableSongs()
            .map { track -> track.toSong() }
    }

    override suspend fun getSongsByIds(ids: List<Long>): List<Song> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        val roomSongs = database.trackDao()
            .getAvailableByMediaStoreIds(ids)
            .map { track -> track.toSong() }

        if (roomSongs.isNotEmpty()) {
            return roomSongs
        }

        return localMusicDataSource.findSongsByIds(ids)
    }

    override suspend fun getAlbums(): List<Album> {
        ensureLibraryIsScanned()
        val albums = database.albumDao().getAvailableAlbums()
        return albums.map { album ->
            val trackCount = database.albumDao()
                .getTracksForAlbum(album.mediaStoreAlbumId)
                .size
            Album(
                albumUuid = album.albumUuid,
                name = album.name,
                mediaStoreAlbumId = album.mediaStoreAlbumId,
                artistName = "",
                year = album.year,
                trackCount = trackCount,
            )
        }
    }

    override suspend fun getArtists(): List<Artist> {
        ensureLibraryIsScanned()
        val artists = database.artistDao().getAvailableArtists()
        return artists.map { artist ->
            val tracks = database.artistDao().getTracksForArtist(artist.artistUuid)
            val albums = database.albumDao().getAlbumsForArtist(artist.artistUuid)
            Artist(
                artistUuid = artist.artistUuid,
                name = artist.name,
                albumCount = albums.size,
                trackCount = tracks.size,
            )
        }
    }

    override suspend fun getGenres(): List<Genre> {
        ensureLibraryIsScanned()
        val genres = database.genreDao().getAvailableGenres()
        return genres.map { genre ->
            val tracks = database.genreDao().getTracksForGenre(genre.genreUuid)
            Genre(
                genreUuid = genre.genreUuid,
                name = genre.name,
                trackCount = tracks.size,
            )
        }
    }

    override suspend fun getSongsByAlbum(mediaStoreAlbumId: Long): List<Song> {
        ensureLibraryIsScanned()
        return database.albumDao()
            .getTracksForAlbum(mediaStoreAlbumId)
            .map { track -> track.toSong() }
    }

    override suspend fun getSongsByArtist(artistUuid: String): List<Song> {
        ensureLibraryIsScanned()
        return database.artistDao()
            .getTracksForArtist(artistUuid)
            .map { track -> track.toSong() }
    }

    override suspend fun getSongsByGenre(genreUuid: String): List<Song> {
        ensureLibraryIsScanned()
        return database.genreDao()
            .getTracksForGenre(genreUuid)
            .map { track -> track.toSong() }
    }

    override suspend fun getAlbumsByArtist(artistUuid: String): List<Album> {
        ensureLibraryIsScanned()
        val albums = database.albumDao().getAlbumsForArtist(artistUuid)
        return albums.map { album ->
            val trackCount = database.albumDao()
                .getTracksForAlbum(album.mediaStoreAlbumId)
                .size
            Album(
                albumUuid = album.albumUuid,
                name = album.name,
                mediaStoreAlbumId = album.mediaStoreAlbumId,
                artistName = "",
                year = album.year,
                trackCount = trackCount,
            )
        }
    }

    // Ensures the library is scanned before reading.
    private suspend fun ensureLibraryIsScanned() {
        val hasTracks = database.trackDao().availableTrackCount() > 0
        if (hasTracks) {
            return
        }

        val latestRevision = database.scanRevisionDao().getLatest()
        val needsScan = latestRevision == null || latestRevision.status != ScanStatus.COMPLETED

        if (needsScan) {
            scanner.fullScan()
        }
    }

    // Maps a Room TrackEntity to the playback-facing Song model.
    private fun TrackEntity.toSong(): Song {
        return Song(
            id = mediaStoreId,
            mediaStoreId = mediaStoreId,
            title = title.ifBlank { "Unknown title" },
            artist = artist.ifBlank { "Unknown artist" },
            album = album.ifBlank { "Unknown album" },
            durationMs = durationMs,
            contentUri = Uri.parse(contentUri),
            albumId = albumId,
        )
    }
}