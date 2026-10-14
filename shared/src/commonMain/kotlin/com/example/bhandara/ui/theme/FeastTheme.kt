package com.example.bhandara.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.outfit_bold
import com.example.bhandara.shared.resources.outfit_medium
import com.example.bhandara.shared.resources.outfit_regular
import com.example.bhandara.shared.resources.outfit_semibold
import org.jetbrains.compose.resources.Font

/**
 * The redesign's colours (the "5d" system from Claude Design): deep indigo and saffron over a warm map, with a
 * dark version (option 1b) that keeps saffron as the one bright accent. Screens move to it as they're redesigned;
 * the others still use [BhandaraTheme]'s colours.
 */
@Immutable
data class FeastColors(
    val isDark: Boolean,
    val mapLand: Color,
    val mapWater: Color,
    val mapPark: Color,
    val mapRoad: Color,
    /** Cards, sheets and the side menu */
    val surface: Color,
    /** Behind the open side menu */
    val scrim: Color,
    /** Filled buttons, the menu button, shop pins */
    val indigo: Color,
    val onIndigo: Color,
    /** Text and icons on [surface] */
    val ink: Color,
    val muted: Color,
    /** Soft fills: the second button, the language row */
    val lavender: Color,
    val border: Color,
    /** Icons that lead a row */
    val accent: Color,
    val saffron: Color,
    /** Dark ink on saffron, the same in both themes */
    val onSaffron: Color,
    val saffronSoft: Color,
    val saffronText: Color,
    /** A switch's track when off, on a [saffronSoft] row */
    val saffronTrack: Color,
    /** Light text on indigo, e.g. the version line in the side menu */
    val headerSubtext: Color,
    /** Behind full pages (shop details, reviews) */
    val page: Color,
    /** Longer text, e.g. a description */
    val body: Color,
    /** Very soft lavender, e.g. "See full menu" */
    val lavenderSoft: Color,
    /** Open, veg, available */
    val success: Color,
    val successSoft: Color,
    /** Second line on a [successSoft] card */
    val successText: Color,
    /** Destructive actions */
    val danger: Color,
    val dangerSoft: Color,
    /** "Is this your shop?": dashed saffron card */
    val claimBorder: Color,
    val claimFill: Color,
    /** Unlit stars on a card, and on the indigo rating summary */
    val starOff: Color,
    val starOffOnIndigo: Color,
    /** Empty part of the rating bars on the indigo summary; steps not reached yet on an indigo card */
    val barTrackOnIndigo: Color,
    /** Hint text in an empty field */
    val placeholder: Color,
    /** A button that can't be used yet: [border] fill with this text */
    val disabledText: Color,
    /** The picked option's outline and radio, e.g. a report reason */
    val selectedOutline: Color,
    /** Behind a bottom sheet */
    val sheetScrim: Color,
    /** Behind an open ⋮ menu */
    val menuScrim: Color,
    /** The striped saffron tile shown where a feast has no photo */
    val feastStripeA: Color,
    val feastStripeB: Color,
    /** A taken-down feast's page and its greyed stripes */
    val fadedPage: Color,
    val fadedStripeA: Color,
    val fadedStripeB: Color,
    /** Text on a [dangerSoft] box */
    val dangerText: Color,
    /** An icon tile on an indigo card */
    val tileOnIndigo: Color,
    /** Longer text on an indigo card */
    val bodyOnIndigo: Color,
    /** Longer text on a [saffronSoft] card */
    val saffronBody: Color,
    /** The heart in "Crafted with ♥" */
    val heart: Color,
    /** Behind a profile's first letter when there's no photo */
    val avatarFill: Color,
    /** The outline of the "Danger zone" card */
    val dangerBorder: Color,
    /** A red button that can't be used yet (e.g. Delete before the name is typed) */
    val dangerDisabled: Color,
    /** The icon tile on a picked indigo card (e.g. "Shop or stall" when chosen) */
    val tileOnSelected: Color,
) {
    companion object {
        val Light = FeastColors(
            isDark = false,
            mapLand = Color(0xFFF6EEDF),
            mapWater = Color(0xFFCFE3EA),
            mapPark = Color(0xFFDDEBCF),
            mapRoad = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            scrim = Color(0xFF1C1916).copy(alpha = 0.45f),
            indigo = Color(0xFF1E1B4B),
            onIndigo = Color(0xFFFFFFFF),
            ink = Color(0xFF1E1B4B),
            muted = Color(0xFF6B6890),
            lavender = Color(0xFFEEEDFB),
            border = Color(0xFFE3E1F0),
            accent = Color(0xFF4F46E5),
            saffron = Color(0xFFF59E0B),
            onSaffron = Color(0xFF1E1B4B),
            saffronSoft = Color(0xFFFFF4E0),
            saffronText = Color(0xFFB45309),
            saffronTrack = Color(0xFFF2DDB6),
            headerSubtext = Color(0xFFC7C4F0),
            page = Color(0xFFFAF7F1),
            body = Color(0xFF4A4770),
            lavenderSoft = Color(0xFFF4F3FD),
            success = Color(0xFF15803D),
            successSoft = Color(0xFFDCF2E2),
            successText = Color(0xFF3F6B4E),
            danger = Color(0xFFB91C1C),
            dangerSoft = Color(0xFFFEF2F2),
            claimBorder = Color(0xFFF2C77A),
            claimFill = Color(0xFFFFF9EE),
            starOff = Color(0xFFC7C4F0),
            starOffOnIndigo = Color(0xFF6B68A8),
            barTrackOnIndigo = Color(0xFF2E2A6B),
            placeholder = Color(0xFF8A87A8),
            disabledText = Color(0xFF9C99B8),
            selectedOutline = Color(0xFF1E1B4B),
            sheetScrim = Color(0xFF1E1B4B).copy(alpha = 0.5f),
            menuScrim = Color(0xFF1E1B4B).copy(alpha = 0.28f),
            feastStripeA = Color(0xFFF7D9A0),
            feastStripeB = Color(0xFFF3CD86),
            fadedPage = Color(0xFFF3F1EC),
            fadedStripeA = Color(0xFFE4E0D8),
            fadedStripeB = Color(0xFFDCD7CD),
            dangerText = Color(0xFF7F1D1D),
            tileOnIndigo = Color(0xFF2E2A6B),
            bodyOnIndigo = Color(0xFFE0DEF8),
            saffronBody = Color(0xFF6B5A3E),
            heart = Color(0xFFE0336E),
            avatarFill = Color(0xFFBFD7F5),
            dangerBorder = Color(0xFFFECACA),
            dangerDisabled = Color(0xFFF3D4D4),
            tileOnSelected = Color(0xFF2E2A6B),
        )

        val Dark = FeastColors(
            isDark = true,
            mapLand = Color(0xFF17152B),
            mapWater = Color(0xFF1D3445),
            mapPark = Color(0xFF1C2F25),
            mapRoad = Color(0xFF2C2948),
            surface = Color(0xFF1E1C33),
            scrim = Color(0xFF07060E).copy(alpha = 0.7f),
            indigo = Color(0xFF4740A8),
            onIndigo = Color(0xFFFFFFFF),
            ink = Color(0xFFF1EFFF),
            muted = Color(0xFFA9A6CF),
            lavender = Color(0xFF2A2750),
            border = Color(0xFF3A3758),
            accent = Color(0xFFA5A0FF),
            saffron = Color(0xFFF59E0B),
            onSaffron = Color(0xFF1E1B4B),
            saffronSoft = Color(0xFF33260F),
            saffronText = Color(0xFFFBBF5A),
            saffronTrack = Color(0xFF4A3A20),
            headerSubtext = Color(0xFFC7C4F0),
            page = Color(0xFF17152B),
            body = Color(0xFFD4D1EE),
            lavenderSoft = Color(0xFF23213F),
            success = Color(0xFF4ADE80),
            successSoft = Color(0xFF15301F),
            successText = Color(0xFF8FC7A0),
            danger = Color(0xFFF87171),
            dangerSoft = Color(0xFF3A1A20),
            claimBorder = Color(0xFF6B4E1C),
            claimFill = Color(0xFF241C0E),
            starOff = Color(0xFF4A4680),
            starOffOnIndigo = Color(0xFF7A74D0),
            barTrackOnIndigo = Color(0xFF5B54C0),
            placeholder = Color(0xFF8582AD),
            disabledText = Color(0xFF6E6B92),
            selectedOutline = Color(0xFFA5A0FF),
            sheetScrim = Color(0xFF07060E).copy(alpha = 0.65f),
            menuScrim = Color(0xFF07060E).copy(alpha = 0.5f),
            feastStripeA = Color(0xFF3D2E14),
            feastStripeB = Color(0xFF33260F),
            fadedPage = Color(0xFF17152B),
            fadedStripeA = Color(0xFF2A2840),
            fadedStripeB = Color(0xFF24223A),
            dangerText = Color(0xFFFCA5A5),
            tileOnIndigo = Color(0xFF2A2750),
            bodyOnIndigo = Color(0xFFE4E2FA),
            saffronBody = Color(0xFFE8D3AE),
            heart = Color(0xFFF472A0),
            avatarFill = Color(0xFF2E3A5C),
            dangerBorder = Color(0xFF5C2226),
            dangerDisabled = Color(0xFF5C2226),
            tileOnSelected = Color(0xFF5B54C0),
        )

        fun of(dark: Boolean): FeastColors = if (dark) Dark else Light
    }
}

val LocalFeastColors = staticCompositionLocalOf { FeastColors.Light }

/** The redesign's colours, inside [FeastTheme] */
val MaterialTheme.feastColors: FeastColors
    @Composable
    @ReadOnlyComposable
    get() = LocalFeastColors.current

/** Outfit, the redesign's font */
@Composable
fun outfitFamily(): FontFamily = FontFamily(
    Font(Res.font.outfit_regular, FontWeight.Normal),
    Font(Res.font.outfit_medium, FontWeight.Medium),
    Font(Res.font.outfit_semibold, FontWeight.SemiBold),
    Font(Res.font.outfit_bold, FontWeight.Bold),
)

/**
 * Wraps a redesigned screen: [FeastColors] for its own parts, and Material colours and type mapped from them so
 * Material pieces inside (dialogs, menus, snackbars) match.
 */
@Composable
fun FeastTheme(darkTheme: Boolean = isAppInDarkTheme(), content: @Composable () -> Unit) {
    val colors = FeastColors.of(darkTheme)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.indigo, onPrimary = colors.onIndigo,
            primaryContainer = colors.lavender, onPrimaryContainer = colors.ink,
            secondary = colors.saffron, onSecondary = colors.onSaffron,
            secondaryContainer = colors.saffronSoft, onSecondaryContainer = colors.ink,
            background = colors.mapLand, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink, onSurfaceVariant = colors.muted,
            surfaceContainer = colors.surface, surfaceContainerHigh = colors.surface, surfaceContainerLow = colors.surface,
            outline = colors.border, outlineVariant = colors.border,
            inverseSurface = colors.ink, inverseOnSurface = Color(0xFF1E1B4B),
            error = colors.danger, errorContainer = colors.dangerSoft,
        )
    } else {
        lightColorScheme(
            primary = colors.indigo, onPrimary = colors.onIndigo,
            primaryContainer = colors.lavender, onPrimaryContainer = colors.ink,
            secondary = colors.saffron, onSecondary = colors.onSaffron,
            secondaryContainer = colors.saffronSoft, onSecondaryContainer = colors.ink,
            background = colors.mapLand, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink, onSurfaceVariant = colors.muted,
            surfaceContainer = colors.surface, surfaceContainerHigh = colors.surface, surfaceContainerLow = colors.surface,
            outline = colors.border, outlineVariant = colors.border,
            inverseSurface = colors.ink, inverseOnSurface = Color.White,
            error = colors.danger, errorContainer = colors.dangerSoft,
        )
    }
    CompositionLocalProvider(LocalFeastColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = Typography().withFont(outfitFamily())) {
            DesignScale(content)
        }
    }
}

/** The width the redesign's screens are drawn at */
private const val DESIGN_WIDTH_DP = 360f

/** True inside a [DesignScale], so a FeastTheme inside another doesn't scale twice */
private val LocalDesignScaled = staticCompositionLocalOf { false }

/**
 * The phone's own density, before [DesignScale]; null outside one. For native views sized in the platform's units
 * (e.g. the iOS map), which must not be scaled again.
 */
val LocalUnscaledDensity = staticCompositionLocalOf<Density?> { null }

/**
 * Scales a redesigned screen so the design's 360dp width fills the phone's width: the screen then looks as in the
 * design, in the same proportions, on a wider phone too (most are 390–430dp). Text keeps the phone's font size and
 * the app's text size setting ([LocalTextScale]) on top. Within limits, so a tablet doesn't get giant.
 */
@Composable
private fun DesignScale(content: @Composable () -> Unit) {
    if (LocalDesignScaled.current) {
        content()
        return
    }
    BoxWithConstraints(propagateMinConstraints = true) {
        val density = LocalDensity.current
        val scale = if (maxWidth.value.isFinite()) (maxWidth.value / DESIGN_WIDTH_DP).coerceIn(0.9f, 1.25f) else 1f
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * scale, density.fontScale * LocalTextScale.current),
            LocalUnscaledDensity provides density,
            LocalDesignScaled provides true,
            content = content,
        )
    }
}

private fun Typography.withFont(family: FontFamily): Typography {
    // Line height as the design's (the browser's "normal" for Outfit), whatever the text size, not Material's
    // fixed heights, which made rows taller than designed
    fun TextStyle.f() = copy(fontFamily = family, lineHeight = 1.26.em)
    return copy(
        displayLarge = displayLarge.f(), displayMedium = displayMedium.f(), displaySmall = displaySmall.f(),
        headlineLarge = headlineLarge.f(), headlineMedium = headlineMedium.f(), headlineSmall = headlineSmall.f(),
        titleLarge = titleLarge.f(), titleMedium = titleMedium.f(), titleSmall = titleSmall.f(),
        bodyLarge = bodyLarge.f(), bodyMedium = bodyMedium.f(), bodySmall = bodySmall.f(),
        labelLarge = labelLarge.f(), labelMedium = labelMedium.f(), labelSmall = labelSmall.f(),
    )
}
