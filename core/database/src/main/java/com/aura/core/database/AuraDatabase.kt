package com.aura.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.core.database.library.ScanRevisionDao
import com.aura.core.database.library.ScanRevisionEntity
import com.aura.core.database.library.TrackDao
import com.aura.core.database.library.TrackEntity

// AURA database.
// Version 2 introduces the library identity schema.
// The auto-migration from 1 to 2 adds the scan_revisions and track_sources tables
// without touching existing app_metadata data.
@Database(
    entities = [
        AppMetadataEntity::class,
        ScanRevisionEntity::class,
        TrackEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class AuraDatabase : RoomDatabase() {

    abstract fun scanRevisionDao(): ScanRevisionDao

    abstract fun trackDao(): TrackDao
}