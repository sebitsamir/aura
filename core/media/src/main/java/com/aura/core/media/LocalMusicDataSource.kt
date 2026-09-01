package com.aura.core.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.aura.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMusicDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val songs = mutableListOf<Song>()
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val mediaStoreId = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(collection, mediaStoreId)
                val title = cursor.getString(titleColumn).orEmpty().ifBlank { "Unknown title" }
                val artist = cursor.getString(artistColumn).orEmpty().ifBlank { "Unknown artist" }
                val album = cursor.getString(albumColumn).orEmpty().ifBlank { "Unknown album" }
                val duration = cursor.getLong(durationColumn).coerceAtLeast(0L)
                val albumId = cursor.getLong(albumIdColumn)

                songs += Song(
                    id = mediaStoreId,
                    mediaStoreId = mediaStoreId,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = duration,
                    contentUri = contentUri,
                    albumId = albumId,
                )
            }
        }
        songs
    }

    suspend fun findSongsByIds(ids: List<Long>): List<Song> {
        if (ids.isEmpty()) return emptyList()
        val songsById = loadSongs().associateBy { it.id }
        return ids.mapNotNull { songsById[it] }
    }

    fun albumArtUri(albumId: Long): Uri {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumId,
        )
        return uri
    }
}
