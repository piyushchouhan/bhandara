package com.example.bhandara.ui.shopsmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.network.ApiError
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FeedApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapPin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Where the map stands on knowing the user's location */
sealed interface MapLocationState {
    data object Locating : MapLocationState

    data class Found(val location: GeoLocation) : MapLocationState

    /** Not decided yet: the screen should ask */
    data object PermissionNeeded : MapLocationState

    /** The user said no: the screen explains how to turn it on */
    data object PermissionDenied : MapLocationState

    /** Allowed, but the phone can't tell where it is (e.g. location services off) */
    data object Unavailable : MapLocationState
}

data class ShopsMapState(
    val location: MapLocationState = MapLocationState.Locating,
    val camera: CameraTarget = CameraTarget(CENTRE_OF_INDIA, zoom = 4f),
    val shops: List<LocalShopResponse> = emptyList(),
    val carts: List<LocalShopResponse> = emptyList(),
    val loadingShops: Boolean = false,
    /** Why shops couldn't be loaded, ready to show */
    val error: String? = null,
    /** A one-off note for the user (e.g. "Thanks for verifying!"); cleared with onMessageShown */
    val message: String? = null,
    val searchQuery: String = "",
    val showMovingVendors: Boolean = false,
    val zoom: Float = CameraTarget.DEFAULT_ZOOM,
    /** A nearby unverified shop or cart to ask "Is this place real?" about */
    val verificationPrompt: LocalShopResponse? = null,
) {
    /**
     * The pins to draw: live carts in moving-vendor mode, otherwise shops; both filtered by the search. Shops come
     * best first, and only the best few are shown until the user zooms in, so the map stays readable.
     */
    fun pins(): List<MapPin> {
        if (showMovingVendors) {
            return carts.filter { it.matches(searchQuery) }.map { it.toPin(emoji = "🚚", isLiveCart = true) }
        }
        val limit = when {
            zoom >= 16f -> Int.MAX_VALUE
            zoom >= 15f -> 10
            else -> 5
        }
        return shops.filter { it.matches(searchQuery) }.take(limit).map { it.toPin(emoji = "📍", isLiveCart = false) }
    }

    private fun LocalShopResponse.matches(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return shopName.contains(q, ignoreCase = true) ||
            cuisineType?.contains(q, ignoreCase = true) == true ||
            shopType?.contains(q, ignoreCase = true) == true ||
            menuItems?.any { it.contains(q, ignoreCase = true) } == true
    }

    private fun LocalShopResponse.toPin(emoji: String, isLiveCart: Boolean) =
        MapPin(id = id, position = GeoLocation(latitude, longitude), label = shopName, emoji = emoji, isLiveCart = isLiveCart)

    companion object {
        /** Before the user is found the map shows the whole country, never a city they might not be in */
        val CENTRE_OF_INDIA = GeoLocation(22.0, 79.0)
    }
}

/** The shops map: shared by Android and iOS */
class ShopsMapViewModel(
    private val locationService: LocationService,
    private val feedApi: FeedApi,
    private val shopApi: ShopApi,
    private val settings: AppSettings,
) : ViewModel() {

    private val _state = MutableStateFlow(ShopsMapState())
    val state: StateFlow<ShopsMapState> = _state.asStateFlow()

    private var started = false

    /** Find the user and load the map; only the first call does anything (the screen calls it when shown) */
    fun start() {
        if (started) return
        started = true
        locateAndLoad()
    }

    /** After the screen asked for location access */
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) locateAndLoad() else _state.update { it.copy(location = MapLocationState.PermissionDenied) }
    }

    /** Try again after the location or the shops couldn't be loaded */
    fun retry() {
        val location = (_state.value.location as? MapLocationState.Found)?.location
        if (location != null && _state.value.error != null) {
            viewModelScope.launch { loadShops(location) }
        } else {
            locateAndLoad()
        }
    }

    /** Move the map back to where the user is now */
    fun recenter() {
        viewModelScope.launch {
            val result = locationService.locate()
            if (result is LocationResult.Found) {
                _state.update {
                    it.copy(location = MapLocationState.Found(result.location), camera = CameraTarget(result.location))
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) = _state.update { it.copy(searchQuery = query) }

    fun onShowMovingVendorsChange(show: Boolean) = _state.update { it.copy(showMovingVendors = show) }

    fun onZoomChanged(zoom: Float) = _state.update { it.copy(zoom = zoom) }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    /** Closes the "Is this place real?" card without answering; it may come back next time */
    fun dismissVerification() = _state.update { it.copy(verificationPrompt = null) }

    /** Answers "Is this place real?" with YES, NO or NOT_SURE, from where the user is now */
    fun answerVerification(vote: String) {
        val prompt = _state.value.verificationPrompt ?: return
        _state.update { it.copy(verificationPrompt = null) }
        viewModelScope.launch {
            // YES/NO only count from people who are actually there, so send where the user is right now
            val here = (locationService.locate() as? LocationResult.Found)?.location
            val message = when (val result = shopApi.verifyShop(prompt.id, vote, here)) {
                is ApiResult.Success -> {
                    settings.markAnsweredVerification(prompt.id)
                    "Thanks for verifying!"
                }
                is ApiResult.Failure -> {
                    // A conflict means this user shouldn't be asked again; other refusals (e.g. too far away)
                    // let the question come back when they're closer
                    if (result.error.kind == ApiError.Kind.CONFLICT) settings.markAnsweredVerification(prompt.id)
                    result.error.message
                }
            }
            _state.update { it.copy(message = message) }
        }
    }

    private fun locateAndLoad() {
        _state.update { it.copy(location = MapLocationState.Locating) }
        viewModelScope.launch {
            when (val result = locationService.locate()) {
                is LocationResult.Found -> {
                    _state.update {
                        it.copy(location = MapLocationState.Found(result.location), camera = CameraTarget(result.location))
                    }
                    loadShops(result.location)
                }
                LocationResult.PermissionDenied -> _state.update { it.copy(location = MapLocationState.PermissionNeeded) }
                LocationResult.Unavailable -> _state.update { it.copy(location = MapLocationState.Unavailable) }
            }
        }
    }

    private suspend fun loadShops(near: GeoLocation) {
        _state.update { it.copy(loadingShops = true, error = null) }
        when (val result = feedApi.getFeed(near)) {
            is ApiResult.Success -> {
                val feed = result.value
                val prompt = feed.verificationPrompt?.takeUnless { settings.hasAnsweredVerification(it.id) }
                _state.update {
                    it.copy(
                        loadingShops = false,
                        shops = feed.activeShops.filter { shop -> shop.isMovingCart != true },
                        carts = feed.activeShops.filter { shop -> shop.isMovingCart == true },
                        verificationPrompt = prompt,
                    )
                }
            }
            is ApiResult.Failure -> _state.update { it.copy(loadingShops = false, error = result.error.message) }
        }
    }
}
