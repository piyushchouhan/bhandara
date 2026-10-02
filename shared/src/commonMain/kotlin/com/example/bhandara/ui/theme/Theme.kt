package com.example.bhandara.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Custom colors that don't exist in Material 3's ColorScheme ---

data class BhandaraColors(
    val foodShopPrimary: Color,
    val foodShopPrimaryContainer: Color
)

val LocalBhandaraColors = compositionLocalOf {
    BhandaraColors(
        foodShopPrimary = Magenta40,
        foodShopPrimaryContainer = Magenta20
    )
}

// Extension on MaterialTheme for convenient access
val MaterialTheme.bhandaraColors: BhandaraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBhandaraColors.current

// --- Standard Material 3 color schemes (no custom params) ---

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Black,
    surface = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = Color.White
)

@Composable
fun BhandaraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val bhandaraColors = if (darkTheme) {
        BhandaraColors(
            foodShopPrimary = Magenta40,
            foodShopPrimaryContainer = Magenta20
        )
    } else {
        BhandaraColors(
            foodShopPrimary = Magenta80,
            foodShopPrimaryContainer = Magenta20
        )
    }

    CompositionLocalProvider(LocalBhandaraColors provides bhandaraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = bhandaraTypography(),
            content = content
        )
    }
}