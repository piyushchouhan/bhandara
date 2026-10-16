package com.example.bhandara.data.settings

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.bhandara.data.models.api.MenuItemRequest
import com.russhwolf.settings.Settings
import kotlin.time.Clock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Small things the app remembers on this phone, the same way on Android (SharedPreferences) and iOS
 * (NSUserDefaults).
 *
 * The store names and keys are the ones the Android app has always used, so data saved by older versions is
 * still found after updating. Never rename them without migrating the old values.
 */
/**
 * A saved place, or a change to one: [kind] is "SHOP" or "FEAST", [savedAt] ms since 1970 (0 when unsaved)
 */
data class SavedChange(val kind: String, val id: String, val saved: Boolean, val savedAt: Long)

class AppSettings(factory: Settings.Factory, private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }) {

    /** The user's settings (Settings screen) */
    val preferences: AppPreferences by lazy { AppPreferences(factory.create(AppPreferences.STORE)) }


    private val sync = factory.create(USER_SYNC)
    private val vendor = factory.create(VENDOR)
    private val drafts = factory.create(DRAFT_MENU)
    private val shopActions = factory.create(SHOP_ACTIONS)
    private val reviewActions = factory.create(REVIEW_ACTIONS)
    private val feastActions = factory.create(FEAST_ACTIONS)
    private val verification = factory.create(VERIFICATION)
    private val savedPlaces = factory.create(SAVED_PLACES)
    private val savedSync = factory.create(SAVED_SYNC)

    private val json = Json { ignoreUnknownKeys = true }

    // ==================== Registration with the backend ====================

    /** Whether this user has been registered with the backend (location updates wait until they are) */
    fun isRegisteredWithBackend(uid: String): Boolean = sync.getBoolean(KEY_BACKEND_SYNCED + uid, false)

    fun markRegisteredWithBackend(uid: String) = sync.putBoolean(KEY_BACKEND_SYNCED + uid, true)

    /** The backend no longer knows this user (e.g. its data was reset): the phone registers again */
    fun forgetRegisteredWithBackend(uid: String) {
        sync.remove(KEY_BACKEND_SYNCED + uid)
        sync.remove(KEY_PUSH_TOKEN_SENT + uid)
    }

    /** The push-notification token the backend last confirmed for this user, or null */
    fun lastPushTokenSent(uid: String): String? = sync.getStringOrNull(KEY_PUSH_TOKEN_SENT + uid)

    fun markPushTokenSent(uid: String, fcmToken: String) = sync.putString(KEY_PUSH_TOKEN_SENT + uid, fcmToken)

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

    // ==================== Drafts while adding a shop ====================

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

    /**
     * The "add a shop" form as the user left it, or null if there is none. Setting an empty draft (or null)
     * removes it; an unreadable draft is treated as none.
     */
    var addShopDraft: AddShopDraft?
        get() = drafts.getStringOrNull(KEY_DRAFT_SHOP)
            ?.let { runCatching { json.decodeFromString(AddShopDraft.serializer(), it) }.getOrNull() }
            ?.takeUnless { it.isEmpty }
        set(draft) {
            if (draft == null || draft.isEmpty) {
                drafts.remove(KEY_DRAFT_SHOP)
            } else {
                drafts.putString(KEY_DRAFT_SHOP, json.encodeToString(AddShopDraft.serializer(), draft))
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

    fun hasReportedFeast(feastId: String): Boolean = feastActions.getBoolean(KEY_REPORTED_FEAST + feastId, false)

    fun markReportedFeast(feastId: String) = feastActions.putBoolean(KEY_REPORTED_FEAST + feastId, true)

    /** Whether the user already answered "Is this place real?" for this shop */
    fun hasAnsweredVerification(shopId: String): Boolean = verification.getBoolean(KEY_VERIFIED + shopId, false)

    fun markAnsweredVerification(shopId: String) = verification.putBoolean(KEY_VERIFIED + shopId, true)

    // ==================== Places the user saved ====================
    // Kept on the phone so they work offline; each change is also queued until it reaches the account
    // (data/saved/SavedPlacesSync), so every phone the user signs in on shows the same places.

    fun isShopSaved(shopId: String): Boolean = savedPlaces.getBoolean(KEY_SAVED_SHOP + shopId, false)

    fun setShopSaved(shopId: String, saved: Boolean) = setSaved(KEY_SAVED_SHOP, shopId, saved)

    fun isFeastSaved(feastId: String): Boolean = savedPlaces.getBoolean(KEY_SAVED_FEAST + feastId, false)

    fun setFeastSaved(feastId: String, saved: Boolean) = setSaved(KEY_SAVED_FEAST, feastId, saved)

    val savedShopIds: List<String> get() = idsWith(KEY_SAVED_SHOP)

    val savedFeastIds: List<String> get() = idsWith(KEY_SAVED_FEAST)

    /** When the shop or feast was saved (ms since 1970); 0 for places saved before this was kept */
    fun savedAt(key: String, id: String): Long = savedPlaces.getLong(KEY_SAVED_AT + key + id, 0L)

    /** How many shops and feasts the user saved */
    val savedPlaceCount: Int get() = savedShopIds.size + savedFeastIds.size

    /** Forgets every saved place on this phone, and what was still to be synced (the account was deleted or switched) */
    fun clearSavedPlaces() {
        savedPlaces.clear()
        savedSync.clear()
        _savedChanges.value++
    }

    private val _savedChanges = MutableStateFlow(0L)

    /** Goes up each time the user saves or unsaves something, so it can be synced */
    val savedChanges: StateFlow<Long> = _savedChanges.asStateFlow()

    /** The account the saved places were last synced with, and whether it was a guest (anonymous) one */
    var savedSyncedUid: String?
        get() = savedSync.getStringOrNull(KEY_SYNCED_UID)
        set(value) = if (value == null) savedSync.remove(KEY_SYNCED_UID) else savedSync.putString(KEY_SYNCED_UID, value)

    var savedSyncedAsGuest: Boolean
        get() = savedSync.getBoolean(KEY_SYNCED_AS_GUEST, true)
        set(value) = savedSync.putBoolean(KEY_SYNCED_AS_GUEST, value)

    /** Saves and unsaves not yet synced with the account, in no particular order */
    fun pendingSavedChanges(): List<SavedChange> =
        savedSync.keys.filter { it.startsWith(KEY_PENDING) }.mapNotNull { key ->
            val (kind, id) = key.removePrefix(KEY_PENDING).split('_', limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
            val prefix = prefixFor(kind) ?: return@mapNotNull null
            val saved = savedSync.getBoolean(key, false)
            SavedChange(kind, id, saved, if (saved) savedAt(prefix, id) else 0L)
        }

    /** Queues every place saved on this phone, e.g. to bring a guest's places into the account they signed in to */
    fun queueAllSaved() {
        savedShopIds.forEach { savedSync.putBoolean(KEY_PENDING + SHOP + "_" + it, true) }
        savedFeastIds.forEach { savedSync.putBoolean(KEY_PENDING + FEAST + "_" + it, true) }
    }

    /**
     * After a sync: the phone shows what the account has, plus anything changed on the phone since [sent] went out
     * (still queued); [sent] leaves the queue unless it changed again meanwhile
     */
    fun applySyncedSaved(account: List<SavedChange>, sent: List<SavedChange>) {
        sent.forEach { change ->
            val key = KEY_PENDING + change.kind + "_" + change.id
            if (savedSync.getBooleanOrNull(key) == change.saved) savedSync.remove(key)
        }
        val stillPending = pendingSavedChanges()
        savedPlaces.clear()
        account.forEach { place -> prefixFor(place.kind)?.let { write(it, place.id, true, place.savedAt) } }
        stillPending.forEach { change -> prefixFor(change.kind)?.let { write(it, change.id, change.saved, change.savedAt.takeIf { at -> at > 0 } ?: nowMillis()) } }
        // Not a change to sync: counting it would start another sync, and that another, without end
    }

    private fun setSaved(prefix: String, id: String, saved: Boolean) {
        write(prefix, id, saved, nowMillis())
        savedSync.putBoolean(KEY_PENDING + kindFor(prefix) + "_" + id, saved)
        _savedChanges.value++
    }

    private fun write(prefix: String, id: String, saved: Boolean, at: Long) {
        if (saved) {
            savedPlaces.putBoolean(prefix + id, true)
            savedPlaces.putLong(KEY_SAVED_AT + prefix + id, at)
        } else {
            savedPlaces.remove(prefix + id)
            savedPlaces.remove(KEY_SAVED_AT + prefix + id)
        }
    }

    private fun kindFor(prefix: String) = if (prefix == KEY_SAVED_SHOP) SHOP else FEAST

    private fun prefixFor(kind: String) = when (kind) {
        SHOP -> KEY_SAVED_SHOP
        FEAST -> KEY_SAVED_FEAST
        else -> null
    }

    private fun idsWith(prefix: String): List<String> =
        savedPlaces.keys.filter { it.startsWith(prefix) }.map { it.removePrefix(prefix) }

    companion object {
        // Store names and keys used by the Android app since before this class existed
        const val USER_SYNC = "user_sync_prefs"
        const val VENDOR = "VendorPrefs"
        const val DRAFT_MENU = "DraftMenuItems"
        const val SHOP_ACTIONS = "ShopActionsPrefs"
        const val REVIEW_ACTIONS = "ReviewActionsPrefs"
        const val VERIFICATION = "VerificationPrefs"
        const val FEAST_ACTIONS = "FeastActionsPrefs"
        const val SAVED_PLACES = "SavedPlacesPrefs"
        const val SAVED_SYNC = "SavedSyncPrefs"

        const val KEY_BACKEND_SYNCED = "backend_synced_"
        const val KEY_PUSH_TOKEN_SENT = "push_token_sent_"
        const val KEY_VENDOR_SHOP_ID = "vendor_shop_id"
        const val KEY_VENDOR_OWNER_UID = "vendor_owner_uid"
        const val KEY_VENDOR_MODE = "vendor_mode_active"
        const val KEY_DRAFT_ITEMS = "draft_items"
        const val KEY_DRAFT_SHOP = "draft_shop"
        const val KEY_REPORTED_SHOP = "reported_shop_"
        const val KEY_SUGGESTED_DELETE_SHOP = "suggested_delete_shop_"
        const val KEY_REPORTED_REVIEW = "reported_review_"
        const val KEY_VERIFIED = "verified_"
        const val KEY_REPORTED_FEAST = "reported_feast_"
        const val KEY_SAVED_SHOP = "saved_shop_"
        const val KEY_SAVED_FEAST = "saved_feast_"
        const val KEY_SAVED_AT = "saved_at_"
        private const val KEY_PENDING = "pending_"
        private const val KEY_SYNCED_UID = "synced_uid"
        private const val KEY_SYNCED_AS_GUEST = "synced_as_guest"

        /** What the backend calls a saved shop and a saved feast */
        const val SHOP = "SHOP"
        const val FEAST = "FEAST"

        private const val NO_SHOP = -1L
    }
}
