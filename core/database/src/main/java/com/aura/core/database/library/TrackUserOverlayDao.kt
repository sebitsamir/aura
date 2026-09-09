package com.aura.core.database.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackUserOverlayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(overlay: TrackUserOverlayEntity)

    @Query("SELECT * FROM track_user_overlays WHERE trackUuid = :trackUuid")
    suspend fun getOverlay(trackUuid: String): TrackUserOverlayEntity?

    @Query("SELECT * FROM track_user_overlays WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<TrackUserOverlayEntity>>
    
    @Query("UPDATE track_user_overlays SET isFavorite = :isFavorite, lastModified = :timestamp WHERE trackUuid = :trackUuid")
    suspend fun setFavorite(trackUuid: String, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())
}