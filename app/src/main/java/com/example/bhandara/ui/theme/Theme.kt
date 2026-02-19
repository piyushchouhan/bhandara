package com.example.bhandara.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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
            typography = Typography,
            content = content
        )
    }
}