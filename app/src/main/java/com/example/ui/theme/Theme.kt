package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SolarCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = Color(0xFF80F2FF),

    secondary = SolarGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3E2D00),
    onSecondaryContainer = Color(0xFFFFE082),

    tertiary = SolarGreen,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF003919),
    onTertiaryContainer = Color(0xFFB9F6CA),

    background = SolarBgDark,
    onBackground = TextPrimary,

    surface = SolarCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SolarCardElevated,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0A0E),
    surfaceContainer = Color(0xFF101014),
    surfaceContainerHigh = Color(0xFF16161C),
    surfaceContainerHighest = Color(0xFF1E1E26),

    surfaceTint = Color.Transparent, // PREVENT muddy M3 tonal elevation tinting
    outline = SolarBorder,
    outlineVariant = Color(0xFF1C1C24),

    error = SolarRed,
    onError = Color.White,
    errorContainer = Color(0xFF4D0014),
    onErrorContainer = Color(0xFFFFB4AB)
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
