package com.aura.core.model

// Domain model representing a music artist.
data class Artist(
    val artistUuid: String,
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
)