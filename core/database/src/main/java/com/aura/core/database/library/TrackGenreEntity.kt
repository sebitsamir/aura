package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Junction table linking tracks to genres.
// A track may have one or more genres in theory.
// MediaStore currently provides one genre per track.
@Entity(
    tableName = "track_genre",
    primaryKeys = ["trackUuid", "genreUuid"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["auraUuid"],
            childColumns = ["trackUuid"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GenreEntity::class,
            parentColumns = ["genreUuid"],
            childColumns = ["genreUuid"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["genreUuid"]),
    ],
)
data class TrackGenreEntity(
    val trackUuid: String,
    val genreUuid: String,
)