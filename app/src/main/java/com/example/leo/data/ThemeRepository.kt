package com.example.leo.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Tiny persistence wrapper for app theme.
 * Uses SharedPreferences to avoid adding Gradle deps.
 */
class ThemeRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDarkMode(): Boolean =
        prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "leo_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
