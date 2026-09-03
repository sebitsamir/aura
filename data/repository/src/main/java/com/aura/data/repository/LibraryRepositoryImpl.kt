package com.aura.data.repository

import android.net.Uri
import com.aura.core.database.AuraDatabase
import com.aura.core.database.library.ScanStatus
import com.aura.core.database.library.TrackEntity
import com.aura.core.media.LocalMusicDataSource
import com.aura.core.model.Song
import com.aura.core.scanner.MediaStoreTrackScanner
import com.aura.domain.playback.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

// Library repository bridge.
// Songs are read from Room after a scan.
// Playback recovery remains protected by the MediaStore fallback
// until the Room library is populated.
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

        // Phase 1 recovery path remains available while Room is still empty.
        return localMusicDataSource.findSongsByIds(ids)
    }

    // Ensures that the first library access performs a scan when needed.
    // Later Phase 2 slices will replace this with explicit scan state and UI.
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

    // Maps the durable Room track source into the existing playback-facing Song model.
    // Song.id intentionally remains the MediaStore ID during Phase 2.1
    // so Phase 1 playback recovery is not disturbed.
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