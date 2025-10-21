package com.example.leo.ui.theme

// -----------------------------
// Material3
// -----------------------------
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// If you already have custom colors/typography/shapes, keep them.
// Just switch between light/dark using the flag.
private val LightColors = lightColorScheme()
private val DarkColors  = darkColorScheme()

@Composable
fun LittleGeniusTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}