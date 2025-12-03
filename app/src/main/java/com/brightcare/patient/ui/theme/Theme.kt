package com.brightcare.patient.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,
    secondary = Orange500,
    onSecondary = White,
    secondaryContainer = Orange50,
    onSecondaryContainer = Orange600,
    tertiary = Gray600,
    onTertiary = White,
    background = WhiteBg,
    onBackground = Gray900,
    surface = WhiteBg,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    error = Error,
    onError = White
)

@Composable
fun BrightCarePatientTheme(
    content: @Composable () -> Unit
) {
    // Always use light theme - no dark theme support
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
