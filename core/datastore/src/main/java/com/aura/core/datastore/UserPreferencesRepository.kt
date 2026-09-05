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
import com.aura.core.model.AuraAppearance
import com.aura.core.model.PlaybackSnapshot
import com.aura.core.model.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aura_preferences",
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private companion object {
        val APPEARANCE = stringPreferencesKey("appearance")

        /**
         * Kept only so installs created before the multi-appearance system
         * can be migrated without silently losing the user's preference.
         */
        val LEGACY_IS_DARK_MODE_ENABLED =
            booleanPreferencesKey("is_dark_mode_enabled")
    }

    /**
     * AURA's persisted appearance.
     *
     * Migration rules:
     *
     * 1. New "appearance" preference wins.
     * 2. Old Boolean dark-mode preference is mapped to Obsidian/Ivory.
     * 3. Fresh installs default to Obsidian, AURA's primary appearance.
     */
    val appearance: Flow<AuraAppearance> = dataStore.data.map { preferences ->
        val storedAppearance = preferences[APPEARANCE]

        if (storedAppearance != null) {
            AuraAppearance.fromStoredValue(storedAppearance)
        } else {
            when (preferences[LEGACY_IS_DARK_MODE_ENABLED]) {
                true -> AuraAppearance.OBSIDIAN
                false -> AuraAppearance.IVORY
                null -> AuraAppearance.OBSIDIAN
            }
        }
    }

    suspend fun setAppearance(appearance: AuraAppearance) {
        dataStore.edit { preferences ->
            preferences[APPEARANCE] = appearance.name

            // Once the new preference is persisted, the legacy Boolean
            // should no longer be authoritative.
            preferences.remove(LEGACY_IS_DARK_MODE_ENABLED)
        }
    }

    /**
     * Compatibility API for any code that still expects the old Boolean.
     *
     * New code should use [appearance].
     */
    @Deprecated(
        message = "Use appearance instead.",
        replaceWith = ReplaceWith("appearance"),
    )
    val isDarkModeEnabled: Flow<Boolean> = appearance.map { current ->
        when (current) {
            AuraAppearance.IVORY -> false
            AuraAppearance.SYSTEM -> true
            AuraAppearance.OBSIDIAN,
            AuraAppearance.AMOLED,
            AuraAppearance.ATMOSPHERE -> true
        }
    }

    /**
     * Compatibility API for callers using the old dark-mode setter.
     */
    @Deprecated(
        message = "Use setAppearance instead.",
        replaceWith = ReplaceWith(
            "setAppearance(if (enabled) AuraAppearance.OBSIDIAN else AuraAppearance.IVORY)",
        ),
    )
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        setAppearance(
            if (enabled) {
                AuraAppearance.OBSIDIAN
            } else {
                AuraAppearance.IVORY
            },
        )
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

        if (songIdsRaw.isBlank()) {
            return null
        }

        val songIds = songIdsRaw
            .split(",")
            .mapNotNull { rawId ->
                rawId.toLongOrNull()
            }

        if (songIds.isEmpty()) {
            return null
        }

        val repeatMode = preferences[REPEAT_MODE]
            ?.let { storedMode ->
                runCatching {
                    RepeatMode.valueOf(storedMode)
                }.getOrNull()
            }
            ?: RepeatMode.OFF

        return PlaybackSnapshot(
            songIds = songIds,
            currentIndex = preferences[CURRENT_INDEX] ?: 0,
            positionMs = preferences[POSITION_MS] ?: 0L,
            queueRevision = preferences[QUEUE_REVISION] ?: 0,
            repeatMode = repeatMode,
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