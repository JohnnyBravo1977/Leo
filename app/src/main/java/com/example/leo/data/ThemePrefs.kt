package com.example.leo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private const val STORE_NAME = "app_prefs"
private val KEY_DARK = booleanPreferencesKey("dark_mode")

private val Context.dataStore by preferencesDataStore(name = STORE_NAME)

class ThemePrefs(private val ctx: Context) {
    val darkModeFlow: Flow<Boolean> = ctx.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_DARK] ?: false }

    fun setDarkMode(enabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            ctx.dataStore.edit { prefs ->
                prefs[KEY_DARK] = enabled
            }
        }
    }
}
