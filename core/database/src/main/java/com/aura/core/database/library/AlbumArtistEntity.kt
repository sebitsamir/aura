package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Junction table linking albums to album artists.
// An album has one primary album artist.
@Entity(
    tableName = "album_artist",
    primaryKeys = ["albumUuid", "artistUuid"],
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["albumUuid"],
            childColumns = ["albumUuid"],
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
data class AlbumArtistEntity(
    val albumUuid: String,
    val artistUuid: String,
)