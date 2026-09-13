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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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
        if (ids.isEmpty()) return emptyList()
        return database.trackDao()
            .getAvailableByMediaStoreIds(ids)
            .map { track -> track.toSong() }
    }

    override suspend fun getAlbums(): List<Album> {
        ensureLibraryIsScanned()
        return database.albumDao().getAvailableAlbums().map { album ->
            val trackCount = database.albumDao().getTracksForAlbum(album.mediaStoreAlbumId).size
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
        return database.artistDao().getAvailableArtists().map { artist ->
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
        return database.genreDao().getAvailableGenres().map { genre ->
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
        return database.folderDao().getAvailableFolders().map { folder ->
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
        return database.albumDao().getTracksForAlbum(mediaStoreAlbumId).map { it.toSong() }
    }

    override suspend fun getSongsByArtist(artistUuid: String): List<Song> {
        ensureLibraryIsScanned()
        return database.artistDao().getTracksForArtist(artistUuid).map { it.toSong() }
    }

    override suspend fun getSongsByGenre(genreUuid: String): List<Song> {
        ensureLibraryIsScanned()
        return database.genreDao().getTracksForGenre(genreUuid).map { it.toSong() }
    }

    override suspend fun getSongsByFolder(folderPath: String): List<Song> {
        ensureLibraryIsScanned()
        return database.folderDao().getTracksForFolder(folderPath).map { it.toSong() }
    }

    override suspend fun getAlbumsByArtist(artistUuid: String): List<Album> {
        ensureLibraryIsScanned()
        return database.albumDao().getAlbumsForArtist(artistUuid).map { album ->
            val trackCount = database.albumDao().getTracksForAlbum(album.mediaStoreAlbumId).size
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

    override fun getRecentlyPlayed(limit: Int): Flow<List<Song>> {
        return database.playbackEventDao()
            .getRecentlyPlayedTracks(limit)
            .map { tracks -> tracks.map { it.toSong() } }
    }

    private suspend fun ensureLibraryIsScanned() {
        val hasTracks = database.trackDao().availableTrackCount() > 0
        if (hasTracks) return
        val latestRevision = database.scanRevisionDao().getLatest()
        val needsScan = latestRevision == null || latestRevision.status != ScanStatus.COMPLETED
        if (needsScan) scanner.fullScan()
    }

    private fun TrackEntity.toSong(): Song {
        return Song(
            id = mediaStoreId,
            mediaStoreId = mediaStoreId,
            auraUuid = auraUuid,
            title = title.ifBlank { "Unknown title" },
            artist = artist.ifBlank { "Unknown artist" },
            album = album.ifBlank { "Unknown album" },
            durationMs = durationMs,
            contentUri = Uri.parse(contentUri),
            albumId = albumId,
        )
    }
}
