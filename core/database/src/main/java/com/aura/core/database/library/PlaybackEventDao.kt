package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: PlaybackEventEntity)

    @Query("SELECT * FROM playback_events WHERE trackUuid = :trackUuid ORDER BY timestamp DESC")
    fun getEventsForTrack(trackUuid: String): Flow<List<PlaybackEventEntity>>
    
    @Query("SELECT COUNT(*) FROM playback_events WHERE eventType = :eventType AND trackUuid = :trackUuid")
    suspend fun countEvents(trackUuid: String, eventType: Int): Int

    @Query(
        """
        SELECT track_sources.* FROM track_sources
        INNER JOIN (
            SELECT trackUuid, MAX(timestamp) as max_ts
            FROM playback_events
            GROUP BY trackUuid
        ) ON track_sources.auraUuid = trackUuid
        WHERE availability = 0
        ORDER BY max_ts DESC
        LIMIT :limit
        """
    )
    fun getRecentlyPlayedTracks(limit: Int): Flow<List<TrackEntity>>
}
