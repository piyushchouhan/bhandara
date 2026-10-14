package com.example.bhandara.ui.feasts

import com.example.bhandara.data.network.ActivityAction
import com.example.bhandara.data.network.ActivityApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.distanceMeters
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.models.api.FeastReportReason
import com.example.bhandara.data.models.api.FeastReportRequest
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.components.PHOTOS_FAILED
import com.example.bhandara.ui.components.uploadReportPhotos
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * What a feast's page shows.
 *
 * @param distanceMeters from where the user is now; null without location permission
 * @param message        a one-time message (e.g. "Thanks for reporting"), cleared with onMessageShown()
 * @param isSaved        the user saved this feast (kept on the phone)
 */
data class FeastDetailsState(
    val loading: Boolean = true,
    val feast: FeastResponse? = null,
    val error: String? = null,
    val timing: FeastTiming = FeastTiming.Unknown,
    val distanceMeters: Double? = null,
    val isOrganizer: Boolean = false,
    val hasReported: Boolean = false,
    val reporting: Boolean = false,
    val message: String? = null,
    val isSaved: Boolean = false,
) {
    val isActive: Boolean get() = feast?.isActive == true

    /** Anyone but the organizer can flag a live feast as fake, once */
    val canReport: Boolean get() = isActive && !isOrganizer && !hasReported

    /** No point calling about a feast that's over or was taken down */
    val canCall: Boolean get() = isActive && timing != FeastTiming.Ended && !feast?.contactPhone.isNullOrBlank()
}

/** One community feast's page, shared by Android and iOS */
class FeastDetailsViewModel(
    private val feastId: String,
    private val feastApi: FeastApi,
    private val settings: AppSettings,
    private val currentUid: () -> String?,
    private val locationService: LocationService?,
    private val now: () -> LocalDateTime = ::localNow,
    /** Uploads photos sent with a report and returns their URLs (throws if any fails); null where not supported */
    private val uploadPhotos: (suspend (List<PickedPhoto>) -> List<String>)? = null,
    /** Counts views and directions for the organizer's numbers; null where not counted */
    private val activityApi: ActivityApi? = null,
) : ViewModel() {

    private var viewCounted = false

    private val _state = MutableStateFlow(FeastDetailsState(hasReported = settings.hasReportedFeast(feastId), isSaved = settings.isFeastSaved(feastId)))
    val state: StateFlow<FeastDetailsState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    /** The user is asking for directions to the feast */
    fun onDirections() = count(ActivityAction.DIRECTIONS)

    private fun count(action: ActivityAction) {
        val api = activityApi ?: return
        viewModelScope.launch { api.feast(feastId, action) }
    }

    /** Saves the feast to the user's places (on the phone), or takes it out */
    fun toggleSaved() {
        val saved = !_state.value.isSaved
        settings.setFeastSaved(feastId, saved)
        _state.update { it.copy(isSaved = saved, message = if (saved) "Saved to your places" else "Removed from your places") }
    }

    /** Reports the feast, saying why, with an optional note and photos */
    fun report(reason: FeastReportReason, note: String = "", photos: List<PickedPhoto> = emptyList()) {
        val state = _state.value
        if (!state.canReport || state.reporting) return
        _state.update { it.copy(reporting = true) }
        viewModelScope.launch {
            val photoUrls = uploadReportPhotos(uploadPhotos, photos) ?: run {
                _state.update { it.copy(reporting = false, message = PHOTOS_FAILED) }
                return@launch
            }
            val request = FeastReportRequest(reason.name, note.trim().ifEmpty { null }, photoUrls.ifEmpty { null })
            when (val result = feastApi.reportFeast(feastId, request)) {
                is ApiResult.Success -> {
                    settings.markReportedFeast(feastId)
                    // Enough reports take a feast down straight away
                    val takenDown = !result.value.isActive
                    _state.update {
                        it.copy(
                            reporting = false,
                            hasReported = true,
                            feast = it.feast?.copy(isActive = result.value.isActive),
                            message = if (takenDown) {
                                "Thanks for reporting. This feast has been taken down."
                            } else {
                                "Thanks for reporting. A moderator will take a look."
                            },
                        )
                    }
                }
                is ApiResult.Failure -> _state.update { it.copy(reporting = false, message = result.error.message) }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    private fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = feastApi.getFeast(feastId)) {
                is ApiResult.Success -> {
                    val feast = result.value
                    val uid = currentUid()
                    _state.update {
                        it.copy(
                            loading = false,
                            feast = feast,
                            timing = feastTiming(feast, now()),
                            isOrganizer = uid != null && feast.firebaseUid == uid,
                        )
                    }
                    // One view per visit to the page
                    if (!viewCounted) {
                        viewCounted = true
                        count(ActivityAction.VIEW)
                    }
                    measureDistance(feast)
                }
                is ApiResult.Failure -> _state.update { it.copy(loading = false, error = result.error.message) }
            }
        }
    }

    /** The backend doesn't know where the user is when it sends one feast, so the distance is worked out here */
    private suspend fun measureDistance(feast: FeastResponse) {
        val here = (locationService?.locate() as? LocationResult.Found)?.location ?: return
        _state.update { it.copy(distanceMeters = distanceMeters(here, GeoLocation(feast.latitude, feast.longitude))) }
    }
}
