package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

// A lightweight projection used during scan reconciliation.
// The scanner uses this to preserve durable AURA UUIDs across rescans
// without loading full entity rows.
data class TrackLocator(
    val mediaStoreId: Long,
    val auraUuid: String,
    val firstSeenRevision: Long,
)

// Data access object for scanned track sources.
@Dao
interface TrackDao {

    // Inserts or updates a batch of scanned tracks.
    @Upsert
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    // Returns only identity fields for reconciliation within a volume.
    @Query(
        """
        SELECT mediaStoreId, auraUuid, firstSeenRevision
        FROM track_sources
        WHERE volumeName = :volumeName
        """
    )
    suspend fun getLocatorsForVolume(volumeName: String): List<TrackLocator>

    // Returns all available tracks sorted by title for the library song list.
    @Query(
        """
        SELECT *
        FROM track_sources
        WHERE availability = 0
        ORDER BY title COLLATE NOCASE ASC
        """
    )
    suspend fun getAvailableSongs(): List<TrackEntity>

    // Returns available tracks matching a set of MediaStore IDs.
    // Used by playback recovery to validate queue identities.
    @Query(
        """
        SELECT *
        FROM track_sources
        WHERE mediaStoreId IN (:mediaStoreIds)
        AND availability = 0
        """
    )
    suspend fun getAvailableByMediaStoreIds(mediaStoreIds: List<Long>): List<TrackEntity>

    // Returns the count of available tracks.
    @Query("SELECT COUNT(*) FROM track_sources WHERE availability = 0")
    suspend fun availableTrackCount(): Int

    // Marks tracks not seen in the current revision as unavailable.
    // This never deletes rows. Section 120.2 of the specification.
    @Query(
        """
        UPDATE track_sources
        SET availability = :unavailable
        WHERE lastSeenRevision < :revisionId
        AND availability = :available
        """
    )
    suspend fun markUnseenUnavailable(
        revisionId: Long,
        unavailable: Int,
        available: Int,
    ): Int
}