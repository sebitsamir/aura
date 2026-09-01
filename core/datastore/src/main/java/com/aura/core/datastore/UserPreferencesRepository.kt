package com.aura.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.core.model.PlaybackSnapshot
import com.aura.core.model.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private companion object {
        val IS_DARK_MODE_ENABLED = booleanPreferencesKey("is_dark_mode_enabled")
    }

    val isDarkModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_ENABLED] ?: true
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_ENABLED] = enabled
        }
    }
}

@Singleton
class PlaybackRecoveryStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private companion object {
        val SONG_IDS = stringPreferencesKey("playback_song_ids")
        val CURRENT_INDEX = intPreferencesKey("playback_current_index")
        val POSITION_MS = longPreferencesKey("playback_position_ms")
        val QUEUE_REVISION = intPreferencesKey("playback_queue_revision")
        val REPEAT_MODE = stringPreferencesKey("playback_repeat_mode")
        val SHUFFLE_ENABLED = booleanPreferencesKey("playback_shuffle_enabled")
        val WAS_PLAYING = booleanPreferencesKey("playback_was_playing")
        val TIMESTAMP_MS = longPreferencesKey("playback_timestamp_ms")
    }

    suspend fun save(snapshot: PlaybackSnapshot) {
        dataStore.edit { preferences ->
            preferences[SONG_IDS] = snapshot.songIds.joinToString(",")
            preferences[CURRENT_INDEX] = snapshot.currentIndex
            preferences[POSITION_MS] = snapshot.positionMs
            preferences[QUEUE_REVISION] = snapshot.queueRevision
            preferences[REPEAT_MODE] = snapshot.repeatMode.name
            preferences[SHUFFLE_ENABLED] = snapshot.shuffleEnabled
            preferences[WAS_PLAYING] = snapshot.wasPlaying
            preferences[TIMESTAMP_MS] = snapshot.timestampMs
        }
    }

    suspend fun load(): PlaybackSnapshot? {
        val preferences = dataStore.data.first()
        val songIdsRaw = preferences[SONG_IDS] ?: return null
        if (songIdsRaw.isBlank()) return null

        return PlaybackSnapshot(
            songIds = songIdsRaw.split(",").mapNotNull { it.toLongOrNull() },
            currentIndex = preferences[CURRENT_INDEX] ?: 0,
            positionMs = preferences[POSITION_MS] ?: 0L,
            queueRevision = preferences[QUEUE_REVISION] ?: 0,
            repeatMode = preferences[REPEAT_MODE]?.let { runCatching { RepeatMode.valueOf(it) }.getOrNull() }
                ?: RepeatMode.OFF,
            shuffleEnabled = preferences[SHUFFLE_ENABLED] ?: false,
            wasPlaying = preferences[WAS_PLAYING] ?: false,
            timestampMs = preferences[TIMESTAMP_MS] ?: 0L,
        )
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(SONG_IDS)
            preferences.remove(CURRENT_INDEX)
            preferences.remove(POSITION_MS)
            preferences.remove(QUEUE_REVISION)
            preferences.remove(REPEAT_MODE)
            preferences.remove(SHUFFLE_ENABLED)
            preferences.remove(WAS_PLAYING)
            preferences.remove(TIMESTAMP_MS)
        }
    }
}
