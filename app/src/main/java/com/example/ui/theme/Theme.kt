package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightNavySmokeColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = Color.White,
    primaryContainer = NavyContainer,
    onPrimaryContainer = Color.White,
    secondary = Navy600,
    onSecondary = Color.White,
    secondaryContainer = SmokeWhiteMuted,
    onSecondaryContainer = Navy900,
    tertiary = AbsentRed,
    onTertiary = Color.White,
    background = SmokeWhite,
    onBackground = Navy900,
    surface = SmokeWhiteLight,
    onSurface = Navy900,
    surfaceVariant = SmokeWhiteMuted,
    onSurfaceVariant = Color(0xFF334155),
    outline = SmokeWhiteBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

private val DarkNavySmokeColorScheme = darkColorScheme(
    primary = Navy500,
    onPrimary = Color.White,
    primaryContainer = NavySurface,
    onPrimaryContainer = Color.White,
    secondary = Navy600,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFFF87171),
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = SmokeWhite,
    surface = DarkSurface,
    onSurface = SmokeWhite,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun AbsenteesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkNavySmokeColorScheme else LightNavySmokeColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
