package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Data access object for scan revisions.
@Dao
interface ScanRevisionDao {

    // Starts a new scan revision and returns its generated row ID.
    @Insert
    suspend fun insert(revision: ScanRevisionEntity): Long

    // Completes a scan revision with final counts and terminal status.
    @Query(
        """
        UPDATE scan_revisions
        SET finishedAt = :finishedAt,
            status = :status,
            scannedCount = :scannedCount,
            addedCount = :addedCount,
            updatedCount = :updatedCount,
            unavailableCount = :unavailableCount
        WHERE id = :id
        """
    )
    suspend fun finish(
        id: Long,
        finishedAt: Long,
        status: Int,
        scannedCount: Int,
        addedCount: Int,
        updatedCount: Int,
        unavailableCount: Int,
    )

    // Returns the most recent scan revision.
    @Query("SELECT * FROM scan_revisions ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): ScanRevisionEntity?
}