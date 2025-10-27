package com.example.leo.ui.theme

// -----------------------------
// Compose Material 3
// -----------------------------
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.leo.data.ThemeMode

// Keep palettes conservative; refine later if you want custom colors.
private val LightColors = lightColorScheme()

private val DarkColors = darkColorScheme()

// True black for AMOLED: black surfaces + white text.
// We leave accents to defaults so the app still looks Material.
private val BlackColors = darkColorScheme(
    background     = Color(0xFF000000),
    surface        = Color(0xFF000000),
    surfaceVariant = Color(0xFF121212),
    onBackground   = Color(0xFFFFFFFF),
    onSurface      = Color(0xFFFFFFFF)
)

@Composable
fun AppTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit
) {
    val scheme = when (mode) {
        ThemeMode.Light -> LightColors
        ThemeMode.Dark  -> DarkColors
        ThemeMode.Black -> BlackColors
    }
    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}