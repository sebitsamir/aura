package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// A unique genre derived from scanned tracks.
// Genres are identified by a durable AURA UUID.
@Entity(
    tableName = "genres",
    indices = [
        Index(value = ["normalizedName"], unique = true),
    ],
)
data class GenreEntity(
    @PrimaryKey
    val genreUuid: String,
    val name: String,
    val normalizedName: String,
    val availability: Int,
    val firstSeenRevision: Long,
    val lastSeenRevision: Long,
)