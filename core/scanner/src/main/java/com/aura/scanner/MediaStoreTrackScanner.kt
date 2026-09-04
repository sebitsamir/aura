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
import com.aura.core.database.library.FolderEntity
import com.aura.core.database.library.GenreEntity
import com.aura.core.database.library.ScanRevisionEntity
import com.aura.core.database.library.ScanStatus
import com.aura.core.database.library.TrackArtistEntity
import com.aura.core.database.library.TrackAvailability
import com.aura.core.database.library.TrackEntity
import com.aura.core.database.library.TrackGenreEntity
import com.aura.core.model.ScanPhase
import com.aura.core.model.ScanProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

// Scans MediaStore and populates the Room database with tracks,
// artists, albums, genres, folders, and their relationships.
// Supports incremental scanning for efficient rescans.
// Emits scan progress for UI observation.
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

    private val _scanProgress = MutableSharedFlow<ScanProgress>(extraBufferCapacity = 64)

    // Observable scan progress. The UI collects this flow to show scanning status.
    val scanProgress: SharedFlow<ScanProgress> = _scanProgress.asSharedFlow()

    // Performs a full scan of the external media volume.
    // Scans tracks, then derives artists, albums, genres, and folders.
    // Uses incremental scanning to skip unchanged files.
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
            emitProgress(ScanPhase.ENUMERATING, 0, 0, "Enumerating media volumes")

            val volumes = accessibleVolumes()

            for (volume in volumes) {
                coroutineContext.ensureActive()
                try {
                    scanVolumeIncremental(volume, revisionId, counts)
                } catch (securityException: SecurityException) {
                    throw securityException
                } catch (exception: Exception) {
                    Timber.tag(TAG).e(exception, "Failed to scan volume")
                }
            }

            emitProgress(ScanPhase.DERIVING_RELATIONSHIPS, 0, 0, "Deriving library relationships")
            coroutineContext.ensureActive()
            deriveLibraryRelationships(revisionId)

            emitProgress(ScanPhase.MARKING_UNAVAILABLE, 0, 0, "Marking unavailable sources")
            val unavailableCount = markUnseenUnavailable(revisionId)
            counts.unavailable = unavailableCount

            emitProgress(ScanPhase.COMPLETING, 0, 0, "Completing scan")
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

    // Performs an incremental scan of a single media volume.
    // Only extracts full metadata for new or modified files.
    // Unchanged files only get their lastSeenRevision updated.
    private suspend fun scanVolumeIncremental(
        volumeName: String,
        revisionId: Long,
        counts: ScanCounts,
    ) {
        val collection = audioCollection(volumeName)

        // Step 1: Lightweight enumeration of all file IDs and timestamps.
        emitProgress(ScanPhase.ENUMERATING, 0, 0, "Enumerating files in $volumeName")

        val mediaStoreFiles = mutableListOf<Pair<Long, Long>>()

        val enumProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
        val enumSelection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            collection,
            enumProjection,
            enumSelection,
            null,
            null,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified = cursor.getLong(dateModifiedColumn).coerceAtLeast(0L)
                mediaStoreFiles.add(id to dateModified)
            }
        }

        val totalFiles = mediaStoreFiles.size
        emitProgress(ScanPhase.ENUMERATING, totalFiles, totalFiles, "Found $totalFiles files")

        // Step 2: Compare with existing database records.
        val existingLocators = database.trackDao().getLocatorsForVolume(volumeName)
        val existingByMediaStoreId = existingLocators.associateBy { it.mediaStoreId }
        val mediaStoreIdSet = mediaStoreFiles.map { it.first }.toSet()

        // Determine which files need full metadata extraction.
        val needsFullScan = mutableListOf<Pair<Long, Long>>()
        val unchangedIds = mutableListOf<Long>()

        for ((mediaStoreId, dateModified) in mediaStoreFiles) {
            val existing = existingByMediaStoreId[mediaStoreId]
            if (existing == null) {
                // New file: needs full metadata extraction.
                needsFullScan.add(mediaStoreId to dateModified)
            } else if (dateModified > existing.dateModified) {
                // Modified file: needs full metadata extraction.
                needsFullScan.add(mediaStoreId to dateModified)
            } else {
                // Unchanged file: only update lastSeenRevision.
                unchangedIds.add(mediaStoreId)
            }
        }

        // Determine which files have been removed.
        val removedIds = existingByMediaStoreId.keys.filter { it !in mediaStoreIdSet }

        // Step 3: Extract full metadata for new/modified files.
        val fullScanIdSet = needsFullScan.map { it.first }.toSet()
        emitProgress(ScanPhase.SCANNING, 0, fullScanIdSet.size, "Scanning new and modified files")

        if (fullScanIdSet.isNotEmpty()) {
            val projection = buildProjection()
            val idList = fullScanIdSet.joinToString(",")
            val selection = "${MediaStore.Audio.Media._ID} IN ($idList)"

            val batch = ArrayList<TrackEntity>(BATCH_SIZE)
            var scannedCount = 0

            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                null,
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
                    val folderPath = extractFolderPath(cursor)

                    val entity = TrackEntity(
                        auraUuid = existing?.auraUuid ?: UUID.randomUUID().toString(),
                        volumeName = volumeName,
                        mediaStoreId = mediaStoreId,
                        contentUri = contentUri.toString(),
                        title = cursor.getString(titleColumn).orEmpty().ifBlank { "Unknown title" },
                        artist = cursor.getString(artistColumn).orEmpty().ifBlank { "Unknown artist" },
                        album = cursor.getString(albumColumn).orEmpty().ifBlank { "Unknown album" },
                        albumId = cursor.getLong(albumIdColumn),
                        folderPath = folderPath,
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

                    scannedCount += 1
                    if (scannedCount % 50 == 0) {
                        emitProgress(ScanPhase.SCANNING, scannedCount, fullScanIdSet.size, "Scanning files")
                    }
                }
            }

            if (batch.isNotEmpty()) {
                database.trackDao().upsertTracks(batch)
                batch.clear()
            }
        }

        // Step 4: Update lastSeenRevision for unchanged files.
        if (unchangedIds.isNotEmpty()) {
            unchangedIds.chunked(BATCH_SIZE).forEach { chunk ->
                coroutineContext.ensureActive()
                database.trackDao().updateLastSeenRevision(volumeName, chunk, revisionId)
            }
            counts.scanned += unchangedIds.size
        }

        // Step 5: Mark removed files as unavailable.
        if (removedIds.isNotEmpty()) {
            removedIds.chunked(BATCH_SIZE).forEach { chunk ->
                coroutineContext.ensureActive()
                database.trackDao().markTracksUnavailable(
                    volumeName,
                    chunk,
                    TrackAvailability.UNAVAILABLE,
                )
            }
            counts.unavailable += removedIds.size
        }

        emitProgress(ScanPhase.SCANNING, totalFiles, totalFiles, "Volume scan complete")
    }

    // Extracts the folder path from the current cursor row.
    // Uses RELATIVE_PATH on API 29+ and DATA on older versions.
    private fun extractFolderPath(cursor: android.database.Cursor): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativePathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            if (relativePathColumn >= 0) {
                cursor.getString(relativePathColumn)?.removeSuffix("/") ?: ""
            } else {
                extractFolderFromData(cursor)
            }
        } else {
            extractFolderFromData(cursor)
        }
    }

    // Extracts the folder path from the DATA column on API < 29.
    private fun extractFolderFromData(cursor: android.database.Cursor): String {
        val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        if (dataColumn < 0) return ""

        val dataPath = cursor.getString(dataColumn) ?: return ""
        val lastSlash = dataPath.lastIndexOf('/')
        return if (lastSlash > 0) dataPath.substring(0, lastSlash) else ""
    }

    // Builds the full MediaStore projection array for metadata extraction.
    private fun buildProjection(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.RELATIVE_PATH,
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.DATA,
            )
        }
    }

    // Derives artists, albums, genres, and folders from scanned tracks.
    // This runs after all tracks are upserted for the current scan.
    private suspend fun deriveLibraryRelationships(revisionId: Long) {
        val tracks = database.trackDao().getAvailableSongs()
        if (tracks.isEmpty()) return

        val artistMap = mutableMapOf<String, ArtistEntity>()
        val albumMap = mutableMapOf<Long, AlbumEntity>()
        val genreMap = mutableMapOf<String, GenreEntity>()
        val folderMap = mutableMapOf<String, FolderEntity>()
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

            // Derive folder.
            if (track.folderPath.isNotBlank()) {
                val normalizedFolder = normalizeName(track.folderPath)
                val existingFolder = folderMap[normalizedFolder]
                val folderUuid = existingFolder?.folderUuid ?: UUID.randomUUID().toString()
                val folderName = track.folderPath.substringAfterLast('/')

                if (existingFolder == null) {
                    val rawParentPath = if (folderName.length < track.folderPath.length) {
                        track.folderPath.removeSuffix("/$folderName").removeSuffix(folderName)
                    } else {
                        null
                    }

                    folderMap[normalizedFolder] = FolderEntity(
                        folderUuid = folderUuid,
                        path = track.folderPath,
                        name = folderName.ifBlank { track.folderPath },
                        parentPath = rawParentPath?.ifBlank { null },
                        availability = TrackAvailability.AVAILABLE,
                        firstSeenRevision = revisionId,
                        lastSeenRevision = revisionId,
                    )
                }
            }
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
        if (folderMap.isNotEmpty()) {
            database.folderDao().upsertFolders(folderMap.values.toList())
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
        // Genre derivation remains a placeholder.
        // MediaStore genre data is not always available per-track.
        // Future phases may use MediaStore.Audio.Genres for richer genre data.
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
        database.folderDao().markUnseenUnavailable(
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

    // Emits scan progress to the shared flow.
    private suspend fun emitProgress(
        phase: ScanPhase,
        current: Int,
        total: Int,
        message: String,
    ) {
        _scanProgress.emit(
            ScanProgress(
                phase = phase,
                current = current,
                total = total,
                message = message,
            )
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