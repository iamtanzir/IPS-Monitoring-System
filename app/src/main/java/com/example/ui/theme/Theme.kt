package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SolarCyan,
    secondary = SolarGold,
    tertiary = SolarGreen,
    background = SolarBgDark,
    surface = SolarCardBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = SolarBorder
)

private val LightColorScheme = lightColorScheme(
    primary = SolarCyan,
    secondary = SolarGold,
    tertiary = SolarGreen,
    background = Color(0xFFF0F4F8),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default to match screenshots
    accentColor: Color = SolarCyan,
    content: @Composable () -> Unit
) {
    val currentColorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = accentColor,
            onPrimary = Color.Black
        )
    } else {
        LightColorScheme.copy(
            primary = accentColor,
            onPrimary = Color.White
        )
    }

    MaterialTheme(
        colorScheme = currentColorScheme,
        typography = Typography,
        content = content
    )
}
