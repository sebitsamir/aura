package com.aura.data.repository

import android.net.Uri
import com.aura.core.database.AuraDatabase
import com.aura.core.database.library.ScanStatus
import com.aura.core.database.library.TrackEntity
import com.aura.core.model.Album
import com.aura.core.model.Artist
import com.aura.core.model.Folder
import com.aura.core.model.Genre
import com.aura.core.model.Song
import com.aura.core.scanner.MediaStoreTrackScanner
import com.aura.domain.playback.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

// Library repository implementation.
// Reads from Room after a scan.
// Playback recovery remains protected by the MediaStore fallback
// until the Room library is populated.
@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val database: AuraDatabase,
    private val scanner: MediaStoreTrackScanner,
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

        return emptyList()
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

    override suspend fun getFolders(): List<Folder> {
        ensureLibraryIsScanned()
        val folders = database.folderDao().getAvailableFolders()
        return folders.map { folder ->
            val trackCount = database.folderDao().getTrackCountForFolder(folder.path)
            Folder(
                folderUuid = folder.folderUuid,
                path = folder.path,
                name = folder.name,
                parentPath = folder.parentPath,
                trackCount = trackCount,
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

    override suspend fun getSongsByFolder(folderPath: String): List<Song> {
        ensureLibraryIsScanned()
        return database.folderDao()
            .getTracksForFolder(folderPath)
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

    // Ensures that the first library access performs a scan when needed.
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

    // Maps the durable Room track source into the playback-facing Song model.
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