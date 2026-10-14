package com.example.bhandara.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings

/** Light, dark, or whatever the phone uses */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** How big text is, on top of the phone's own font size */
enum class TextSize(val scale: Float) { SMALL(0.9f), DEFAULT(1f), LARGE(1.15f), LARGEST(1.3f) }

/** Which tab the app opens on */
enum class StartTab { SHOPS, FEASTS }

/** The notifications a user can turn on or off; the backend keeps these, the phone a copy */
enum class NotificationKind { NEARBY_FEASTS, SAVED_FEASTS, REPORT_UPDATES, SHOP_REVIEWS, CLAIM_STATUS, QUIET_HOURS }

/** Where "Directions" opens: Google Maps, or the phone's own maps app (Apple Maps on iPhone, a choice on Android) */
enum class DirectionsApp { GOOGLE_MAPS, PHONE_MAPS }

/**
 * The user's settings (Settings screen), kept on the phone. Each is Compose state, so screens and the theme follow a
 * change at once.
 */
class AppPreferences(private val store: Settings) {

    var theme: ThemeMode by mutableStateOf(enumOr(KEY_THEME, ThemeMode.SYSTEM))
        private set
    var textSize: TextSize by mutableStateOf(enumOr(KEY_TEXT_SIZE, TextSize.DEFAULT))
        private set
    var searchRadiusMeters: Int by mutableStateOf(store.getInt(KEY_RADIUS, DEFAULT_RADIUS).takeIf { it in RADII } ?: DEFAULT_RADIUS)
        private set
    var startTab: StartTab by mutableStateOf(enumOr(KEY_START_TAB, StartTab.SHOPS))
        private set
    var directionsApp: DirectionsApp by mutableStateOf(enumOr(KEY_DIRECTIONS, DirectionsApp.GOOGLE_MAPS))
        private set
    var vegOnly: Boolean by mutableStateOf(store.getBoolean(KEY_VEG_ONLY, false))
        private set
    var showEggItems: Boolean by mutableStateOf(store.getBoolean(KEY_SHOW_EGG, true))
        private set
    var notifyNearbyFeasts: Boolean by mutableStateOf(store.getBoolean(KEY_NOTIFY_FEASTS, true))
        private set
    /** A feast the user saved ended early */
    var notifySavedFeasts: Boolean by mutableStateOf(store.getBoolean(KEY_NOTIFY_SAVED_FEASTS, true))
        private set
    var notifyReportUpdates: Boolean by mutableStateOf(store.getBoolean(KEY_NOTIFY_REPORTS, true))
        private set
    var notifyShopReviews: Boolean by mutableStateOf(store.getBoolean(KEY_NOTIFY_REVIEWS, true))
        private set
    var notifyClaimStatus: Boolean by mutableStateOf(store.getBoolean(KEY_NOTIFY_CLAIMS, true))
        private set
    /** No notifications from 10 PM to 7 AM */
    var quietHours: Boolean by mutableStateOf(store.getBoolean(KEY_QUIET_HOURS, false))
        private set

    fun notification(kind: NotificationKind): Boolean = when (kind) {
        NotificationKind.NEARBY_FEASTS -> notifyNearbyFeasts
        NotificationKind.SAVED_FEASTS -> notifySavedFeasts
        NotificationKind.REPORT_UPDATES -> notifyReportUpdates
        NotificationKind.SHOP_REVIEWS -> notifyShopReviews
        NotificationKind.CLAIM_STATUS -> notifyClaimStatus
        NotificationKind.QUIET_HOURS -> quietHours
    }

    fun updateNotification(kind: NotificationKind, on: Boolean) {
        when (kind) {
            NotificationKind.NEARBY_FEASTS -> notifyNearbyFeasts = on
            NotificationKind.SAVED_FEASTS -> notifySavedFeasts = on
            NotificationKind.REPORT_UPDATES -> notifyReportUpdates = on
            NotificationKind.SHOP_REVIEWS -> notifyShopReviews = on
            NotificationKind.CLAIM_STATUS -> notifyClaimStatus = on
            NotificationKind.QUIET_HOURS -> quietHours = on
        }
        store.putBoolean(keyFor(kind), on)
    }

    private fun keyFor(kind: NotificationKind) = when (kind) {
        NotificationKind.NEARBY_FEASTS -> KEY_NOTIFY_FEASTS
        NotificationKind.SAVED_FEASTS -> KEY_NOTIFY_SAVED_FEASTS
        NotificationKind.REPORT_UPDATES -> KEY_NOTIFY_REPORTS
        NotificationKind.SHOP_REVIEWS -> KEY_NOTIFY_REVIEWS
        NotificationKind.CLAIM_STATUS -> KEY_NOTIFY_CLAIMS
        NotificationKind.QUIET_HOURS -> KEY_QUIET_HOURS
    }

    fun updateTheme(value: ThemeMode) { theme = value; store.putString(KEY_THEME, value.name) }
    fun updateTextSize(value: TextSize) { textSize = value; store.putString(KEY_TEXT_SIZE, value.name) }
    fun updateSearchRadius(meters: Int) {
        val value = meters.takeIf { it in RADII } ?: DEFAULT_RADIUS
        searchRadiusMeters = value
        store.putInt(KEY_RADIUS, value)
    }
    fun updateStartTab(value: StartTab) { startTab = value; store.putString(KEY_START_TAB, value.name) }
    fun updateDirectionsApp(value: DirectionsApp) { directionsApp = value; store.putString(KEY_DIRECTIONS, value.name) }
    fun updateVegOnly(value: Boolean) { vegOnly = value; store.putBoolean(KEY_VEG_ONLY, value) }
    fun updateShowEggItems(value: Boolean) { showEggItems = value; store.putBoolean(KEY_SHOW_EGG, value) }
    fun updateNotifyNearbyFeasts(value: Boolean) = updateNotification(NotificationKind.NEARBY_FEASTS, value)

    private inline fun <reified T : Enum<T>> enumOr(key: String, default: T): T =
        store.getStringOrNull(key)?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

    companion object {
        /** The search radii offered (meters); the backend allows up to 10 km */
        val RADII = listOf(500, 2_000, 5_000, 10_000)
        const val DEFAULT_RADIUS = 5_000

        const val STORE = "AppPreferences"
        private const val KEY_THEME = "theme"
        private const val KEY_TEXT_SIZE = "text_size"
        private const val KEY_RADIUS = "search_radius_m"
        private const val KEY_START_TAB = "start_tab"
        private const val KEY_DIRECTIONS = "directions_app"
        private const val KEY_VEG_ONLY = "veg_only"
        private const val KEY_SHOW_EGG = "show_egg_items"
        private const val KEY_NOTIFY_FEASTS = "notify_nearby_feasts"
        private const val KEY_NOTIFY_REPORTS = "notify_report_updates"
        private const val KEY_NOTIFY_SAVED_FEASTS = "notify_saved_feasts"
        private const val KEY_NOTIFY_REVIEWS = "notify_shop_reviews"
        private const val KEY_NOTIFY_CLAIMS = "notify_claim_status"
        private const val KEY_QUIET_HOURS = "quiet_hours"
    }
}
