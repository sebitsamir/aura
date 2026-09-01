package com.aura.core.model

import android.net.Uri

data class Song(
    val id: Long,
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumId: Long = 0L,
)
