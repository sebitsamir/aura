package com.aura.core.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.aura.core.database.AuraDatabase
import com.aura.core.database.library.ScanRevisionEntity
import com.aura.core.database.library.ScanStatus
import com.aura.core.database.library.TrackAvailability
import com.aura.core.database.library.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Result of a library scan attempt.
sealed interface ScanResult {

    // The scan completed and the database was reconciled.
    data class Success(
        val revisionId: Long,
        val scannedCount: Int,
        val addedCount: Int,
        val updatedCount: Int,
        val unavailableCount: Int,
    ) : ScanResult

    // The scan could not run because audio permission was denied.
    data object PermissionDenied : ScanResult

    // The scan failed for a non-permission reason.
    data class Failed(val message: String) : ScanResult
}

// Phase 2.1 MediaStore scanner.
// Scans all accessible external volumes, writes track identity records to Room,
// and marks previously-seen tracks that are no longer present as unavailable.
// The scanner never deletes rows. Section 120.2 of the specification.
@Singleton
class MediaStoreTrackScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AuraDatabase,
) {

    private companion object {
        const val TAG = "MediaStoreTrackScanner"
        const val BATCH_SIZE = 200
        const val FALLBACK_EXTERNAL_VOLUME = "external"
    }

    // Runs a full scan across accessible external volumes.
    // This method is idempotent. Running it twice produces the same result.
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
                currentCoroutineContext().ensureActive()
                try {
                    scanVolume(volume, revisionId, counts)
                } catch (securityException: SecurityException) {
                    // Permission failures must stop the entire scan.
                    throw securityException
                } catch (exception: Exception) {
                    // A single problematic volume should not destroy the scan.
                    Timber.tag(TAG).e(exception, "Failed to scan volume")
                }
            }

            // Mark tracks that were not seen in this revision as unavailable.
            val unavailableCount = database.trackDao().markUnseenUnavailable(
                revisionId = revisionId,
                unavailable = TrackAvailability.UNAVAILABLE,
                available = TrackAvailability.AVAILABLE,
            )
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

    // Scans one media volume and upserts track sources in bounded batches.
    private suspend fun scanVolume(
        volumeName: String,
        revisionId: Long,
        counts: ScanCounts,
    ) {
        val collection = audioCollection(volumeName)

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        // Load existing locators for this volume to preserve UUIDs across rescans.
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
                currentCoroutineContext().ensureActive()

                val mediaStoreId = cursor.getLong(idColumn)
                val existing = existingByMediaStoreId[mediaStoreId]
                val contentUri = ContentUris.withAppendedId(collection, mediaStoreId)

                val entity = TrackEntity(
                    // Preserve the existing UUID if this track was seen before.
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

        // Flush any remaining items in the batch.
        if (batch.isNotEmpty()) {
            database.trackDao().upsertTracks(batch)
            batch.clear()
        }
    }

    // Finishes the revision inside a non-cancellable context.
    // This prevents cancelled scans from leaving a revision stuck as RUNNING.
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

    // Returns the external volumes that should be scanned.
    private fun accessibleVolumes(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.getVolumeNames(context)
                .filterNot { volume -> volume == MediaStore.VOLUME_INTERNAL }
        } else {
            listOf(FALLBACK_EXTERNAL_VOLUME)
        }
    }

    // Returns the MediaStore audio collection URI for a given volume.
    private fun audioCollection(volumeName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(volumeName)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }

    // Mutable counters for one scan attempt.
    private class ScanCounts {
        var scanned: Int = 0
        var added: Int = 0
        var updated: Int = 0
        var unavailable: Int = 0
    }
}