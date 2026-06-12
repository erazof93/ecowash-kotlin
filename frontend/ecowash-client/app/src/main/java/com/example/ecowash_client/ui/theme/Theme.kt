package com.example.ecowash_client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = EcoGreenDark,
    onPrimaryContainer = EcoGreenLight,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = EcoGreenCard,
    onSecondaryContainer = EcoGreenOnBg,
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC4D8CA),
    outline = DarkOutline,
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = EcoGreenLight,
    onPrimaryContainer = Color(0xFF002114),
    secondary = LightSecondary,
    onSecondary = LightOnPrimary,
    secondaryContainer = Color(0xFFB8F5CC),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = LightTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFDDE5DA),
    onSurfaceVariant = Color(0xFF414941),
    outline = Color(0xFFB8CCBB),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun EcowashclientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
