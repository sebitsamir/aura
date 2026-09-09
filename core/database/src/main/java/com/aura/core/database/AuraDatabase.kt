package com.aura.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aura.core.database.library.AlbumArtistEntity
import com.aura.core.database.library.AlbumDao
import com.aura.core.database.library.AlbumEntity
import com.aura.core.database.library.ArtistDao
import com.aura.core.database.library.ArtistEntity
import com.aura.core.database.library.FolderDao
import com.aura.core.database.library.FolderEntity
import com.aura.core.database.library.GenreDao
import com.aura.core.database.library.GenreEntity
import com.aura.core.database.library.PlaybackEventDao
import com.aura.core.database.library.PlaybackEventEntity
import com.aura.core.database.library.ScanRevisionDao
import com.aura.core.database.library.ScanRevisionEntity
import com.aura.core.database.library.TrackArtistEntity
import com.aura.core.database.library.TrackDao
import com.aura.core.database.library.TrackEntity
import com.aura.core.database.library.TrackGenreEntity
import com.aura.core.database.library.TrackUserOverlayDao
import com.aura.core.database.library.TrackUserOverlayEntity

@Database(
    entities = [
        AppMetadataEntity::class,
        ScanRevisionEntity::class,
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        GenreEntity::class,
        TrackArtistEntity::class,
        AlbumArtistEntity::class,
        TrackGenreEntity::class,
        FolderEntity::class,
        TrackUserOverlayEntity::class,
        PlaybackEventEntity::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun scanRevisionDao(): ScanRevisionDao
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun genreDao(): GenreDao
    abstract fun folderDao(): FolderDao
    abstract fun trackUserOverlayDao(): TrackUserOverlayDao
    abstract fun playbackEventDao(): PlaybackEventDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `track_user_overlays` (
                `trackUuid` TEXT NOT NULL,
                `isFavorite` INTEGER NOT NULL DEFAULT 0,
                `rating` INTEGER,
                `userTags` TEXT,
                `lastModified` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`trackUuid`),
                FOREIGN KEY(`trackUuid`) REFERENCES `track_sources`(`auraUuid`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_track_user_overlays_trackUuid` ON `track_user_overlays` (`trackUuid`)")
        
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `playback_events` (
                `eventId` TEXT NOT NULL,
                `trackUuid` TEXT NOT NULL,
                `mediaStoreId` INTEGER NOT NULL,
                `eventType` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `positionMs` INTEGER NOT NULL,
                `durationMs` INTEGER NOT NULL,
                `sessionId` TEXT NOT NULL,
                `sourceSurface` TEXT,
                PRIMARY KEY(`eventId`)
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_events_trackUuid` ON `playback_events` (`trackUuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_events_eventType` ON `playback_events` (`eventType`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_events_timestamp` ON `playback_events` (`timestamp`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `folders` (
                `folderUuid` TEXT NOT NULL,
                `path` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `parentPath` TEXT,
                `availability` INTEGER NOT NULL,
                `firstSeenRevision` INTEGER NOT NULL,
                `lastSeenRevision` INTEGER NOT NULL,
                PRIMARY KEY(`folderUuid`)
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_folders_path` ON `folders` (`path`)")
        
        db.execSQL("ALTER TABLE `track_sources` ADD COLUMN `folderPath` TEXT NOT NULL DEFAULT ''")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_sources_folderPath` ON `track_sources` (`folderPath`)")
    }
}