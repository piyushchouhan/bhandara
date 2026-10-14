package com.example.bhandara.ui.feasts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapPin
import com.example.bhandara.ui.shopsmap.MapLocationState
import com.example.bhandara.ui.shopsmap.ShopsMapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/** The groups the feasts list is split into, soonest first */
enum class FeastGroup { NOW, LATER_TODAY, TOMORROW, COMING_UP }

/** One group of the feasts list, e.g. everything happening now */
data class FeastSection(val group: FeastGroup, val feasts: List<FeastResponse>)

/**
 * What the "I'm hungry" map shows.
 *
 * @param checkedAt when the feasts were last checked against the clock; ended ones are hidden from then on
 */
data class FeastsMapState(
    val location: MapLocationState = MapLocationState.Locating,
    val camera: CameraTarget = CameraTarget(ShopsMapState.CENTRE_OF_INDIA, zoom = 4f),
    val feasts: List<FeastResponse> = emptyList(),
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null,
    val selectedId: String? = null,
    val checkedAt: LocalDateTime? = null,
) {
    /** Feasts on now or coming up; the list can be older than a feast's end, so ended ones are left out here too */
    fun visibleFeasts(): List<FeastResponse> =
        feasts.filter { feast -> checkedAt?.let { feastTiming(feast, it) } != FeastTiming.Ended }

    /** Loaded fine, but nothing is on nearby */
    val noFeastsNearby: Boolean get() = loaded && error == null && visibleFeasts().isEmpty()

    /**
     * @param unnamed the label for feasts without an organizer name, in the user's language
     * @param pill    for the redesign's pins: the text on each feast's pill ("Now", "6 PM"); live ones are on now
     */
    fun pins(unnamed: String, pill: ((FeastTiming) -> String)? = null): List<MapPin> = visibleFeasts().map {
        val timing = timing(it)
        MapPin(
            id = it.id,
            position = GeoLocation(it.latitude, it.longitude),
            label = it.organizerName?.takeIf(String::isNotBlank) ?: unnamed,
            emoji = FEAST_EMOJI,
            isLiveCart = false,
            feastPill = pill?.invoke(timing),
            feastLive = timing is FeastTiming.Now,
        )
    }

    fun selectedFeast(): FeastResponse? = selectedId?.let { id -> visibleFeasts().firstOrNull { it.id == id } }

    fun timing(feast: FeastResponse): FeastTiming = checkedAt?.let { feastTiming(feast, it) } ?: FeastTiming.Unknown

    /**
     * The feasts split into happening now, later today, tomorrow and coming up, leaving out empty groups. On now
     * comes nearest first; the others soonest first, then nearest. Feasts with unreadable times count as coming up.
     */
    fun sections(): List<FeastSection> {
        val grouped = visibleFeasts().groupBy { feast ->
            when (timing(feast)) {
                is FeastTiming.Now -> FeastGroup.NOW
                is FeastTiming.Today -> FeastGroup.LATER_TODAY
                is FeastTiming.Tomorrow -> FeastGroup.TOMORROW
                else -> FeastGroup.COMING_UP
            }
        }
        val nearest = compareBy<FeastResponse> { it.distance ?: Double.MAX_VALUE }
        val soonest = compareBy<FeastResponse> { it.feastDate }.thenBy { it.startTime }.then(nearest)
        return FeastGroup.entries.mapNotNull { group ->
            grouped[group]?.sortedWith(if (group == FeastGroup.NOW) nearest else soonest)?.let { FeastSection(group, it) }
        }
    }

    companion object {
        const val FEAST_EMOJI = "🍲"
    }
}

/** The "I'm hungry" map of community feasts around the user, shared by Android and iOS */
class FeastsMapViewModel(
    private val locationService: LocationService,
    private val feastApi: FeastApi,
    private val now: () -> LocalDateTime = ::localNow,
    /** How far to look, as chosen in Settings */
    private val radiusMeters: () -> Int = { 5_000 },
) : ViewModel() {

    private val _state = MutableStateFlow(FeastsMapState())
    val state: StateFlow<FeastsMapState> = _state.asStateFlow()

    private var started = false

    /** Find the user and load the feasts; only the first call does anything (the screen calls it when shown) */
    fun start() {
        if (started) return
        started = true
        locateAndLoad()
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) locateAndLoad() else _state.update { it.copy(location = MapLocationState.PermissionDenied) }
    }

    fun retry() = locateAndLoad()

    /** Back to where the user is now, with the feasts around them refreshed */
    fun recenter() = locateAndLoad()

    fun onPinClick(pin: MapPin) = _state.update { it.copy(selectedId = pin.id, checkedAt = now()) }

    fun clearSelection() = _state.update { it.copy(selectedId = null) }

    private fun locateAndLoad() {
        if (_state.value.location !is MapLocationState.Found) _state.update { it.copy(location = MapLocationState.Locating) }
        viewModelScope.launch {
            when (val result = locationService.locate()) {
                is LocationResult.Found -> {
                    _state.update {
                        it.copy(location = MapLocationState.Found(result.location), camera = CameraTarget(result.location))
                    }
                    load(result.location)
                }
                LocationResult.PermissionDenied -> _state.update { it.copy(location = MapLocationState.PermissionNeeded) }
                LocationResult.Unavailable -> _state.update { it.copy(location = MapLocationState.Unavailable) }
            }
        }
    }

    private suspend fun load(near: GeoLocation) {
        _state.update { it.copy(loading = true, error = null) }
        when (val result = feastApi.getNearby(near, radiusMeters().toDouble())) {
            is ApiResult.Success -> _state.update {
                it.copy(loading = false, loaded = true, feasts = result.value, checkedAt = now())
            }
            is ApiResult.Failure -> _state.update { it.copy(loading = false, error = result.error.message) }
        }
    }
}
