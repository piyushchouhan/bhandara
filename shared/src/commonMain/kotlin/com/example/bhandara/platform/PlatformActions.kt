package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Things screens ask the phone to do with another app: call, get directions, share, open a web page, save a photo.
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

    /** Opens this app's page in the phone's Settings (e.g. to turn location back on) */
    fun openAppSettings()

    /**
     * Downloads the photo at [url] into the phone's photo gallery. False when it couldn't be downloaded or saved
     * (e.g. no connection, or on iOS the user didn't allow adding photos).
     */
    suspend fun savePhoto(url: String): Boolean

    /** Opens the system share sheet with a file made from [content] (e.g. the user's data as JSON) */
    fun shareFile(fileName: String, mimeType: String, content: String)
}

/** Settings the platform actions follow; kept up to date by ProvideAppPreferences */
object PlatformPreferences {
    var directionsApp: com.example.bhandara.data.settings.DirectionsApp = com.example.bhandara.data.settings.DirectionsApp.GOOGLE_MAPS
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

    /** Apple Maps directions to an exact spot (iPhone's own maps app) */
    fun appleMapsDirections(latitude: Double, longitude: Double): String =
        "https://maps.apple.com/?daddr=${coordinate(latitude)},${coordinate(longitude)}"

    /** A geo: link, which Android offers to every maps app installed */
    fun geo(latitude: Double, longitude: Double): String =
        "geo:${coordinate(latitude)},${coordinate(longitude)}?q=${coordinate(latitude)},${coordinate(longitude)}"

    /** The privacy policy, a public page on the backend (also the one given to the app stores) */
    fun privacyPolicy(apiBaseUrl: String): String = apiBaseUrl.trimEnd('/') + "/privacy"

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
