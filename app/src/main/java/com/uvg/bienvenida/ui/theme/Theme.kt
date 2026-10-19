package com.uvg.bienvenida.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00668A), onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EEFA), onPrimaryContainer = Color(0xFF123F54),
    secondary = Color(0xFFB90016), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD9), onSecondaryContainer = Color(0xFF410007),
    tertiaryContainer = Color(0xFFD9F5E4), onTertiaryContainer = Color(0xFF14532D),
    background = Color(0xFFF7F9FC), onBackground = Color(0xFF1A2B4C),
    surface = Color.White, onSurface = Color(0xFF1A2B4C),
    surfaceVariant = Color(0xFFE8EEF3), onSurfaceVariant = Color(0xFF485663)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF83D0F5), onPrimary = Color(0xFF003449),
    primaryContainer = Color(0xFF164B63), onPrimaryContainer = Color(0xFFD9EEFA),
    secondary = Color(0xFFFFB3B3), onSecondary = Color(0xFF68000C),
    secondaryContainer = Color(0xFF8E1223), onSecondaryContainer = Color(0xFFFFDAD9),
    tertiaryContainer = Color(0xFF174D32), onTertiaryContainer = Color(0xFFBBF1CD),
    background = Color(0xFF101820), onBackground = Color(0xFFE3EAF2),
    surface = Color(0xFF1A2530), onSurface = Color(0xFFE3EAF2),
    surfaceVariant = Color(0xFF2A3945), onSurfaceVariant = Color(0xFFBCCAD6)
)

@Composable
fun HandsonTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography, content = content)
}