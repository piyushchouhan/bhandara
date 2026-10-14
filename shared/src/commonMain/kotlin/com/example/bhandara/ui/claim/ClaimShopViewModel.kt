package com.example.bhandara.ui.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.models.api.ShopClaimRequest
import com.example.bhandara.data.models.api.ShopClaimResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.photos.PhotoUploadException
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.reportfeast.normalizePhone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Photos of the shop front a claim can have (the backend's limit) */
const val MAX_CLAIM_PHOTOS = 3

/** What submitting a claim is doing right now, to show */
enum class ClaimProgress { LOCATING, UPLOADING, CHECKING }

/** How a claim was decided */
enum class ClaimOutcome { APPROVED, IN_REVIEW, REJECTED }

/**
 * @param error         what stops the user, ready to show
 * @param needsLocation the claim needs the user's location (permission or location off)
 * @param decision      the backend's decision, once made
 */
data class ClaimShopState(
    val photos: List<PickedPhoto> = emptyList(),
    val phone: String = "",
    val progress: ClaimProgress? = null,
    val uploadProgress: Int? = null,
    val error: String? = null,
    val needsLocation: Boolean = false,
    val decision: ShopClaimResponse? = null,
) {
    val submitting: Boolean get() = progress != null
    val canAddPhotos: Boolean get() = photos.size < MAX_CLAIM_PHOTOS
    val canSubmit: Boolean get() = photos.isNotEmpty() && !submitting && decision == null

    val outcome: ClaimOutcome?
        get() = when (decision?.status) {
            null -> null
            "APPROVED" -> ClaimOutcome.APPROVED
            "REJECTED" -> ClaimOutcome.REJECTED
            else -> ClaimOutcome.IN_REVIEW
        }
}

/**
 * Claiming a shop the platform listed, the same on Android and iOS: the real owner stands at the shop, takes photos
 * of its front (camera only, so they're taken there and now) and sends them with where they are.
 */
class ClaimShopViewModel(
    private val shopId: String,
    private val claimApi: ClaimApi,
    private val uploadPhotos: suspend (List<PickedPhoto>, (Int) -> Unit) -> List<String>,
    private val locationService: LocationService,
) : ViewModel() {

    private val _state = MutableStateFlow(ClaimShopState())
    val state: StateFlow<ClaimShopState> = _state.asStateFlow()

    fun addPhotos(photos: List<PickedPhoto>) =
        edit { it.copy(photos = it.photos + photos.take(MAX_CLAIM_PHOTOS - it.photos.size)) }

    fun removePhoto(photo: PickedPhoto) = edit { it.copy(photos = it.photos - photo) }

    fun onPhoneChange(text: String) = edit { it.copy(phone = text.take(20)) }

    /** After a rejection: start again with new photos */
    fun tryAgain() = _state.update { ClaimShopState(phone = it.phone) }

    /**
     * Finds where the user is (before uploading anything), uploads the photos and sends the claim. Anything that
     * fails is explained, and the photos stay to try again.
     */
    fun submit() {
        val state = _state.value
        if (!state.canSubmit) {
            if (state.photos.isEmpty() && state.decision == null) _state.update { it.copy(error = "Take at least one photo of the shop front") }
            return
        }
        if (state.phone.isNotBlank() && normalizePhone(state.phone) == null) {
            _state.update { it.copy(error = "Enter a phone number with 10 to 15 digits") }
            return
        }
        _state.update { it.copy(progress = ClaimProgress.LOCATING, error = null, needsLocation = false) }
        viewModelScope.launch {
            val here = (locationService.locate() as? LocationResult.Found)?.location ?: return@launch fail(
                "Your location is needed to check you're at the shop. Allow location and make sure it's on.",
                needsLocation = true,
            )
            _state.update { it.copy(progress = ClaimProgress.UPLOADING, uploadProgress = 0) }
            val urls = try {
                uploadPhotos(state.photos) { progress -> _state.update { it.copy(uploadProgress = progress) } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: PhotoUploadException) {
                return@launch fail("Couldn't upload the photos. Check your connection and try again.")
            }
            val shop = shopId.toLongOrNull() ?: return@launch fail("This shop can't be claimed")
            _state.update { it.copy(progress = ClaimProgress.CHECKING, uploadProgress = null) }
            val claim = ShopClaimRequest(
                shopId = shop,
                imageUrls = urls,
                latitude = here.latitude,
                longitude = here.longitude,
                ownerPhone = normalizePhone(state.phone),
            )
            when (val result = claimApi.claimShop(claim)) {
                is ApiResult.Success -> _state.update { it.copy(progress = null, decision = result.value) }
                is ApiResult.Failure -> fail(result.error.message)
            }
        }
    }

    private fun fail(message: String, needsLocation: Boolean = false) =
        _state.update { it.copy(progress = null, uploadProgress = null, error = message, needsLocation = needsLocation) }

    private fun edit(change: (ClaimShopState) -> ClaimShopState) {
        if (_state.value.submitting || _state.value.decision != null) return
        _state.update { change(it).copy(error = null) }
    }
}
