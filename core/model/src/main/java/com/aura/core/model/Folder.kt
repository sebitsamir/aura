package com.aura.core.model

// Domain model representing a music folder.
data class Folder(
    val folderUuid: String,
    val path: String,
    val name: String,
    val parentPath: String?,
    val trackCount: Int,
)