package com.example.bhandara.data.settings

import com.example.bhandara.data.models.api.MenuItemRequest
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What the app remembers on the phone. Runs on both platforms, with in-memory stores standing in for
 * SharedPreferences / NSUserDefaults.
 */
class AppSettingsTest {

    /** Hands out one in-memory store per name, and lets tests look inside them */
    private class Stores : Settings.Factory {
        val byName = mutableMapOf<String, MapSettings>()
        override fun create(name: String?): Settings = byName.getOrPut(name!!) { MapSettings() }
    }

    private val stores = Stores()
    private val settings = AppSettings(stores)

    @Test
    fun registrationWithTheBackendIsRememberedPerUser() {
        assertFalse(settings.isRegisteredWithBackend("user-a"))

        settings.markRegisteredWithBackend("user-a")

        assertTrue(settings.isRegisteredWithBackend("user-a"))
        assertFalse(settings.isRegisteredWithBackend("user-b"))
    }

    @Test
    fun aVendorsCartIsRememberedAndNothingBeforeThat() {
        assertNull(settings.vendorShopId)
        assertNull(settings.vendorOwnerUid)
        assertFalse(settings.isVendorModeOn)

        settings.rememberVendorCart(shopId = 42, ownerUid = "vendor-uid")
        settings.isVendorModeOn = true

        assertEquals(42L, settings.vendorShopId)
        assertEquals("vendor-uid", settings.vendorOwnerUid)
        assertTrue(settings.isVendorModeOn)
    }

    @Test
    fun aDraftMenuIsKeptUntilItIsCleared() {
        val items = listOf(MenuItemRequest("Vada Pav", "VEG", 20.0), MenuItemRequest("Egg Bun", "EGG", null))

        settings.draftMenuItems = items
        assertEquals(items, settings.draftMenuItems)

        settings.draftMenuItems = emptyList()
        assertEquals(emptyList(), settings.draftMenuItems)
    }

    @Test
    fun aDraftSavedByTheOlderAppIsStillRead() {
        // What the Android app wrote with org.json before this class existed
        stores.create(AppSettings.DRAFT_MENU).putString(
            AppSettings.KEY_DRAFT_ITEMS,
            """[{"name":"Vada Pav","foodType":"VEG","price":20.0},{"name":"Misal","foodType":"VEG","price":null}]""",
        )

        assertEquals(
            listOf(MenuItemRequest("Vada Pav", "VEG", 20.0), MenuItemRequest("Misal", "VEG", null)),
            settings.draftMenuItems,
        )
    }

    @Test
    fun anUnreadableDraftIsDroppedInsteadOfCrashingTheApp() {
        stores.create(AppSettings.DRAFT_MENU).putString(AppSettings.KEY_DRAFT_ITEMS, "{not a list")

        assertEquals(emptyList(), settings.draftMenuItems)
    }

    @Test
    fun whatTheUserAlreadyReportedIsRememberedPerItem() {
        settings.markReportedShop("5")
        settings.markSuggestedDeletingShop("6")
        settings.markReportedReview("7")
        settings.markAnsweredVerification("8")

        assertTrue(settings.hasReportedShop("5"))
        assertFalse(settings.hasReportedShop("6"))
        assertTrue(settings.hasSuggestedDeletingShop("6"))
        assertFalse(settings.hasSuggestedDeletingShop("5"))
        assertTrue(settings.hasReportedReview("7"))
        assertTrue(settings.hasAnsweredVerification("8"))
        assertFalse(settings.hasAnsweredVerification("5"))
    }

    @Test
    fun dataIsStoredWhereOlderVersionsOfTheAndroidAppKeptIt() {
        // Changing any of these names would make updated apps forget what users had saved
        settings.markRegisteredWithBackend("u1")
        settings.rememberVendorCart(42, "vendor-uid")
        settings.isVendorModeOn = true
        settings.draftMenuItems = listOf(MenuItemRequest("Chai", "VEG", 10.0))
        settings.markReportedShop("5")
        settings.markSuggestedDeletingShop("5")
        settings.markReportedReview("7")
        settings.markAnsweredVerification("8")

        val keysByStore = stores.byName.mapValues { (_, store) -> store.keys.toSet() }
        assertEquals(
            mapOf(
                "user_sync_prefs" to setOf("backend_synced_u1"),
                "VendorPrefs" to setOf("vendor_shop_id", "vendor_owner_uid", "vendor_mode_active"),
                "DraftMenuItems" to setOf("draft_items"),
                "ShopActionsPrefs" to setOf("reported_shop_5", "suggested_delete_shop_5"),
                "ReviewActionsPrefs" to setOf("reported_review_7"),
                "VerificationPrefs" to setOf("verified_8"),
            ),
            keysByStore,
        )
    }
}
