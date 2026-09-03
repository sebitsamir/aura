package com.aura.core.scanner

// Result of a library scan attempt.
sealed interface ScanResult {

    // The scan completed and the database was reconciled.
    data class Success(
        val revisionId: Long,
        val scannedCount: Int,
        val addedCount: Int,
        val updatedCount: Int,
        val unavailableCount: Int,
    ) : ScanResult

    // The scan could not run because audio permission was denied.
    data object PermissionDenied : ScanResult

    // The scan failed for a non-permission reason.
    data class Failed(
        val message: String,
    ) : ScanResult
}