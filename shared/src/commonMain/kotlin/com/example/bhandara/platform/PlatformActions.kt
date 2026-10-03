package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Things screens ask the phone to do with another app: call, get directions, share, open a web page.
 * Implemented on each platform ([rememberPlatformActions]); shared screens get it from [LocalPlatformActions].
 */
interface PlatformActions {

    /** Opens the dialer with the number filled in (the user still taps call). Ignores non-numbers. */
    fun dial(phoneNumber: String)

    /** Opens the Google Maps app (or the website if it isn't installed) with directions to the spot */
    fun openDirections(latitude: Double, longitude: Double)

    /** Opens the system share sheet with this text */
    fun share(text: String)

    /** Opens a web page in the browser */
    fun openUrl(url: String)
}

/** The current platform's [PlatformActions]; provided at the root of the app's UI */
val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> {
    error("PlatformActions not provided: wrap the UI in ProvideAppEnvironment")
}

/** This platform's implementation, tied to the current screen */
@Composable
expect fun rememberPlatformActions(): PlatformActions

/** The links [PlatformActions] hands to other apps; kept here so both platforms build them the same way */
object ExternalLinks {

    /** Google Maps directions to an exact spot; opens the app when installed, the website otherwise */
    fun directions(latitude: Double, longitude: Double): String =
        "https://www.google.com/maps/dir/?api=1&destination=${coordinate(latitude)},${coordinate(longitude)}"

    /** A tel: link with only the digits (and a leading +), or null if it isn't a phone number */
    fun phoneCall(phoneNumber: String): String? {
        val trimmed = phoneNumber.trim()
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length < 3) return null
        return "tel:" + (if (trimmed.startsWith("+")) "+" else "") + digits
    }

    // Always a dot as the decimal separator, whatever the phone's language
    private fun coordinate(value: Double): String = value.toString()
}
