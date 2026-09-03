# AURA Database

## Version 2

Version 2 introduces the library identity schema.

### Tables Added In Version 2

#### scan_revisions

Stores each scan attempt.

Fields:

- id
- startedAt
- finishedAt
- status
- scannedCount
- addedCount
- updatedCount
- unavailableCount

Status values:

- 0 running
- 1 completed
- 2 cancelled
- 3 failed

#### track_sources

Stores durable track identity records discovered by the scanner.

Primary identity:

- auraUuid

Source locators:

- volumeName
- mediaStoreId
- contentUri

Availability:

- 0 available
- 1 unavailable

Rules:

- The AURA UUID is the durable identity.
- MediaStore ID is a locator.
- Unseen tracks are marked unavailable.
- Unseen tracks are not deleted.
- User-owned overlays must later reference auraUuid, not MediaStore ID.

## Migration Policy

- Destructive migration is forbidden.
- Version 1 to version 2 only adds tables.
- Room schema exports must remain committed.