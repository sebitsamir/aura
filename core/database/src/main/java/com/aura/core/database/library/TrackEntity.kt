package com.aura.core.database.library

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Availability states for scanned track sources.
// UNAVAILABLE tracks are retained so user data is never destroyed
// when a removable volume disappears temporarily.
object TrackAvailability {
    const val AVAILABLE = 0
    const val UNAVAILABLE = 1
}

// The durable library identity record for one discovered audio source.
// auraUuid is the stable AURA identity. MediaStore ID is a locator, not identity.
@Entity(
    tableName = "track_sources",
    indices = [
        Index(value = ["volumeName", "mediaStoreId"], unique = true),
        Index(value = ["availability"]),
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["folderPath"]),
    ],
)
data class TrackEntity(
    @PrimaryKey
    val auraUuid: String,
    val volumeName: String,
    val mediaStoreId: Long,
    val contentUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    @ColumnInfo(defaultValue = "")
    val folderPath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateModified: Long,
    val availability: Int,
    val firstSeenRevision: Long,
    val lastSeenRevision: Long,
)