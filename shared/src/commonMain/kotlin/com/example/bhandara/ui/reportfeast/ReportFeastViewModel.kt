package com.example.bhandara.ui.reportfeast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.photos.PhotoUploadException
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.feasts.localNow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/** Where the form starts: a new feast, an existing one to change, or a past one to post again */
sealed interface FeastFormStart {
    data object New : FeastFormStart

    /** Changing a feast that hasn't ended; it stays where it is on the map */
    data class Edit(val feastId: String) : FeastFormStart

    /** A new feast from a past one: same food, photos, organizer and place details, a new date */
    data class Repost(val feastId: String) : FeastFormStart
}

/**
 * @param loading         the feast to edit or repost is being fetched
 * @param error           what stops the user, ready to show
 * @param needsLocation   the feast couldn't be posted without the user's location (permission or location off)
 * @param uploadProgress  0-100 while photos upload
 * @param postedFeastId   the new feast, once posted; the screen opens it
 */
data class ReportFeastState(
    val step: FeastStep = FeastStep.FOOD,
    val form: FeastForm = FeastForm(),
    val error: String? = null,
    val needsLocation: Boolean = false,
    val submitting: Boolean = false,
    val uploadProgress: Int? = null,
    val postedFeastId: String? = null,
    val start: FeastFormStart = FeastFormStart.New,
    val loading: Boolean = false,
) {
    val isLastStep: Boolean get() = step == FeastStep.entries.last()
    val canAddPhotos: Boolean get() = form.photoCount < MAX_FEAST_PHOTOS
    val isEditing: Boolean get() = start is FeastFormStart.Edit
}

/**
 * Posting a community feast, the same on Android and iOS: what's served (with photos), when, and where. The feast
 * goes on the map where the user is when they post it. Also changes one of the user's feasts ([FeastFormStart.Edit],
 * keeping its place) or starts a new one from a past one ([FeastFormStart.Repost]).
 *
 * @param uploadPhotos uploads the photos, all or none (see [com.example.bhandara.photos.PhotoUploader])
 */
class ReportFeastViewModel(
    private val feastApi: FeastApi,
    private val uploadPhotos: suspend (List<PickedPhoto>, (Int) -> Unit) -> List<String>,
    private val locationService: LocationService,
    private val currentUid: () -> String?,
    private val now: () -> LocalDateTime = ::localNow,
    private val start: FeastFormStart = FeastFormStart.New,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportFeastState(start = start, loading = start != FeastFormStart.New))
    val state: StateFlow<ReportFeastState> = _state.asStateFlow()

    /** Where the feast being edited is; an edit keeps it */
    private var editedPlace: Pair<Double, Double>? = null

    init {
        val feastId = when (start) {
            is FeastFormStart.Edit -> start.feastId
            is FeastFormStart.Repost -> start.feastId
            FeastFormStart.New -> null
        }
        if (feastId != null) load(feastId)
    }

    /** Fills the form from the feast being edited or reposted */
    fun load(feastId: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = feastApi.getFeast(feastId)) {
                is ApiResult.Success -> {
                    val feast = result.value
                    val editing = start is FeastFormStart.Edit
                    if (editing) editedPlace = feast.latitude to feast.longitude
                    _state.update { it.copy(loading = false, form = feast.toForm(keepDate = editing)) }
                }
                is ApiResult.Failure -> _state.update { it.copy(loading = false, error = result.error.message) }
            }
        }
    }

    // ==================== Filling in the form ====================

    fun addPhotos(photos: List<PickedPhoto>) =
        edit { it.copy(photos = it.photos + photos.take(MAX_FEAST_PHOTOS - it.photoCount)) }

    fun removePhoto(photo: PickedPhoto) = edit { it.copy(photos = it.photos - photo) }

    /** Drops a photo the feast already had */
    fun removePhotoUrl(url: String) = edit { it.copy(photoUrls = it.photoUrls - url) }

    /** Typing a menu item; a comma adds what's before it, so "puri, halwa," becomes two items */
    fun onMenuInputChange(text: String) = edit { form ->
        if (',' !in text) return@edit form.copy(menuInput = text)
        val parts = text.split(',')
        form.copy(menuItems = withItems(form.menuItems, parts.dropLast(1)), menuInput = parts.last().trimStart())
    }

    /** "Done" on the keyboard: adds the item being typed */
    fun addMenuInput() = edit { it.copy(menuItems = withItems(it.menuItems, listOf(it.menuInput)), menuInput = "") }

    fun removeMenuItem(item: String) = edit { it.copy(menuItems = it.menuItems - item) }

    fun onFoodTypeChange(text: String) = edit { it.copy(foodType = text.take(50)) }
    fun onDescriptionChange(text: String) = edit { it.copy(description = text.take(MAX_FEAST_DESCRIPTION)) }
    fun onDateChange(date: LocalDate) = edit { it.copy(date = date) }
    fun onStartTimeChange(time: LocalTime) = edit { it.copy(startTime = time) }
    fun onEndTimeChange(time: LocalTime) = edit { it.copy(endTime = time) }
    fun onOrganizerNameChange(text: String) = edit { it.copy(organizerName = text.take(100)) }
    fun onContactPhoneChange(text: String) = edit { it.copy(contactPhone = text.take(20)) }
    fun onAddressChange(text: String) = edit { it.copy(address = text.take(MAX_FEAST_ADDRESS)) }
    fun onLandmarkChange(text: String) = edit { it.copy(landmark = text.take(MAX_FEAST_LANDMARK)) }
    fun onCapacityChange(text: String) = edit { it.copy(capacity = text.filter { c -> c.isDigit() }.take(6)) }

    /** Starts over with an empty form (a new feast; editing has nothing to clear) */
    fun clearAll() {
        if (!_state.value.submitting && start == FeastFormStart.New) _state.value = ReportFeastState()
    }

    // ==================== Moving between steps ====================

    /** Whether the current step is filled in well enough to go on */
    fun canGoOn(): Boolean = with(_state.value) { form.problem(now(), listOf(step)) == null }

    fun next() {
        val state = _state.value
        val problem = state.form.problem(now(), listOf(state.step))
        if (problem != null) {
            _state.update { it.copy(error = problem.message) }
            return
        }
        val next = FeastStep.entries.getOrNull(state.step.ordinal + 1) ?: return
        _state.update { it.copy(step = next, error = null, form = it.form.withTypedItemAdded()) }
    }

    /** Back one step; false on the first step, so the screen closes */
    fun back(): Boolean {
        val state = _state.value
        if (state.submitting) return true
        val previous = FeastStep.entries.getOrNull(state.step.ordinal - 1) ?: return false
        _state.update { it.copy(step = previous, error = null) }
        return true
    }

    fun onErrorShown() = _state.update { it.copy(error = null) }

    // ==================== Posting ====================

    /**
     * Checks everything, finds where the user is (before uploading anything, so photos aren't uploaded for nothing),
     * uploads the photos and posts the feast. Anything that fails is explained and nothing is posted.
     */
    fun submit() {
        val state = _state.value
        if (state.submitting || state.loading || state.postedFeastId != null) return
        val form = state.form.withTypedItemAdded()
        form.problem(now())?.let { problem ->
            _state.update { it.copy(form = form, step = problem.step, error = problem.message) }
            return
        }
        _state.update { it.copy(form = form, submitting = true, error = null, needsLocation = false) }
        viewModelScope.launch {
            // An edit keeps the feast where it is; a new feast goes where the user is
            val (latitude, longitude) = editedPlace ?: when (val result = locationService.locate()) {
                is LocationResult.Found -> result.location.latitude to result.location.longitude
                else -> return@launch fail(
                    "Your location is needed to put the feast on the map. Allow location and make sure it's on.",
                    needsLocation = true,
                )
            }
            val uploaded = if (form.photos.isEmpty()) emptyList() else {
                _state.update { it.copy(uploadProgress = 0) }
                try {
                    uploadPhotos(form.photos) { progress -> _state.update { it.copy(uploadProgress = progress) } }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: PhotoUploadException) {
                    return@launch fail("Couldn't upload the photos. Check your connection and try again.")
                }
            }
            val photoUrls = form.photoUrls + uploaded
            val request = FeastRequest(
                firebaseUid = currentUid().orEmpty(),
                organizerName = form.organizerName.trim().ifEmpty { null },
                contactPhone = normalizePhone(form.contactPhone),
                menuItems = form.allMenuItems,
                foodType = form.foodType.trim().ifEmpty { null },
                description = form.description.trim().ifEmpty { null },
                imageUrls = photoUrls,
                feastDate = form.date.toString(),
                startTime = form.startTime!!.asBackendTime(),
                endTime = form.endTime!!.asBackendTime(),
                latitude = latitude,
                longitude = longitude,
                address = form.address.trim().ifEmpty { null },
                landmark = form.landmark.trim().ifEmpty { null },
                estimatedCapacity = form.capacity.trim().toIntOrNull(),
            )
            val result = when (start) {
                is FeastFormStart.Edit -> feastApi.updateFeast(start.feastId, request)
                else -> feastApi.createFeast(request)
            }
            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(submitting = false, uploadProgress = null, postedFeastId = result.value.id)
                }
                is ApiResult.Failure -> fail(result.error.message)
            }
        }
    }

    private fun fail(message: String, needsLocation: Boolean = false) =
        _state.update { it.copy(submitting = false, uploadProgress = null, error = message, needsLocation = needsLocation) }

    private fun edit(change: (FeastForm) -> FeastForm) {
        if (_state.value.submitting) return
        _state.update { it.copy(form = change(it.form), error = null) }
    }

    /** "18:30:00", as the backend reads times */
    private fun LocalTime.asBackendTime(): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00"

    private fun FeastForm.withTypedItemAdded() = copy(menuItems = allMenuItems, menuInput = "")

    /** The form for a feast; a repost gets no date, so the user picks a new one */
    private fun FeastResponse.toForm(keepDate: Boolean) = FeastForm(
        photoUrls = imageUrls.take(MAX_FEAST_PHOTOS),
        menuItems = menuItems.take(MAX_FEAST_MENU_ITEMS),
        foodType = foodType.orEmpty(),
        description = description.orEmpty(),
        date = if (keepDate) runCatching { LocalDate.parse(feastDate) }.getOrNull() else null,
        startTime = runCatching { LocalTime.parse(startTime) }.getOrNull(),
        endTime = runCatching { LocalTime.parse(endTime) }.getOrNull(),
        organizerName = organizerName.orEmpty(),
        contactPhone = contactPhone.orEmpty(),
        address = address.orEmpty(),
        landmark = landmark.orEmpty(),
        capacity = estimatedCapacity.takeIf { it > 0 }?.toString().orEmpty(),
    )

    private fun withItems(items: List<String>, new: List<String>): List<String> =
        (items + new.map { it.trim() }).filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(MAX_FEAST_MENU_ITEMS)
}
