package com.aura.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val id: Int = 0,
    val schemaMarker: String = "phase0",
)
