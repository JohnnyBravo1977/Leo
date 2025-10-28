package com.example.leo.ui.theme

// -----------------------------
// Compose Material 3
// -----------------------------
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// 🚫 remove: import com.example.leo.data.ThemeMode

// Keep palettes conservative; refine later if you want custom colors.
private val LightColors = lightColorScheme()

private val DarkColors = darkColorScheme()

// True black for AMOLED: black surfaces + white text.
private val BlackColors = darkColorScheme(
    background     = Color(0xFF000000),
    surface        = Color(0xFF000000),
    surfaceVariant = Color(0xFF121212),
    onBackground   = Color(0xFFFFFFFF),
    onSurface      = Color(0xFFFFFFFF)
)

/**
 * Single-boolean theme API.
 * - darkTheme = true uses DarkColors (or BlackColors if amoledBlack = true)
 * - darkTheme = false uses LightColors
 *
 * File name can stay AppTheme.kt, function is LeoTheme to match MainActivity.
 */
@Composable
fun LeoTheme(
    darkTheme: Boolean = false,
    amoledBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme =
        if (darkTheme) {
            if (amoledBlack) BlackColors else DarkColors
        } else {
            LightColors
        }

    // If you don't have a Typography object defined, delete "typography = Typography,"
    MaterialTheme(
        colorScheme = scheme,
        // typography = Typography,
        content = content
    )
}
