package com.aura.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.core.database.library.AlbumArtistEntity
import com.aura.core.database.library.AlbumDao
import com.aura.core.database.library.AlbumEntity
import com.aura.core.database.library.ArtistDao
import com.aura.core.database.library.ArtistEntity
import com.aura.core.database.library.GenreDao
import com.aura.core.database.library.GenreEntity
import com.aura.core.database.library.ScanRevisionDao
import com.aura.core.database.library.ScanRevisionEntity
import com.aura.core.database.library.TrackArtistEntity
import com.aura.core.database.library.TrackDao
import com.aura.core.database.library.TrackEntity
import com.aura.core.database.library.TrackGenreEntity

// AURA database.
// Version 3 introduces the library relationship schema:
// artists, albums, genres, and their junction tables.
@Database(
    entities = [
        AppMetadataEntity::class,
        ScanRevisionEntity::class,
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        GenreEntity::class,
        TrackArtistEntity::class,
        AlbumArtistEntity::class,
        TrackGenreEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class AuraDatabase : RoomDatabase() {

    abstract fun scanRevisionDao(): ScanRevisionDao

    abstract fun trackDao(): TrackDao

    abstract fun artistDao(): ArtistDao

    abstract fun albumDao(): AlbumDao

    abstract fun genreDao(): GenreDao
}