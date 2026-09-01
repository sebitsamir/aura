package com.aura.core.playback

import com.aura.core.model.Song
import com.aura.core.model.RepeatMode
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setDurationMs(durationMs)
                .build(),
        )
        .build()
}

fun Int.toRepeatMode(): RepeatMode = when (this) {
    androidx.media3.common.Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    androidx.media3.common.Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}

fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
    RepeatMode.OFF -> androidx.media3.common.Player.REPEAT_MODE_OFF
    RepeatMode.ONE -> androidx.media3.common.Player.REPEAT_MODE_ONE
    RepeatMode.ALL -> androidx.media3.common.Player.REPEAT_MODE_ALL
}
