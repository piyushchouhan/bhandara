package com.example.bhandara.ui.shopsmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.ShopZone
import com.example.bhandara.data.network.ApiError
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FeedApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapPin
import com.example.bhandara.ui.map.MapZone
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
    /** The pin the user tapped, shown in a card */
    val selectedId: String? = null,
    /** Groups of fixed shops standing close together */
    val zones: List<ShopZone> = emptyList(),
) {
    /** The tapped place, if its pin is still on the map (a search or mode change can hide it) */
    fun selectedPlace(): LocalShopResponse? {
        val id = selectedId ?: return null
        if (pins().none { it.id == id }) return null
        return (shops + carts).firstOrNull { it.id == id }
    }

    /** Shops match the search but only some are shown until the user zooms in */
    fun hasMoreShopsWhenZoomedIn(): Boolean =
        !showMovingVendors && shops.count { it.matches(searchQuery) } > pins().count { it.zoneCount == null }

    /** Zones are only for browsing shops: a search or the moving-vendor mode shows places one by one */
    private fun showsZones(): Boolean = !showMovingVendors && searchQuery.isBlank()

    /**
     * The shapes of the shop zones: clear while zoomed out (where they stand in for their shops), faint from
     * [ZONE_PINS_ZOOM] on, where the shops' own pins are shown.
     */
    fun mapZones(): List<MapZone> {
        if (!showsZones()) return emptyList()
        return zones.filter { it.outline.isNotEmpty() }.map { zone ->
            MapZone(
                id = zone.id,
                outline = zone.outline.filter { it.size >= 2 }.map { GeoLocation(it[0], it[1]) },
                faint = zoom >= ZONE_PINS_ZOOM,
            )
        }
    }

    /**
     * The pins to draw: live carts in moving-vendor mode, otherwise shops; both filtered by the search. Shops come
     * best first, and only the best few are shown until the user zooms in, so the map stays readable; zoomed out,
     * each shop zone has one label pin (named in [language]) instead of its shops. The tapped place is marked
     * selected and the others dimmed.
     */
    fun pins(language: String = "en"): List<MapPin> {
        val places = if (showMovingVendors) {
            carts.filter { it.matches(searchQuery) }.map { it.toPin(emoji = "🚚", isLiveCart = true) }
        } else {
            val limit = when {
                zoom >= 16f -> Int.MAX_VALUE
                zoom >= 15f -> 10
                else -> 5
            }
            // Zoomed out, a zone's label stands in for its shops (the tapped one stays); the rest compete for the
            // few places shown
            val zoned = zoom < ZONE_PINS_ZOOM && showsZones()
            val inZones = if (zoned) zones.flatMap { it.shopIds }.toSet() else emptySet()
            val loose = shops.filter { it.matches(searchQuery) && it.id !in inZones }.take(limit)
            val keptSelected = shops.filter { it.id == selectedId && it.id in inZones }
            val zonePins = if (zoned) zones.map { it.toPin(language) } else emptyList()
            zonePins + (loose + keptSelected).map { it.toPin(emoji = "📍", isLiveCart = false) }
        }
        val selected = selectedId?.takeIf { id -> places.any { it.id == id } } ?: return places
        return places.map { it.copy(selected = it.id == selected, dimmed = it.id != selected) }
    }

    private fun LocalShopResponse.matches(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return shopName.contains(q, ignoreCase = true) ||
            cuisineType?.contains(q, ignoreCase = true) == true ||
            shopType?.contains(q, ignoreCase = true) == true ||
            menuItems?.any { it.contains(q, ignoreCase = true) } == true
    }

    private fun ShopZone.toPin(language: String) = MapPin(
        id = ZONE_PIN_PREFIX + id,
        position = GeoLocation(centerLatitude, centerLongitude),
        label = nameIn(language).orEmpty(),
        emoji = zoneEmoji(type),
        zoneCount = shopCount,
    )

    private fun LocalShopResponse.toPin(emoji: String, isLiveCart: Boolean) =
        MapPin(id = id, position = GeoLocation(latitude, longitude), label = shopName, emoji = emoji, isLiveCart = isLiveCart)

    companion object {
        /** Before the user is found the map shows the whole country, never a city they might not be in */
        val CENTRE_OF_INDIA = GeoLocation(22.0, 79.0)

        /**
         * From this zoom on a zone's shops have their own pins; below it the zone's flag stands in for them. At 17 a
         * dp is about 1.2 m, so shops 10 m apart no longer sit on each other (at 16 they'd be ~4 dp apart).
         */
        const val ZONE_PINS_ZOOM = 17f

        /** Tapping a zone zooms in this far, where its shops' pins are spread apart */
        const val ZONE_TAP_ZOOM = 18f

        /** Zone labels are pins with this id prefix, so they can't clash with a shop's id */
        const val ZONE_PIN_PREFIX = "zone:"

        fun zoneEmoji(type: String?): String = when (type) {
            "STREET_FOOD" -> "🍢"
            "FISH_MARKET" -> "🐟"
            "VEG_MARKET" -> "🥬"
            "SWEETS" -> "🍬"
            "TEA_SNACKS" -> "☕"
            else -> "🏪"
        }
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

    /**
     * Find the user and load the map (the screen calls it each time it's shown). Coming back to it (e.g. after
     * adding a shop) loads the shops again where the user was found, without moving the map.
     */
    fun start() {
        if (started) return refresh()
        started = true
        locateAndLoad()
    }

    private fun refresh() {
        val location = (_state.value.location as? MapLocationState.Found)?.location ?: return
        if (_state.value.loadingShops) return
        viewModelScope.launch { loadShops(location) }
    }

    /** After the screen asked for location access */
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) locateAndLoad() else _state.update { it.copy(location = MapLocationState.PermissionDenied) }
    }

    /**
     * What the phone allows now, read each time the app comes back (e.g. from the permission dialog or Settings).
     * The map can think location is off when it isn't: on Android, a second permission request while another is
     * showing (the app asks at start too) answers "denied" at once, even if the user then allows the first.
     */
    fun onLocationAccessChanged(allowed: Boolean) {
        val location = _state.value.location
        if (allowed && (location == MapLocationState.PermissionNeeded || location == MapLocationState.PermissionDenied)) locateAndLoad()
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

    /** A shop or cart opens its card; a zone's label zooms in on the zone */
    fun onPinClick(pin: MapPin) {
        if (pin.id.startsWith(ShopsMapState.ZONE_PIN_PREFIX)) {
            onZoneClick(pin.id.removePrefix(ShopsMapState.ZONE_PIN_PREFIX))
        } else {
            _state.update { it.copy(selectedId = pin.id) }
        }
    }

    /** Zooms in on the zone, close enough that each of its shops has its own pin */
    fun onZoneClick(zoneId: String) {
        val zone = _state.value.zones.firstOrNull { it.id == zoneId } ?: return
        _state.update {
            it.copy(camera = CameraTarget(GeoLocation(zone.centerLatitude, zone.centerLongitude), zoom = ShopsMapState.ZONE_TAP_ZOOM))
        }
    }

    fun clearSelection() = _state.update { it.copy(selectedId = null) }

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
        // As far as the user chose in Settings (5 km unless changed)
        when (val result = feedApi.getFeed(near, settings.preferences.searchRadiusMeters.toDouble())) {
            is ApiResult.Success -> {
                val feed = result.value
                val prompt = feed.verificationPrompt?.takeUnless { settings.hasAnsweredVerification(it.id) }
                _state.update {
                    it.copy(
                        loadingShops = false,
                        shops = feed.activeShops.filter { shop -> shop.isMovingCart != true },
                        carts = feed.activeShops.filter { shop -> shop.isMovingCart == true },
                        verificationPrompt = prompt,
                        zones = feed.zones,
                    )
                }
            }
            is ApiResult.Failure -> _state.update { it.copy(loadingShops = false, error = result.error.message) }
        }
    }
}
