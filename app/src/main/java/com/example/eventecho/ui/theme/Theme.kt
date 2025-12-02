package com.example.eventecho.ui.theme

import android.app.Activity
import android.hardware.lights.Light
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,

    secondary = DarkSurface,
    onSecondary = Color.White,

    tertiary = DarkBackground,
    onTertiary = Color.White,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,

    surfaceVariant = DarkFilterBg,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkSurface,

    primaryContainer = OrangeAccent.copy(alpha = 0.22f),
    onPrimaryContainer = OrangeAccent,

    secondaryContainer = DarkPrimary,
    onSecondaryContainer = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,

    secondary = LightBackground,
    onSecondary = Color.Black,

    tertiary = LightSearchBackground,
    onTertiary = LightSearchText,

    background = LightBackground,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,

    surfaceVariant = LightSearchBackground,
    onSurfaceVariant = LightSearchText,

    outline = DarkTextSecondary,

    primaryContainer = OrangeAccent.copy(alpha = 0.15f),
    onPrimaryContainer = OrangeAccent,

    secondaryContainer = LightSearchBackground,
    onSecondaryContainer = Color.Black
)
@Composable
fun EventEchoTheme(
    darkTheme: Boolean = false, // Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Dynamic color turned off for custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    val window = (view.context as Activity).window
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}