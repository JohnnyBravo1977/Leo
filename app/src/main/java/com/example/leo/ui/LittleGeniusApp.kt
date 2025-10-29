package com.example.leo.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.leo.ui.chat.ChatScreen
import com.example.leo.ui.settings.SettingsScreen

object Routes {
    const val Chat = "chat"
    const val Settings = "settings"
}

@Composable
fun LittleGeniusApp() {
    val nav = rememberNavController()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.Chat,
            modifier = Modifier.padding(inner)
        ) {
            composable(Routes.Chat) {
                ChatScreen(
                    modifier = Modifier.fillMaxSize(),
                    onOpenSettings = { nav.navigate(Routes.Settings) }
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
