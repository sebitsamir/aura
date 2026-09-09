package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_user_overlays",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["auraUuid"],
            childColumns = ["trackUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trackUuid"], unique = true)]
)
data class TrackUserOverlayEntity(
    @PrimaryKey val trackUuid: String,
    val isFavorite: Boolean = false,
    val rating: Int? = null,
    val userTags: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)