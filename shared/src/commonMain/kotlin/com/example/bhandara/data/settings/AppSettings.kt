package com.example.bhandara.data.settings

import com.example.bhandara.data.models.api.MenuItemRequest
import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Small things the app remembers on this phone, the same way on Android (SharedPreferences) and iOS
 * (NSUserDefaults).
 *
 * The store names and keys are the ones the Android app has always used, so data saved by older versions is
 * still found after updating. Never rename them without migrating the old values.
 */
class AppSettings(factory: Settings.Factory) {

    private val sync = factory.create(USER_SYNC)
    private val vendor = factory.create(VENDOR)
    private val drafts = factory.create(DRAFT_MENU)
    private val shopActions = factory.create(SHOP_ACTIONS)
    private val reviewActions = factory.create(REVIEW_ACTIONS)
    private val verification = factory.create(VERIFICATION)

    private val json = Json { ignoreUnknownKeys = true }

    // ==================== Registration with the backend ====================

    /** Whether this user has been registered with the backend (location updates wait until they are) */
    fun isRegisteredWithBackend(uid: String): Boolean = sync.getBoolean(KEY_BACKEND_SYNCED + uid, false)

    fun markRegisteredWithBackend(uid: String) = sync.putBoolean(KEY_BACKEND_SYNCED + uid, true)

    // ==================== Vendor (moving cart owner) ====================

    /** The moving cart this phone's user listed, or null if none */
    val vendorShopId: Long?
        get() = vendor.getLong(KEY_VENDOR_SHOP_ID, NO_SHOP).takeIf { it != NO_SHOP }

    val vendorOwnerUid: String?
        get() = vendor.getStringOrNull(KEY_VENDOR_OWNER_UID)

    fun rememberVendorCart(shopId: Long, ownerUid: String) {
        vendor.putLong(KEY_VENDOR_SHOP_ID, shopId)
        vendor.putString(KEY_VENDOR_OWNER_UID, ownerUid)
    }

    /** Whether the vendor is sharing their cart's live location */
    var isVendorModeOn: Boolean
        get() = vendor.getBoolean(KEY_VENDOR_MODE, false)
        set(value) = vendor.putBoolean(KEY_VENDOR_MODE, value)

    // ==================== Draft menu while adding a shop ====================

    /** Menu items entered while adding a shop, kept until the shop is saved; unreadable drafts are dropped */
    var draftMenuItems: List<MenuItemRequest>
        get() = drafts.getStringOrNull(KEY_DRAFT_ITEMS)
            ?.let { runCatching { json.decodeFromString(ListSerializer(MenuItemRequest.serializer()), it) }.getOrNull() }
            .orEmpty()
        set(items) {
            if (items.isEmpty()) {
                drafts.remove(KEY_DRAFT_ITEMS)
            } else {
                drafts.putString(KEY_DRAFT_ITEMS, json.encodeToString(ListSerializer(MenuItemRequest.serializer()), items))
            }
        }

    // ==================== What this user already did (so the app doesn't offer it again) ====================

    fun hasReportedShop(shopId: String): Boolean = shopActions.getBoolean(KEY_REPORTED_SHOP + shopId, false)

    fun markReportedShop(shopId: String) = shopActions.putBoolean(KEY_REPORTED_SHOP + shopId, true)

    fun hasSuggestedDeletingShop(shopId: String): Boolean =
        shopActions.getBoolean(KEY_SUGGESTED_DELETE_SHOP + shopId, false)

    fun markSuggestedDeletingShop(shopId: String) = shopActions.putBoolean(KEY_SUGGESTED_DELETE_SHOP + shopId, true)

    fun hasReportedReview(reviewId: String): Boolean = reviewActions.getBoolean(KEY_REPORTED_REVIEW + reviewId, false)

    fun markReportedReview(reviewId: String) = reviewActions.putBoolean(KEY_REPORTED_REVIEW + reviewId, true)

    /** Whether the user already answered "Is this place real?" for this shop */
    fun hasAnsweredVerification(shopId: String): Boolean = verification.getBoolean(KEY_VERIFIED + shopId, false)

    fun markAnsweredVerification(shopId: String) = verification.putBoolean(KEY_VERIFIED + shopId, true)

    companion object {
        // Store names and keys used by the Android app since before this class existed
        const val USER_SYNC = "user_sync_prefs"
        const val VENDOR = "VendorPrefs"
        const val DRAFT_MENU = "DraftMenuItems"
        const val SHOP_ACTIONS = "ShopActionsPrefs"
        const val REVIEW_ACTIONS = "ReviewActionsPrefs"
        const val VERIFICATION = "VerificationPrefs"

        const val KEY_BACKEND_SYNCED = "backend_synced_"
        const val KEY_VENDOR_SHOP_ID = "vendor_shop_id"
        const val KEY_VENDOR_OWNER_UID = "vendor_owner_uid"
        const val KEY_VENDOR_MODE = "vendor_mode_active"
        const val KEY_DRAFT_ITEMS = "draft_items"
        const val KEY_REPORTED_SHOP = "reported_shop_"
        const val KEY_SUGGESTED_DELETE_SHOP = "suggested_delete_shop_"
        const val KEY_REPORTED_REVIEW = "reported_review_"
        const val KEY_VERIFIED = "verified_"

        private const val NO_SHOP = -1L
    }
}
