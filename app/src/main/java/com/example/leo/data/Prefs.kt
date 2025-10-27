package com.example.leo.data

// -----------------------------
// DataStore (Preferences)
// -----------------------------
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// -----------------------------
// Theme model
// -----------------------------
enum class ThemeMode { Light, Dark, Black }

// -----------------------------
// DataStore instance
// -----------------------------
private val Context.dataStore by preferencesDataStore(name = "leo_prefs")

object Prefs {
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    fun themeModeFlow(ctx: Context): Flow<ThemeMode> =
        ctx.dataStore.data.map { prefs ->
            when (prefs[KEY_THEME]) {
                ThemeNames.LIGHT -> ThemeMode.Light
                ThemeNames.BLACK -> ThemeMode.Black
                ThemeNames.DARK  -> ThemeMode.Dark
                else             -> ThemeMode.Dark // default
            }
        }

    suspend fun setThemeMode(ctx: Context, mode: ThemeMode) {
        ctx.dataStore.edit { it[KEY_THEME] = when (mode) {
            ThemeMode.Light -> ThemeNames.LIGHT
            ThemeMode.Dark  -> ThemeNames.DARK
            ThemeMode.Black -> ThemeNames.BLACK
        } }
    }

    private object ThemeNames {
        const val LIGHT = "Light"
        const val DARK  = "Dark"
        const val BLACK = "Black"
    }
}
