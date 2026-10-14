package com.example.bhandara.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.example.bhandara.data.settings.AppPreferences
import com.example.bhandara.data.settings.ThemeMode
import com.example.bhandara.platform.PlatformPreferences

/** The theme the user picked in Settings (the phone's by default) */
val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }

/** The text size the user picked in Settings, on top of the phone's font size */
val LocalTextScale = compositionLocalOf { 1f }

/** Dark or light, as the user picked or as the phone is */
@Composable
fun isAppInDarkTheme(): Boolean = when (LocalThemeMode.current) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** Applies the user's settings to everything inside: theme and text size (and where Directions opens) */
@Composable
fun ProvideAppPreferences(preferences: AppPreferences, content: @Composable () -> Unit) {
    PlatformPreferences.directionsApp = preferences.directionsApp
    CompositionLocalProvider(
        LocalThemeMode provides preferences.theme,
        LocalTextScale provides preferences.textSize.scale,
        content = content,
    )
}
