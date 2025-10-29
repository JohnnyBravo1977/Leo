package com.example.leo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.leo.ui.chat.ChatScreen
import com.example.leo.ui.settings.SettingsScreen
import com.example.leo.ui.theme.LeoTheme
import com.example.leo.data.ThemePrefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = ThemePrefs(applicationContext)

        setContent {
            val nav = rememberNavController()

            // Observe dark mode setting from DataStore
            val isDark by prefs.darkModeFlow.collectAsState(initial = false)

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
                            onToggleDark = { enabled ->
                                prefs.setDarkMode(enabled)
                            }
                        )
                    }
                }
            }
        }
    }
}