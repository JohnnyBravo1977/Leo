package com.example.leo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.leo.data.ThemeRepository
import com.example.leo.ui.chat.ChatScreen   // ✅ proper import
import com.example.leo.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LittleGeniusApp() {
    val nav = rememberNavController()

    // Dark mode persistence
    val ctx = LocalContext.current
    val themeRepo = remember { ThemeRepository(ctx) }
    var isDark by rememberSaveable { mutableStateOf(themeRepo.getDarkMode()) }
    val colors = if (isDark) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colors) {
        Scaffold { innerPadding ->
            NavHost(
                navController = nav,
                startDestination = Routes.Chat,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.Chat) {
                    ChatScreen(   // ✅ now imported cleanly
                        onOpenSettings = { nav.navigate(Routes.Settings) }
                    )
                }
                composable(Routes.Settings) {
                    SettingsScreen(
                        isDark = isDark,
                        onToggleDark = { chooseDark ->
                            isDark = chooseDark
                            themeRepo.setDarkMode(chooseDark)
                        }
                    )
                }
            }
        }
    }
}

private object Routes {
    const val Chat = "chat"
    const val Settings = "settings"
}
