package com.healthmate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = HealthTeal,
    onPrimary = Color.White,
    primaryContainer = HealthTealLight,
    onPrimaryContainer = HealthTealDark,
    secondary = WaterBlue,
    error = CaloriesRed,
    background = BackgroundLight,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = HealthTealLight,
    onPrimary = HealthTealDark,
    primaryContainer = HealthTealDark,
    onPrimaryContainer = HealthTealLight,
    secondary = WaterBlue,
    error = CaloriesRed
)

@Composable
fun HealthMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
