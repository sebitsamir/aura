package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// A unique album derived from scanned tracks.
// Albums are identified by a durable AURA UUID.
// The mediaStoreAlbumId is the locator from MediaStore.
@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["mediaStoreAlbumId"]),
        Index(value = ["normalizedName"]),
    ],
)
data class AlbumEntity(
    @PrimaryKey
    val albumUuid: String,
    val name: String,
    val normalizedName: String,
    val mediaStoreAlbumId: Long,
    val year: Int?,
    val availability: Int,
    val firstSeenRevision: Long,
    val lastSeenRevision: Long,
)