package com.example.leo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * FocusPrefs
 * - Holds the LaserFocus flag in AndroidX DataStore.
 * - Kept in the existing data layer alongside ThemePrefs.
 *
 * No app wiring is required to land this file; UI can read/write directly.
 */
private val Context.focusDataStore by preferencesDataStore(name = "focus_prefs")

class FocusPrefs(private val context: Context) {
    private val KEY_LASER = booleanPreferencesKey("laserFocus")

    /** Current LaserFocus state as a Flow. Defaults to false. */
    val laserFlow: Flow<Boolean> = context.focusDataStore.data.map { prefs ->
        prefs[KEY_LASER] ?: false
    }

    /** Persist a new state. */
    suspend fun setLaser(enabled: Boolean) {
        context.focusDataStore.edit { prefs ->
            prefs[KEY_LASER] = enabled
        }
    }
}
