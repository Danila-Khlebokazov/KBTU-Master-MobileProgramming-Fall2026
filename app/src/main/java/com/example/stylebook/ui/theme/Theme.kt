package com.example.stylebook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = EditorialRed,
    onSecondary = Paper,
    tertiary = EditorialRed,
    background = Paper,
    onBackground = Ink,
    surface = PaperSurface,
    onSurface = Ink,
    surfaceVariant = Hairline,
    onSurfaceVariant = Smoke,
    outline = Hairline,
    error = EditorialRed
)

private val DarkColors = darkColorScheme(
    primary = InkDark,
    onPrimary = PaperDark,
    secondary = EditorialRed,
    onSecondary = Paper,
    tertiary = EditorialRed,
    background = PaperDark,
    onBackground = InkDark,
    surface = PaperSurfaceDark,
    onSurface = InkDark,
    surfaceVariant = HairlineDark,
    onSurfaceVariant = SmokeDark,
    outline = HairlineDark,
    error = EditorialRed
)

@Composable
fun StyleBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
