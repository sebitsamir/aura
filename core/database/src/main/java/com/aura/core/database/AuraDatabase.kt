package com.aura.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppMetadataEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AuraDatabase : RoomDatabase()
