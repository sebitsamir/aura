package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

// Data access object for genres.
@Dao
interface GenreDao {

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Upsert
    suspend fun upsertTrackGenreLinks(links: List<TrackGenreEntity>)

    @Query(
        """
        SELECT *
        FROM genres
        WHERE availability = 0
        ORDER BY normalizedName COLLATE NOCASE ASC
        """
    )
    suspend fun getAvailableGenres(): List<GenreEntity>

    @Query(
        """
        SELECT genres.*
        FROM genres
        INNER JOIN track_genre ON genres.genreUuid = track_genre.genreUuid
        WHERE track_genre.trackUuid = :trackUuid
        AND genres.availability = 0
        """
    )
    suspend fun getGenresForTrack(trackUuid: String): List<GenreEntity>

    @Query(
        """
        SELECT tracks.*
        FROM track_sources AS tracks
        INNER JOIN track_genre ON tracks.auraUuid = track_genre.trackUuid
        WHERE track_genre.genreUuid = :genreUuid
        AND tracks.availability = 0
        ORDER BY tracks.title COLLATE NOCASE ASC
        """
    )
    suspend fun getTracksForGenre(genreUuid: String): List<TrackEntity>

    @Query("SELECT COUNT(*) FROM genres WHERE availability = 0")
    suspend fun availableGenreCount(): Int

    @Query(
        """
        UPDATE genres
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