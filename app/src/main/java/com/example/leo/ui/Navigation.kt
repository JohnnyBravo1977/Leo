package com.example.leo.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Chat : Screen("chat", "Chat")
    data object Settings : Screen("settings", "Settings")
}

val allScreens = listOf(Screen.Chat, Screen.Settings)