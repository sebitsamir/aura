package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// A unique folder discovered from scanned track paths.
// Folders are identified by a durable AURA UUID.
// The path is the normalized folder path from MediaStore.
@Entity(
    tableName = "folders",
    indices = [
        Index(value = ["path"], unique = true),
    ],
)
data class FolderEntity(
    @PrimaryKey
    val folderUuid: String,
    val path: String,
    val name: String,
    val parentPath: String?,
    val availability: Int,
    val firstSeenRevision: Long,
    val lastSeenRevision: Long,
)