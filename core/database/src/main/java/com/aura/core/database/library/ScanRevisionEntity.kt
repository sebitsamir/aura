package com.aura.core.database.library

import androidx.room.Entity
import androidx.room.PrimaryKey

// Scan revision statuses used by the library scanner.
// A revision represents one complete scan attempt with final reconciliation counts.
object ScanStatus {
    const val RUNNING = 0
    const val COMPLETED = 1
    const val CANCELLED = 2
    const val FAILED = 3
}

// A single scan attempt record.
// Each full or incremental scan creates one revision row.
// Phase 2.1 uses full scans. Phase 2.3 will add incremental delta scans.
@Entity(tableName = "scan_revisions")
data class ScanRevisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long?,
    val status: Int,
    val scannedCount: Int,
    val addedCount: Int,
    val updatedCount: Int,
    val unavailableCount: Int,
)