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
    fun aHalfFilledShopFormIsKeptUntilItIsCleared() {
        assertNull(settings.addShopDraft)

        val draft = AddShopDraft(
            isMovingCart = false, shopName = "Sharma Chaat", shopType = "Street Food",
            menuItemNames = listOf("Pani Puri"), photoUris = listOf("content://photos/1"),
            ownerPhone = "+919876543210", fullAddress = "Vishrantwadi, Pune", homeDelivery = true,
        )
        settings.addShopDraft = draft
        assertEquals(draft, settings.addShopDraft)

        settings.addShopDraft = null
        assertNull(settings.addShopDraft)
    }

    @Test
    fun anUntouchedFormIsNotADraft() {
        settings.addShopDraft = AddShopDraft(shopName = "Sharma Chaat")
        settings.addShopDraft = AddShopDraft() // e.g. after "Clear All"

        assertNull(settings.addShopDraft)
    }

    @Test
    fun anUnreadableShopDraftIsTreatedAsNone() {
        stores.create(AppSettings.DRAFT_MENU).putString(AppSettings.KEY_DRAFT_SHOP, "{broken")

        assertNull(settings.addShopDraft)
    }

    @Test
    fun whatTheUserAlreadyReportedIsRememberedPerItem() {
        settings.markReportedShop("5")
        settings.markSuggestedDeletingShop("6")
        settings.markReportedReview("7")
        settings.markAnsweredVerification("8")
        settings.markReportedFeast("9")

        assertTrue(settings.hasReportedShop("5"))
        assertFalse(settings.hasReportedShop("6"))
        assertTrue(settings.hasSuggestedDeletingShop("6"))
        assertFalse(settings.hasSuggestedDeletingShop("5"))
        assertTrue(settings.hasReportedReview("7"))
        assertTrue(settings.hasAnsweredVerification("8"))
        assertFalse(settings.hasAnsweredVerification("5"))
        assertTrue(settings.hasReportedFeast("9"))
        assertFalse(settings.hasReportedFeast("5"))
    }

    @Test
    fun aShopCanBeSavedAndUnsaved() {
        assertFalse(settings.isShopSaved("6"))

        settings.setShopSaved("6", true)
        assertTrue(settings.isShopSaved("6"))
        assertFalse(settings.isShopSaved("7"))

        settings.setShopSaved("6", false)
        assertFalse(settings.isShopSaved("6"))
    }

    @Test
    fun savedShopsAndFeastsAreCountedAndListed() {
        assertEquals(0, settings.savedPlaceCount)
        settings.setShopSaved("6", true)
        settings.setShopSaved("7", true)
        settings.setShopSaved("6", false)
        settings.setFeastSaved("6", true)

        assertEquals(2, settings.savedPlaceCount)
        assertEquals(listOf("7"), settings.savedShopIds)
        assertEquals(listOf("6"), settings.savedFeastIds)
        // A shop and a feast can have the same id; they're kept apart
        assertFalse(settings.isShopSaved("6"))
        assertTrue(settings.isFeastSaved("6"))
    }

    @Test
    fun theTimeAPlaceWasSavedIsKeptUntilItIsUnsaved() {
        var now = 1_000L
        val timed = AppSettings(Stores(), nowMillis = { now })

        timed.setShopSaved("6", true)
        now = 2_000L
        timed.setFeastSaved("9", true)

        assertEquals(1_000L, timed.savedAt(AppSettings.KEY_SAVED_SHOP, "6"))
        assertEquals(2_000L, timed.savedAt(AppSettings.KEY_SAVED_FEAST, "9"))
        timed.setShopSaved("6", false)
        assertEquals(0L, timed.savedAt(AppSettings.KEY_SAVED_SHOP, "6"))
    }

    @Test
    fun dataIsStoredWhereOlderVersionsOfTheAndroidAppKeptIt() {
        // Changing any of these names would make updated apps forget what users had saved
        settings.markRegisteredWithBackend("u1")
        settings.markPushTokenSent("u1", "push-token")
        settings.rememberVendorCart(42, "vendor-uid")
        settings.isVendorModeOn = true
        settings.draftMenuItems = listOf(MenuItemRequest("Chai", "VEG", 10.0))
        settings.addShopDraft = AddShopDraft(shopName = "Chai Point")
        settings.markReportedShop("5")
        settings.markSuggestedDeletingShop("5")
        settings.markReportedReview("7")
        settings.markAnsweredVerification("8")
        settings.markReportedFeast("9")
        settings.setShopSaved("6", true)

        val keysByStore = stores.byName.mapValues { (_, store) -> store.keys.toSet() }
        assertEquals(
            mapOf(
                "user_sync_prefs" to setOf("backend_synced_u1", "push_token_sent_u1"),
                "VendorPrefs" to setOf("vendor_shop_id", "vendor_owner_uid", "vendor_mode_active"),
                "DraftMenuItems" to setOf("draft_items", "draft_shop"),
                "ShopActionsPrefs" to setOf("reported_shop_5", "suggested_delete_shop_5"),
                "ReviewActionsPrefs" to setOf("reported_review_7"),
                "VerificationPrefs" to setOf("verified_8"),
                "FeastActionsPrefs" to setOf("reported_feast_9"),
                "SavedPlacesPrefs" to setOf("saved_shop_6", "saved_at_saved_shop_6"),
                // New with syncing saved places: the save waits here until it reaches the account
                "SavedSyncPrefs" to setOf("pending_SHOP_6"),
            ),
            keysByStore,
        )
    }
}
