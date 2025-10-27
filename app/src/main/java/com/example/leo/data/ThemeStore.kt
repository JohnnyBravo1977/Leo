package com.example.leo.data

// -----------------------------
// DataStore (Preferences)
// -----------------------------
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

// -----------------------------
// Kotlin Flow
// -----------------------------
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_NAME = "lg_settings"
private val Context.dataStore by preferencesDataStore(SETTINGS_NAME)

object ThemeStore {
    private val KEY_DARK = booleanPreferencesKey("dark_mode")

    fun darkModeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[KEY_DARK] ?: false }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DARK] = enabled }
    }
}