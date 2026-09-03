package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Junction table linking tracks to artists.
// A track may have one or more artists in theory.
// MediaStore currently provides one artist per track.
@Entity(
    tableName = "track_artist",
    primaryKeys = ["trackUuid", "artistUuid"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["auraUuid"],
            childColumns = ["trackUuid"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["artistUuid"],
            childColumns = ["artistUuid"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["artistUuid"]),
    ],
)
data class TrackArtistEntity(
    val trackUuid: String,
    val artistUuid: String,
)