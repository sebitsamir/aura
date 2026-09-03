package com.aura.core.model

// Domain model representing a music genre.
data class Genre(
    val genreUuid: String,
    val name: String,
    val trackCount: Int,
)