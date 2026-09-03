package com.aura.core.model

// Domain model representing a music album.
data class Album(
    val albumUuid: String,
    val name: String,
    val mediaStoreAlbumId: Long,
    val artistName: String,
    val year: Int?,
    val trackCount: Int,
)