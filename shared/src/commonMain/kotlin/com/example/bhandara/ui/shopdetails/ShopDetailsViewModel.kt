package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.models.api.FssaiStatus
import com.example.bhandara.data.network.FssaiApi
import com.example.bhandara.data.network.ShopWeekStats
import com.example.bhandara.data.network.ActivityAction
import com.example.bhandara.data.network.ActivityApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.distanceMeters
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.MenuItemResponse
import com.example.bhandara.data.models.api.ShopReportReason
import com.example.bhandara.data.network.ApiError
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.components.PHOTOS_FAILED
import com.example.bhandara.ui.components.uploadReportPhotos
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.network.getOrNull
import com.example.bhandara.data.settings.AppSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * What the shop details page shows.
 *
 * @param menu            the menu with prices; empty for older shops that only have [LocalShopResponse.menuItems]
 * @param distanceMeters  from where the user is now; null without location permission
 * @param isSaved         the user saved this shop (kept on the phone)
 * @param working         a report, deletion suggestion or deactivation is being sent
 * @param message         a one-time message (e.g. "Thanks for reporting"), cleared with onMessageShown()
 */
data class ShopDetailsState(
    val loading: Boolean = true,
    val shop: LocalShopResponse? = null,
    val menu: List<MenuItemResponse> = emptyList(),
    val error: String? = null,
    val distanceMeters: Double? = null,
    val isOwner: Boolean = false,
    val hasReported: Boolean = false,
    val hasSuggestedDelete: Boolean = false,
    val isSaved: Boolean = false,
    val working: Boolean = false,
    val message: String? = null,
    /** Manage shop only: views, directions and calls this week; null until known */
    val weekStats: ShopWeekStats? = null,
    /** The owner's latest FSSAI licence (whether it's waiting or was turned down); null when none or not known */
    val ownFssai: FssaiStatus? = null,
) {
    val isActive: Boolean get() = shop?.isActive != false

    /** Owners manage their shop; everyone else can flag it, once each */
    val canReport: Boolean get() = shop != null && !isOwner && !hasReported
    val canSuggestDelete: Boolean get() = shop != null && !isOwner && isActive && !hasSuggestedDelete
    val canDeactivate: Boolean get() = isOwner && isActive
    val canEditMenu: Boolean get() = isOwner && isActive

    /** Listed by the platform and not claimed yet, so its real owner can claim it */
    val canClaim: Boolean get() = shop?.ownerClaimed == false && isActive && !isOwner
    val canCall: Boolean get() = isActive && !shop?.ownerPhone.isNullOrBlank()

    /** The owner of a fixed shop can send an FSSAI licence (carts don't get the badge) */
    val canAddFssai: Boolean get() = isOwner && isActive && shop?.isMovingCart != true
}

/** One shop's page, the same on Android and iOS */
class ShopDetailsViewModel(
    private val shopId: String,
    private val shopApi: ShopApi,
    private val settings: AppSettings,
    private val currentUid: () -> String?,
    private val locationService: LocationService?,
    /** Counts views, directions and calls for the owner's numbers; null where not counted */
    private val activityApi: ActivityApi? = null,
    /** The owner's own page (Manage shop): nothing is counted, and this week's numbers are loaded instead */
    private val ownerView: Boolean = false,
    /** The owner's own FSSAI licence; null where it isn't shown */
    private val fssaiApi: FssaiApi? = null,
    /** Uploads photos sent with a report and returns their URLs (throws if any fails); null where not supported */
    private val uploadPhotos: (suspend (List<PickedPhoto>) -> List<String>)? = null,
) : ViewModel() {

    private var viewCounted = false

    private val _state = MutableStateFlow(
        ShopDetailsState(
            hasReported = settings.hasReportedShop(shopId),
            hasSuggestedDelete = settings.hasSuggestedDeletingShop(shopId),
            isSaved = settings.isShopSaved(shopId),
        )
    )
    val state: StateFlow<ShopDetailsState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    /** The user is asking for directions to the shop */
    fun onDirections() = count(ActivityAction.DIRECTIONS)

    /** The user is calling the shop */
    fun onCall() = count(ActivityAction.CALL)

    private fun count(action: ActivityAction) {
        val api = activityApi ?: return
        if (ownerView) return
        viewModelScope.launch { api.shop(shopId, action) }
    }

    /** Views, directions and calls this week, for the owner; stays unknown ("–") if they can't be fetched */
    private fun loadWeekStats() {
        val api = activityApi ?: return
        viewModelScope.launch {
            api.shopWeek(shopId).getOrNull()?.let { stats -> _state.update { it.copy(weekStats = stats) } }
        }
    }

    /** After the owner added items, so the page shows them */
    fun refreshMenu() {
        viewModelScope.launch {
            shopApi.getMenu(shopId).getOrNull()?.let { menu -> _state.update { it.copy(menu = menu) } }
        }
    }

    /** Reports the shop, with what the user said about it (a note and photos, both optional) */
    fun report(reason: ShopReportReason, note: String = "", photos: List<PickedPhoto> = emptyList()) {
        if (!_state.value.canReport) return
        send(
            call = {
                val photoUrls = uploadReportPhotos(uploadPhotos, photos)
                    ?: return@send ApiResult.Failure(ApiError(ApiError.Kind.NETWORK, PHOTOS_FAILED))
                shopApi.reportShop(shopId, reason, note, photoUrls)
            },
            thanks = "Thanks for reporting. A moderator will take a look.",
        ) {
            settings.markReportedShop(shopId)
            it.copy(hasReported = true)
        }
    }

    fun suggestDelete() {
        if (!_state.value.canSuggestDelete) return
        send(
            call = { shopApi.suggestDeleteShop(shopId) },
            thanks = "Thanks. A moderator will review this shop.",
        ) {
            settings.markSuggestedDeletingShop(shopId)
            it.copy(hasSuggestedDelete = true)
        }
    }

    /** The owner takes their shop off the map */
    fun deactivate() {
        if (!_state.value.canDeactivate) return
        send(
            call = { shopApi.deactivateShop(shopId) },
            thanks = "Shop deactivated. It's no longer shown to others.",
        ) { state -> state.copy(shop = state.shop?.copy(isActive = false)) }
    }

    /** Saves the shop to the user's places, or takes it off again */
    fun toggleSaved() {
        val saved = !_state.value.isSaved
        settings.setShopSaved(shopId, saved)
        _state.update { it.copy(isSaved = saved, message = if (saved) "Saved to your places" else "Removed from your places") }
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    /** The owner sent a licence from the shop page: it now waits for the check */
    fun onFssaiSent() {
        _state.update { it.copy(message = "Sent for checking. It's usually checked within 2 days.") }
        loadOwnFssai()
    }

    private fun loadOwnFssai() {
        val api = fssaiApi ?: return
        if (!_state.value.canAddFssai) return
        viewModelScope.launch {
            api.mine(shopId).getOrNull()?.let { own -> _state.update { it.copy(ownFssai = own) } }
        }
    }

    private fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val menu = async { shopApi.getMenu(shopId) }
            when (val result = shopApi.getShop(shopId)) {
                is ApiResult.Success -> {
                    val shop = result.value
                    val uid = currentUid()
                    _state.update {
                        it.copy(
                            loading = false,
                            shop = shop,
                            // Without a menu the page still shows the shop, with its list of item names
                            menu = menu.await().getOrNull().orEmpty(),
                            isOwner = uid != null && shop.ownerUid == uid,
                        )
                    }
                    // One view per visit to the page (a retry after a failure isn't another one)
                    if (ownerView) {
                        loadWeekStats()
                    } else if (!viewCounted) {
                        viewCounted = true
                        count(ActivityAction.VIEW)
                    }
                    loadOwnFssai()
                    measureDistance(shop)
                }
                is ApiResult.Failure -> {
                    menu.cancel()
                    _state.update { it.copy(loading = false, error = result.error.message) }
                }
            }
        }
    }

    /** The backend doesn't know where the user is when it sends one shop, so the distance is worked out here */
    private suspend fun measureDistance(shop: LocalShopResponse) {
        val here = (locationService?.locate() as? LocationResult.Found)?.location ?: return
        _state.update { it.copy(distanceMeters = distanceMeters(here, GeoLocation(shop.latitude, shop.longitude))) }
    }

    /**
     * Sends one action at a time. On success the page keeps what it shows (the backend's answer lacks e.g. the
     * crowd level) and only applies [onSuccess]; on failure it explains why.
     */
    private fun send(call: suspend () -> ApiResult<*>, thanks: String, onSuccess: (ShopDetailsState) -> ShopDetailsState) {
        if (_state.value.working) return
        _state.update { it.copy(working = true) }
        viewModelScope.launch {
            when (val result = call()) {
                is ApiResult.Success -> _state.update { onSuccess(it).copy(working = false, message = thanks) }
                is ApiResult.Failure -> _state.update { it.copy(working = false, message = result.error.message) }
            }
        }
    }
}
