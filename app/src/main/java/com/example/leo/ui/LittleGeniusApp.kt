package com.example.leo.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.leo.ai.ChatClient // kept for future DI
import com.example.leo.ui.chat.ChatScreen
import com.example.leo.ui.settings.SettingsScreen
import com.example.leo.ui.theme.LeoTheme
import com.example.leo.data.ThemePrefs

/**
 * App-level NavHost and theme wrapper.
 * This is intentionally tiny and explicit to avoid drift.
 */
@Composable
fun LittleGeniusApp() {
    // DataStore-backed preferences
    val prefs = ThemePrefs(LocalContext.current)
    val isDark by prefs.darkModeFlow.collectAsState(initial = false)

    // Single NavController for the app
    val nav = rememberNavController()

    LeoTheme(darkTheme = isDark) {
        NavHost(
            navController = nav,
            startDestination = "chat"
        ) {
            composable("chat") {
                ChatScreen(
                    onOpenSettings = { nav.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    isDark = isDark,
                    onToggleDark = { enabled -> prefs.setDarkMode(enabled) },
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

