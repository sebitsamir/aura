package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

// Data access object for artists.
@Dao
interface ArtistDao {

    @Upsert
    suspend fun upsertArtists(artists: List<ArtistEntity>)

    @Upsert
    suspend fun upsertTrackArtistLinks(links: List<TrackArtistEntity>)

    @Upsert
    suspend fun upsertAlbumArtistLinks(links: List<AlbumArtistEntity>)

    @Query(
        """
        SELECT *
        FROM artists
        WHERE availability = 0
        ORDER BY normalizedName COLLATE NOCASE ASC
        """
    )
    suspend fun getAvailableArtists(): List<ArtistEntity>

    @Query(
        """
        SELECT *
        FROM artists
        WHERE availability = 0
        AND normalizedName = :normalizedName
        LIMIT 1
        """
    )
    suspend fun getArtistByNormalizedName(normalizedName: String): ArtistEntity?

    @Query(
        """
        SELECT artists.*
        FROM artists
        INNER JOIN track_artist ON artists.artistUuid = track_artist.artistUuid
        WHERE track_artist.trackUuid = :trackUuid
        AND artists.availability = 0
        """
    )
    suspend fun getArtistsForTrack(trackUuid: String): List<ArtistEntity>

    @Query(
        """
        SELECT tracks.*
        FROM track_sources AS tracks
        INNER JOIN track_artist ON tracks.auraUuid = track_artist.trackUuid
        WHERE track_artist.artistUuid = :artistUuid
        AND tracks.availability = 0
        ORDER BY tracks.title COLLATE NOCASE ASC
        """
    )
    suspend fun getTracksForArtist(artistUuid: String): List<TrackEntity>

    @Query("SELECT COUNT(*) FROM artists WHERE availability = 0")
    suspend fun availableArtistCount(): Int

    @Query(
        """
        UPDATE artists
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