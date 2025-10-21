package com.example.leo.ui

// -----------------------------
// Compose Foundation
// -----------------------------
import androidx.compose.foundation.layout.padding

// -----------------------------
// Compose Material 3
// -----------------------------
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

// -----------------------------
// Compose Runtime / UI
// -----------------------------
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

// -----------------------------
// Navigation-Compose
// -----------------------------
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// -----------------------------
// Icons
// -----------------------------
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Settings

// -----------------------------
// Local screens / routes
// -----------------------------
import com.example.leo.ui.navigation.Screen
import com.example.leo.ui.navigation.allScreens

// -----------------------------
// Theme + Store
// -----------------------------
import com.example.leo.ui.theme.LittleGeniusTheme
import com.example.leo.data.ThemeStore
import kotlinx.coroutines.launch

@Composable
fun LittleGeniusApp() {
    val navController = rememberNavController()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe persisted dark mode
    val isDark by ThemeStore.darkModeFlow(ctx).collectAsState(initial = false)

    LittleGeniusTheme(darkTheme = isDark) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    allScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        val icon = when (screen) {
                            Screen.Chat     -> Icons.AutoMirrored.Filled.Chat
                            Screen.Settings -> Icons.Filled.Settings
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Chat.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Chat.route) {
                    ChatScreen(
                        onOpenSettings = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDark = isDark,
                        onToggleDark = { enabled ->
                            scope.launch { ThemeStore.setDarkMode(ctx, enabled) }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
