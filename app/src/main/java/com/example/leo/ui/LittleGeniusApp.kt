package com.example.leo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.leo.ui.settings.SettingsScreen
import com.example.leo.ui.chat.ChatScreen
import com.example.leo.data.ThemeRepository



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LittleGeniusApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    // Persisted theme using a tiny SharedPreferences-backed repo
    val ctx = LocalContext.current
    val themeRepo = remember { ThemeRepository(ctx) }

    // Initialize from storage; rememberSaveable keeps it through config changes
    var isDark by rememberSaveable { mutableStateOf(themeRepo.getDarkMode()) }

    val messages = remember { mutableStateListOf<String>() }

    val colorScheme = if (isDark) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(titleFor(route)) },
                    navigationIcon = {
                        if (route == Routes.Settings) {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (route != Routes.Settings) {
                            IconButton(onClick = { nav.navigate(Routes.Settings) }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                )
            }
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = Routes.Chat,
                modifier = Modifier.padding(inner)
            ) {
                composable(Routes.Chat) {
                    ChatScreen(
                        messages = messages,
                        onSend = { msg -> messages.add(msg) }
                    )
                }
                composable(Routes.Settings) {
                    SettingsScreen(
                        isDark = isDark,
                        onToggleDark = {
                            isDark = it
                            // Persist immediately
                            themeRepo.setDarkMode(it)
                        }
                    )
                }
            }
        }
    }
}

private fun titleFor(route: String?): String = when (route) {
    Routes.Settings -> "Settings"
    else -> "Little Genius"
}

private object Routes {
    const val Chat = "chat"
    const val Settings = "settings"
}

