package com.example.bhandara.data.settings

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The user's settings stay on the phone across restarts, with sensible defaults before anything is picked */
class AppPreferencesTest {

    private val store = MapSettings()

    @Test
    fun beforeAnythingIsPickedTheDefaultsApply() {
        val preferences = AppPreferences(store)

        assertEquals(ThemeMode.SYSTEM, preferences.theme)
        assertEquals(TextSize.DEFAULT, preferences.textSize)
        assertEquals(5_000, preferences.searchRadiusMeters)
        assertEquals(StartTab.SHOPS, preferences.startTab)
        assertEquals(DirectionsApp.GOOGLE_MAPS, preferences.directionsApp)
        assertFalse(preferences.vegOnly)
        assertTrue(preferences.showEggItems)
        assertTrue(preferences.notifyNearbyFeasts)
        assertTrue(preferences.notifyReportUpdates)
        assertTrue(preferences.notifyShopReviews)
        assertTrue(preferences.notifyClaimStatus)
        assertFalse(preferences.quietHours, "quiet hours are opt-in")
    }

    @Test
    fun whatIsPickedIsKeptForTheNextStart() {
        AppPreferences(store).apply {
            updateTheme(ThemeMode.DARK)
            updateTextSize(TextSize.LARGEST)
            updateSearchRadius(2_000)
            updateStartTab(StartTab.FEASTS)
            updateDirectionsApp(DirectionsApp.PHONE_MAPS)
            updateVegOnly(true)
            updateShowEggItems(false)
            updateNotifyNearbyFeasts(false)
            updateNotification(NotificationKind.REPORT_UPDATES, false)
            updateNotification(NotificationKind.QUIET_HOURS, true)
        }

        val again = AppPreferences(store)

        assertEquals(ThemeMode.DARK, again.theme)
        assertEquals(TextSize.LARGEST, again.textSize)
        assertEquals(2_000, again.searchRadiusMeters)
        assertEquals(StartTab.FEASTS, again.startTab)
        assertEquals(DirectionsApp.PHONE_MAPS, again.directionsApp)
        assertTrue(again.vegOnly)
        assertFalse(again.showEggItems)
        assertFalse(again.notifyNearbyFeasts)
        assertFalse(again.notifyReportUpdates)
        assertTrue(again.notifyShopReviews)
        assertTrue(again.quietHours)
    }

    @Test
    fun onlyTheOfferedRadiiAreUsed() {
        val preferences = AppPreferences(store)

        preferences.updateSearchRadius(50_000)
        assertEquals(5_000, preferences.searchRadiusMeters, "more than the backend allows falls back to the default")

        // e.g. an older app version's 50 km testing value, or a damaged store
        store.putInt("search_radius_m", 123)
        store.putString("theme", "NEON")
        val fresh = AppPreferences(store)
        assertEquals(5_000, fresh.searchRadiusMeters)
        assertEquals(ThemeMode.SYSTEM, fresh.theme)
    }
}
