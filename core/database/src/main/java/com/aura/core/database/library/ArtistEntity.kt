package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// A unique artist derived from scanned tracks.
// Artists are identified by a durable AURA UUID.
// The normalizedName is used for deduplication and sorting.
@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["normalizedName"], unique = true),
    ],
)
data class ArtistEntity(
    @PrimaryKey
    val artistUuid: String,
    val name: String,
    val normalizedName: String,
    val availability: Int,
    val firstSeenRevision: Long,
    val lastSeenRevision: Long,
)