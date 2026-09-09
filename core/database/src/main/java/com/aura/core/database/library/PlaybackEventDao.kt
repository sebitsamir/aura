package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaybackEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: PlaybackEventEntity)

    @Query("SELECT * FROM playback_events WHERE trackUuid = :trackUuid ORDER BY timestamp DESC")
    suspend fun getEventsForTrack(trackUuid: String): List<PlaybackEventEntity>
    
    @Query("SELECT COUNT(*) FROM playback_events WHERE eventType = :eventType AND trackUuid = :trackUuid")
    suspend fun countEvents(trackUuid: String, eventType: Int): Int
}