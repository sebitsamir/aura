package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

// Data access object for folders.
@Dao
interface FolderDao {

    @Upsert
    suspend fun upsertFolders(folders: List<FolderEntity>)

    @Query(
        """
        SELECT *
        FROM folders
        WHERE availability = 0
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    suspend fun getAvailableFolders(): List<FolderEntity>

    @Query(
        """
        SELECT *
        FROM folders
        WHERE availability = 0
        AND path = :path
        LIMIT 1
        """
    )
    suspend fun getFolderByPath(path: String): FolderEntity?

    @Query(
        """
        SELECT *
        FROM track_sources
        WHERE folderPath = :folderPath
        AND availability = 0
        ORDER BY title COLLATE NOCASE ASC
        """
    )
    suspend fun getTracksForFolder(folderPath: String): List<TrackEntity>

    @Query("SELECT COUNT(*) FROM track_sources WHERE folderPath = :folderPath AND availability = 0")
    suspend fun getTrackCountForFolder(folderPath: String): Int

    @Query("SELECT COUNT(*) FROM folders WHERE availability = 0")
    suspend fun availableFolderCount(): Int

    @Query(
        """
        UPDATE folders
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