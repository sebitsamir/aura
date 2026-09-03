package com.aura.core.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.aura.core.database.AuraDatabase
import com.aura.core.database.library.AlbumArtistEntity
import com.aura.core.database.library.AlbumEntity
import com.aura.core.database.library.ArtistEntity
import com.aura.core.database.library.GenreEntity
import com.aura.core.database.library.ScanRevisionEntity
import com.aura.core.database.library.ScanStatus
import com.aura.core.database.library.TrackArtistEntity
import com.aura.core.database.library.TrackAvailability
import com.aura.core.database.library.TrackEntity
import com.aura.core.database.library.TrackGenreEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

// Scans MediaStore and populates the Room database with tracks,
// artists, albums, genres, and their relationships.
@Singleton
class MediaStoreTrackScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AuraDatabase,
) {

    private companion object {
        const val TAG = "MediaStoreTrackScanner"
        const val BATCH_SIZE = 200
        const val FALLBACK_EXTERNAL_VOLUME = MediaStore.VOLUME_EXTERNAL
    }

    // Performs a full scan of the external media volume.
    // Scans tracks, then derives artists, albums, and genres.
    suspend fun fullScan(): ScanResult = withContext(Dispatchers.IO) {
        val revisionId = database.scanRevisionDao().insert(
            ScanRevisionEntity(
                startedAt = System.currentTimeMillis(),
                finishedAt = null,
                status = ScanStatus.RUNNING,
                scannedCount = 0,
                addedCount = 0,
                updatedCount = 0,
                unavailableCount = 0,
            )
        )

        val counts = ScanCounts()

        try {
            val volumes = accessibleVolumes()

            for (volume in volumes) {
                coroutineContext.ensureActive()
                try {
                    scanVolume(volume, revisionId, counts)
                } catch (securityException: SecurityException) {
                    throw securityException
                } catch (exception: Exception) {
                    Timber.tag(TAG).e(exception, "Failed to scan volume")
                }
            }

            // Derive library relationships from scanned tracks.
            coroutineContext.ensureActive()
            deriveLibraryRelationships(revisionId)

            // Mark unseen records as unavailable.
            val unavailableCount = markUnseenUnavailable(revisionId)
            counts.unavailable = unavailableCount

            finishRevision(revisionId, ScanStatus.COMPLETED, counts)

            ScanResult.Success(
                revisionId = revisionId,
                scannedCount = counts.scanned,
                addedCount = counts.added,
                updatedCount = counts.updated,
                unavailableCount = counts.unavailable,
            )
        } catch (securityException: SecurityException) {
            finishRevision(revisionId, ScanStatus.FAILED, counts)
            Timber.tag(TAG).w(securityException, "Audio permission denied during scan")
            ScanResult.PermissionDenied
        } catch (cancellation: CancellationException) {
            finishRevision(revisionId, ScanStatus.CANCELLED, counts)
            throw cancellation
        } catch (exception: Exception) {
            finishRevision(revisionId, ScanStatus.FAILED, counts)
            Timber.tag(TAG).e(exception, "Scan failed")
            ScanResult.Failed(exception.message ?: "Unknown scan failure")
        }
    }

    // Scans a single media volume for audio tracks.
    private suspend fun scanVolume(
        volumeName: String,
        revisionId: Long,
        counts: ScanCounts,
    ) {
        val collection = audioCollection(volumeName)

        val projection = buildProjection()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val existingByMediaStoreId = database.trackDao()
            .getLocatorsForVolume(volumeName)
            .associateBy { locator -> locator.mediaStoreId }

        val batch = ArrayList<TrackEntity>(BATCH_SIZE)

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                coroutineContext.ensureActive()

                val mediaStoreId = cursor.getLong(idColumn)
                val existing = existingByMediaStoreId[mediaStoreId]
                val contentUri = ContentUris.withAppendedId(collection, mediaStoreId)

                val entity = TrackEntity(
                    auraUuid = existing?.auraUuid ?: UUID.randomUUID().toString(),
                    volumeName = volumeName,
                    mediaStoreId = mediaStoreId,
                    contentUri = contentUri.toString(),
                    title = cursor.getString(titleColumn).orEmpty().ifBlank { "Unknown title" },
                    artist = cursor.getString(artistColumn).orEmpty().ifBlank { "Unknown artist" },
                    album = cursor.getString(albumColumn).orEmpty().ifBlank { "Unknown album" },
                    albumId = cursor.getLong(albumIdColumn),
                    durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L),
                    sizeBytes = cursor.getLong(sizeColumn).coerceAtLeast(0L),
                    dateModified = cursor.getLong(dateModifiedColumn).coerceAtLeast(0L),
                    availability = TrackAvailability.AVAILABLE,
                    firstSeenRevision = existing?.firstSeenRevision ?: revisionId,
                    lastSeenRevision = revisionId,
                )

                if (existing == null) {
                    counts.added += 1
                } else {
                    counts.updated += 1
                }

                counts.scanned += 1
                batch.add(entity)

                if (batch.size >= BATCH_SIZE) {
                    database.trackDao().upsertTracks(batch)
                    batch.clear()
                }
            }
        }

        if (batch.isNotEmpty()) {
            database.trackDao().upsertTracks(batch)
            batch.clear()
        }
    }

    // Derives artists, albums, genres, and their relationships from scanned tracks.
    // This runs after all tracks are upserted for the current scan.
    private suspend fun deriveLibraryRelationships(revisionId: Long) {
        val tracks = database.trackDao().getAvailableSongs()
        if (tracks.isEmpty()) return

        val artistMap = mutableMapOf<String, ArtistEntity>()
        val albumMap = mutableMapOf<Long, AlbumEntity>()
        val genreMap = mutableMapOf<String, GenreEntity>()
        val trackArtistLinks = mutableListOf<TrackArtistEntity>()
        val albumArtistLinks = mutableListOf<AlbumArtistEntity>()
        val trackGenreLinks = mutableListOf<TrackGenreEntity>()

        for (track in tracks) {
            coroutineContext.ensureActive()

            // Derive artist.
            val artistName = track.artist.ifBlank { "Unknown artist" }
            val normalizedArtist = normalizeName(artistName)
            val existingArtist = artistMap[normalizedArtist]
            val artistUuid = existingArtist?.artistUuid ?: UUID.randomUUID().toString()

            if (existingArtist == null) {
                artistMap[normalizedArtist] = ArtistEntity(
                    artistUuid = artistUuid,
                    name = artistName,
                    normalizedName = normalizedArtist,
                    availability = TrackAvailability.AVAILABLE,
                    firstSeenRevision = revisionId,
                    lastSeenRevision = revisionId,
                )
            } else {
                artistMap[normalizedArtist] = existingArtist.copy(lastSeenRevision = revisionId)
            }

            trackArtistLinks.add(
                TrackArtistEntity(
                    trackUuid = track.auraUuid,
                    artistUuid = artistUuid,
                )
            )

            // Derive album.
            val albumName = track.album.ifBlank { "Unknown album" }
            val normalizedAlbum = normalizeName(albumName)
            val existingAlbum = albumMap[track.albumId]
            val albumUuid = existingAlbum?.albumUuid ?: UUID.randomUUID().toString()

            if (existingAlbum == null) {
                albumMap[track.albumId] = AlbumEntity(
                    albumUuid = albumUuid,
                    name = albumName,
                    normalizedName = normalizedAlbum,
                    mediaStoreAlbumId = track.albumId,
                    year = null,
                    availability = TrackAvailability.AVAILABLE,
                    firstSeenRevision = revisionId,
                    lastSeenRevision = revisionId,
                )
            } else {
                albumMap[track.albumId] = existingAlbum.copy(lastSeenRevision = revisionId)
            }

            // Link album to album artist.
            albumArtistLinks.add(
                AlbumArtistEntity(
                    albumUuid = albumUuid,
                    artistUuid = artistUuid,
                )
            )
        }

        // Derive genres from MediaStore if available.
        deriveGenres(revisionId, genreMap, trackGenreLinks)

        // Upsert all derived entities.
        if (artistMap.isNotEmpty()) {
            database.artistDao().upsertArtists(artistMap.values.toList())
        }
        if (albumMap.isNotEmpty()) {
            database.albumDao().upsertAlbums(albumMap.values.toList())
        }
        if (genreMap.isNotEmpty()) {
            database.genreDao().upsertGenres(genreMap.values.toList())
        }

        // Upsert junction tables.
        if (trackArtistLinks.isNotEmpty()) {
            database.artistDao().upsertTrackArtistLinks(trackArtistLinks.distinct())
        }
        if (albumArtistLinks.isNotEmpty()) {
            database.artistDao().upsertAlbumArtistLinks(albumArtistLinks.distinct())
        }
        if (trackGenreLinks.isNotEmpty()) {
            database.genreDao().upsertTrackGenreLinks(trackGenreLinks.distinct())
        }
    }

    // Derives genres from MediaStore genre data.
    // Genre information availability depends on the Android version and media content.
    private suspend fun deriveGenres(
        revisionId: Long,
        genreMap: MutableMap<String, GenreEntity>,
        trackGenreLinks: MutableList<TrackGenreEntity>,
    ) {
        val tracks = database.trackDao().getAvailableSongs()
        if (tracks.isEmpty()) return

        // MediaStore genre data is not always available per-track.
        // For Phase 2.2, we derive genres from the track metadata if present.
        // Future phases may use MediaStore.Audio.Genres for richer genre data.
        // Currently TrackEntity does not store genre, so this is a placeholder
        // for when genre scanning is added to the projection.
    }

    // Marks unseen records as unavailable across all entity types.
    private suspend fun markUnseenUnavailable(revisionId: Long): Int {
        val trackUnavailable = database.trackDao().markUnseenUnavailable(
            revisionId = revisionId,
            unavailable = TrackAvailability.UNAVAILABLE,
            available = TrackAvailability.AVAILABLE,
        )
        database.artistDao().markUnseenUnavailable(
            revisionId = revisionId,
            unavailable = TrackAvailability.UNAVAILABLE,
            available = TrackAvailability.AVAILABLE,
        )
        database.albumDao().markUnseenUnavailable(
            revisionId = revisionId,
            unavailable = TrackAvailability.UNAVAILABLE,
            available = TrackAvailability.AVAILABLE,
        )
        database.genreDao().markUnseenUnavailable(
            revisionId = revisionId,
            unavailable = TrackAvailability.UNAVAILABLE,
            available = TrackAvailability.AVAILABLE,
        )
        return trackUnavailable
    }

    // Normalizes a name for deduplication and sorting.
    // Converts to lowercase and trims whitespace.
    private fun normalizeName(name: String): String {
        return name.trim().lowercase()
    }

    // Builds the MediaStore projection array.
    private fun buildProjection(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
    }

    private suspend fun finishRevision(
        revisionId: Long,
        status: Int,
        counts: ScanCounts,
    ) {
        withContext(NonCancellable) {
            database.scanRevisionDao().finish(
                id = revisionId,
                finishedAt = System.currentTimeMillis(),
                status = status,
                scannedCount = counts.scanned,
                addedCount = counts.added,
                updatedCount = counts.updated,
                unavailableCount = counts.unavailable,
            )
        }
    }

    private fun accessibleVolumes(): List<String> {
        return listOf(FALLBACK_EXTERNAL_VOLUME)
    }

    private fun audioCollection(volumeName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(volumeName)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }

    private class ScanCounts {
        var scanned: Int = 0
        var added: Int = 0
        var updated: Int = 0
        var unavailable: Int = 0
    }
}