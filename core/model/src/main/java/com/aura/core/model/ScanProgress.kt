package com.aura.core.model

// Represents the current progress of a library scan.
// Emitted by the scanner and observed by the UI.
data class ScanProgress(
    val phase: ScanPhase,
    val current: Int,
    val total: Int,
    val message: String,
)

// The distinct phases of a library scan.
enum class ScanPhase {
    ENUMERATING,
    SCANNING,
    DERIVING_RELATIONSHIPS,
    MARKING_UNAVAILABLE,
    COMPLETING,
}