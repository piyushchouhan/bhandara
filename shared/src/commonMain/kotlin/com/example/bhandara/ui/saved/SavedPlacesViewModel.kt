package com.example.bhandara.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.distanceMeters
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.network.ApiError
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.ui.feasts.FeastTiming
import com.example.bhandara.ui.feasts.feastTiming
import com.example.bhandara.ui.feasts.localNow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/** A shop or feast the user saved, as it is now */
sealed interface SavedPlace {
    val id: String

    /** When it was saved (ms since 1970); 0 for places saved before that was kept */
    val savedAt: Long

    /** From where the user is now; null without their location, or for a place that's gone */
    val distanceMeters: Double?

    /** [shop] is null when it no longer exists */
    data class Shop(override val id: String, val shop: LocalShopResponse?, override val savedAt: Long, override val distanceMeters: Double?) : SavedPlace {
        /** Still on the map: not deactivated, rejected or deleted */
        val isListed: Boolean get() = shop != null && shop.isActive != false && shop.verificationStatus != "REJECTED"
        val isOpenNow: Boolean get() = isListed && shop?.isCurrentlyOpen == true
    }

    /** [feast] is null when it no longer exists */
    data class Feast(override val id: String, val feast: FeastResponse?, override val savedAt: Long, override val distanceMeters: Double?, val timing: FeastTiming) : SavedPlace {
        /** Over, taken down or gone: nothing left to go to */
        val isOver: Boolean get() = feast == null || feast.isActive == false || timing == FeastTiming.Ended || feast.status == "TAKEN_DOWN" || feast.status == "PAST"
        val isHappeningNow: Boolean get() = !isOver && timing is FeastTiming.Now
    }
}

enum class SavedFilter { ALL, OPEN_NOW, FEASTS }

enum class SavedSort { RECENT, NEAREST, RATING }

/**
 * Saved places (Profile → My activity): the shops and feasts the user saved on this phone, as they are now
 *
 * @param error why the places couldn't be loaded, ready to show
 * @param removed the place just taken out of the saved list, until the user undoes it or the note goes away
 * @param selectedId the place tapped on the map, shown in a card
 */
data class SavedPlacesState(
    val loading: Boolean = true,
    val error: String? = null,
    val places: List<SavedPlace> = emptyList(),
    val filter: SavedFilter = SavedFilter.ALL,
    val sort: SavedSort = SavedSort.RECENT,
    val showMap: Boolean = false,
    val removed: SavedPlace? = null,
    val selectedId: String? = null,
    val here: GeoLocation? = null,
) {
    val openNowCount: Int get() = places.count { it.isOpenNow }
    val feastCount: Int get() = places.count { it is SavedPlace.Feast }

    /** The places to list, filtered and sorted; the order keeps those still there before those that are gone */
    fun shown(): List<SavedPlace> {
        val filtered = when (filter) {
            SavedFilter.ALL -> places
            SavedFilter.OPEN_NOW -> places.filter { it.isOpenNow }
            SavedFilter.FEASTS -> places.filterIsInstance<SavedPlace.Feast>()
        }
        val sorted = when (sort) {
            SavedSort.RECENT -> filtered.sortedByDescending { it.savedAt }
            SavedSort.NEAREST -> filtered.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
            SavedSort.RATING -> filtered.sortedByDescending { (it as? SavedPlace.Shop)?.shop?.averageRating ?: -1.0 }
        }
        return sorted.sortedBy { it.isGone }
    }

    /** What the map shows: the places still there, with a spot */
    fun onMap(): List<SavedPlace> = shown().filter { !it.isGone }

    val selected: SavedPlace? get() = places.firstOrNull { it.key == selectedId }
}

/** Shops open now, and feasts on right now */
val SavedPlace.isOpenNow: Boolean
    get() = when (this) {
        is SavedPlace.Shop -> isOpenNow
        is SavedPlace.Feast -> isHappeningNow
    }

/** Closed down, removed, or a feast that's over */
val SavedPlace.isGone: Boolean
    get() = when (this) {
        is SavedPlace.Shop -> !isListed
        is SavedPlace.Feast -> isOver
    }

/** Unique across shops and feasts (their ids can be the same number) */
val SavedPlace.key: String
    get() = when (this) {
        is SavedPlace.Shop -> "shop:$id"
        is SavedPlace.Feast -> "feast:$id"
    }

class SavedPlacesViewModel(
    private val settings: AppSettings,
    private val shopApi: ShopApi,
    private val feastApi: FeastApi,
    private val locationService: LocationService?,
    private val now: () -> LocalDateTime = ::localNow,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedPlacesState())
    val state: StateFlow<SavedPlacesState> = _state.asStateFlow()

    init {
        load()
    }

    /**
     * Loads every saved place at once. A place the backend no longer has is shown as gone; any other failure (e.g.
     * no connection) fails the whole list, so nothing is wrongly shown as gone.
     */
    fun load() {
        _state.update { it.copy(loading = it.places.isEmpty(), error = null) }
        viewModelScope.launch {
            val here = (locationService?.locate() as? LocationResult.Found)?.location
            val shops = settings.savedShopIds.map { id ->
                async { id to shopApi.getShop(id) }
            }
            val feasts = settings.savedFeastIds.map { id ->
                async { id to feastApi.getFeast(id) }
            }
            var failure: String? = null
            val places = mutableListOf<SavedPlace>()
            for ((id, result) in shops.awaitAll()) {
                val shop = when (result) {
                    is ApiResult.Success -> result.value
                    is ApiResult.Failure -> if (result.error.kind == ApiError.Kind.NOT_FOUND) null else { failure = failure ?: result.error.message; continue }
                }
                val distance = shop?.let { s -> here?.let { distanceMeters(it, GeoLocation(s.latitude, s.longitude)) } }
                places += SavedPlace.Shop(id, shop, settings.savedAt(AppSettings.KEY_SAVED_SHOP, id), distance)
            }
            for ((id, result) in feasts.awaitAll()) {
                val feast = when (result) {
                    is ApiResult.Success -> result.value
                    is ApiResult.Failure -> if (result.error.kind == ApiError.Kind.NOT_FOUND) null else { failure = failure ?: result.error.message; continue }
                }
                val distance = feast?.let { f -> here?.let { distanceMeters(it, GeoLocation(f.latitude, f.longitude)) } }
                val timing = feast?.let { feastTiming(it, now()) } ?: FeastTiming.Unknown
                places += SavedPlace.Feast(id, feast, settings.savedAt(AppSettings.KEY_SAVED_FEAST, id), distance, timing)
            }
            val error = failure
            _state.update {
                if (error != null && it.places.isEmpty()) it.copy(loading = false, error = error, here = here)
                else it.copy(loading = false, places = places, here = here, error = null)
            }
        }
    }

    fun onFilter(filter: SavedFilter) = _state.update { it.copy(filter = filter) }

    fun onSort(sort: SavedSort) = _state.update { it.copy(sort = sort) }

    fun onShowMap(show: Boolean) = _state.update { it.copy(showMap = show, selectedId = null) }

    fun select(place: SavedPlace?) = _state.update { it.copy(selectedId = place?.key) }

    /** Takes the place out of the saved list; [undo] puts it back */
    fun remove(place: SavedPlace) {
        save(place, false)
        _state.update { state ->
            state.copy(
                places = state.places.filterNot { it.key == place.key },
                removed = place,
                selectedId = state.selectedId.takeIf { it != place.key },
            )
        }
    }

    fun undo() {
        val place = _state.value.removed ?: return
        save(place, true)
        _state.update { it.copy(places = it.places + place, removed = null) }
    }

    /** The "Removed …" note went away */
    fun onRemovedShown() = _state.update { it.copy(removed = null) }

    private fun save(place: SavedPlace, saved: Boolean) = when (place) {
        is SavedPlace.Shop -> settings.setShopSaved(place.id, saved)
        is SavedPlace.Feast -> settings.setFeastSaved(place.id, saved)
    }
}
