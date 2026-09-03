package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

// Data access object for albums.
@Dao
interface AlbumDao {

    @Upsert
    suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Query(
        """
        SELECT *
        FROM albums
        WHERE availability = 0
        ORDER BY normalizedName COLLATE NOCASE ASC
        """
    )
    suspend fun getAvailableAlbums(): List<AlbumEntity>

    @Query(
        """
        SELECT *
        FROM albums
        WHERE availability = 0
        AND mediaStoreAlbumId = :mediaStoreAlbumId
        LIMIT 1
        """
    )
    suspend fun getAlbumByMediaStoreId(mediaStoreAlbumId: Long): AlbumEntity?

    @Query(
        """
        SELECT tracks.*
        FROM track_sources AS tracks
        WHERE tracks.albumId = :mediaStoreAlbumId
        AND tracks.availability = 0
        ORDER BY tracks.title COLLATE NOCASE ASC
        """
    )
    suspend fun getTracksForAlbum(mediaStoreAlbumId: Long): List<TrackEntity>

    @Query(
        """
        SELECT albums.*
        FROM albums
        INNER JOIN album_artist ON albums.albumUuid = album_artist.albumUuid
        WHERE album_artist.artistUuid = :artistUuid
        AND albums.availability = 0
        ORDER BY albums.normalizedName COLLATE NOCASE ASC
        """
    )
    suspend fun getAlbumsForArtist(artistUuid: String): List<AlbumEntity>

    @Query("SELECT COUNT(*) FROM albums WHERE availability = 0")
    suspend fun availableAlbumCount(): Int

    @Query(
        """
        UPDATE albums
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