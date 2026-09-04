package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

data class TrackLocator(
    val mediaStoreId: Long,
    val auraUuid: String,
    val firstSeenRevision: Long,
    val dateModified: Long,
)

@Dao
interface TrackDao {

    @Upsert
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query(
        """
        SELECT mediaStoreId, auraUuid, firstSeenRevision, dateModified
        FROM track_sources
        WHERE volumeName = :volumeName
        """
    )
    suspend fun getLocatorsForVolume(volumeName: String): List<TrackLocator>

    @Query(
        """
        SELECT *
        FROM track_sources
        WHERE availability = 0
        ORDER BY title COLLATE NOCASE ASC
        """
    )
    suspend fun getAvailableSongs(): List<TrackEntity>

    @Query(
        """
        SELECT *
        FROM track_sources
        WHERE mediaStoreId IN (:mediaStoreIds)
        AND availability = 0
        """
    )
    suspend fun getAvailableByMediaStoreIds(mediaStoreIds: List<Long>): List<TrackEntity>

    @Query("SELECT COUNT(*) FROM track_sources WHERE availability = 0")
    suspend fun availableTrackCount(): Int

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

    // Updates lastSeenRevision for tracks that have not changed.
    // Used during incremental scanning to avoid full metadata extraction.
    @Query(
        """
        UPDATE track_sources
        SET lastSeenRevision = :revisionId
        WHERE volumeName = :volumeName
        AND mediaStoreId IN (:mediaStoreIds)
        """
    )
    suspend fun updateLastSeenRevision(
        volumeName: String,
        mediaStoreIds: List<Long>,
        revisionId: Long,
    )

    // Marks specific tracks as unavailable by their MediaStore IDs.
    // Used during incremental scanning for files that no longer exist.
    @Query(
        """
        UPDATE track_sources
        SET availability = :unavailable
        WHERE volumeName = :volumeName
        AND mediaStoreId IN (:mediaStoreIds)
        """
    )
    suspend fun markTracksUnavailable(
        volumeName: String,
        mediaStoreIds: List<Long>,
        unavailable: Int,
    )
}