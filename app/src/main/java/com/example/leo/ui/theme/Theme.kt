package com.example.leo.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Keep this file as the palette + shapes holder.
// The single composable theme entry point now lives in AppTheme.kt as `LeoTheme`.

val Shapes = Shapes()

// Dark palette
val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Light palette
val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
