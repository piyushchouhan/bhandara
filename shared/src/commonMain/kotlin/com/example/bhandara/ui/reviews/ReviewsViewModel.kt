package com.example.bhandara.ui.reviews

import com.example.bhandara.data.network.ActivityApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.ReportDetails
import com.example.bhandara.data.models.api.ReviewRequest
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ApiError
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.data.network.getOrNull
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.photos.PickedPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Most photos one review can have (the backend's limit) */
const val MAX_REVIEW_PHOTOS = 5

/** Longest comment the backend accepts */
const val MAX_REVIEW_COMMENT = 1000

/**
 * A review being written or edited.
 *
 * @param reviewId       the review being edited; null for a new one
 * @param rating         1 to 5 stars; 0 until the user picks
 * @param keptImageUrls  photos already on the review that stay
 * @param newPhotos      photos taken or chosen on this phone, uploaded when the review is sent
 */
data class ReviewDraft(
    val reviewId: String? = null,
    val rating: Int = 0,
    val comment: String = "",
    val keptImageUrls: List<String> = emptyList(),
    val newPhotos: List<PickedPhoto> = emptyList(),
) {
    val photoCount: Int get() = keptImageUrls.size + newPhotos.size
    val canAddPhotos: Boolean get() = photoCount < MAX_REVIEW_PHOTOS
    val canSubmit: Boolean get() = rating in 1..5
}

/**
 * What the reviews tab shows.
 *
 * @param myUid           the signed-in user, to find their own review
 * @param photosSupported this phone can add photos to a review
 * @param uploadProgress  0-100 while new photos upload
 * @param submitted       a review was just posted or saved, so the writing screen can close
 */
data class ReviewsState(
    val loading: Boolean = true,
    val reviews: List<ReviewResponse> = emptyList(),
    val error: String? = null,
    val myUid: String? = null,
    val reportedIds: Set<String> = emptySet(),
    val draft: ReviewDraft? = null,
    val submitting: Boolean = false,
    val uploadProgress: Int? = null,
    val photosSupported: Boolean = false,
    val message: String? = null,
    val submitted: Boolean = false,
    /** The user's own review while a moderator checks it (it mentions prices); only they see it */
    val myReviewInCheck: ReviewResponse? = null,
) {
    /** The backend allows one review per person per shop; one waiting for a check counts too */
    val myReview: ReviewResponse? get() = myUid?.let { uid -> reviews.firstOrNull { it.reviewerUid == uid } } ?: myReviewInCheck

    /** What the list shows: the user's review waiting for a check first, then everyone's */
    val shownReviews: List<ReviewResponse> get() = listOfNotNull(myReviewInCheck) + reviews

    /** How many reviews to show in the title; null while they first load, so it doesn't say 0 */
    val countToShow: Int? get() = if (loading && reviews.isEmpty()) null else reviews.size

    val averageRating: Double? get() = reviews.takeIf { it.isNotEmpty() }?.map { it.rating }?.average()

    /** How many reviews gave each number of stars: index 0 is 1 star, index 4 is 5 stars */
    val starCounts: List<Int> get() = (1..5).map { stars -> reviews.count { it.rating == stars } }

    val canWrite: Boolean get() = myUid != null && !loading && error == null

    fun isMine(review: ReviewResponse): Boolean = myUid != null && review.reviewerUid == myUid

    /** Signed-in users can report someone else's review, once */
    fun canReport(review: ReviewResponse): Boolean = myUid != null && !isMine(review) && review.id !in reportedIds

    /** Signed-in users can mark someone else's review helpful */
    fun canMarkHelpful(review: ReviewResponse): Boolean = myUid != null && !isMine(review)
}

/**
 * A shop's reviews, the same on Android and iOS.
 *
 * @param uploadPhotos uploads picked photos and returns their URLs, reporting progress 0-100; throws if any photo
 *                     fails (see [com.example.bhandara.photos.PhotoUploader]). Null where photos aren't supported.
 */
class ReviewsViewModel(
    private val shopId: String,
    private val reviewApi: ReviewApi,
    private val settings: AppSettings,
    private val currentUid: () -> String?,
    private val uploadPhotos: (suspend (photos: List<PickedPhoto>, onProgress: (Int) -> Unit) -> List<String>)?,
    /** Counts photo views for their reviewers; null where not counted */
    private val activityApi: ActivityApi? = null,
) : ViewModel() {

    /** Photos already counted on this visit, so swiping back and forth counts each once */
    private val photosCounted = mutableSetOf<String>()

    /** Reviews whose "helpful" is being sent, so a quick double tap doesn't send it twice */
    private val helpfulSending = mutableSetOf<String>()

    private val _state = MutableStateFlow(ReviewsState(photosSupported = uploadPhotos != null))
    val state: StateFlow<ReviewsState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    /**
     * Opens the editor: the user's own review if they have one, else a new one
     *
     * @param rating stars already picked for a new review (e.g. tapped on "No reviews yet"); 0 for none
     */
    fun startWriting(rating: Int = 0) {
        if (!_state.value.canWrite) return
        val mine = _state.value.myReview
        val draft = mine?.let {
            ReviewDraft(reviewId = it.id, rating = it.rating, comment = it.comment.orEmpty(), keptImageUrls = it.imageUrls.orEmpty())
        } ?: ReviewDraft(rating = rating.takeIf { it in 1..5 } ?: 0)
        _state.update { it.copy(draft = draft) }
    }

    fun setRating(rating: Int) {
        if (rating in 1..5) updateDraft { it.copy(rating = rating) }
    }

    fun setComment(comment: String) = updateDraft { it.copy(comment = comment.take(MAX_REVIEW_COMMENT)) }

    fun removeKeptPhoto(url: String) = updateDraft { it.copy(keptImageUrls = it.keptImageUrls - url) }

    /** Adds picked photos, up to the limit */
    fun addPhotos(photos: List<PickedPhoto>) {
        if (uploadPhotos == null) return
        updateDraft { it.copy(newPhotos = it.newPhotos + photos.take(MAX_REVIEW_PHOTOS - it.photoCount)) }
    }

    fun removeNewPhoto(photo: PickedPhoto) = updateDraft { it.copy(newPhotos = it.newPhotos - photo) }

    fun cancelDraft() {
        if (!_state.value.submitting) _state.update { it.copy(draft = null) }
    }

    /** Uploads any new photos, then posts the review (or the edit); the draft stays open if anything fails */
    fun submit() {
        val draft = _state.value.draft ?: return
        if (_state.value.submitting || !draft.canSubmit) return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            val newUrls = if (draft.newPhotos.isEmpty()) emptyList() else upload(draft.newPhotos) ?: run {
                _state.update { it.copy(submitting = false, uploadProgress = null, message = "Couldn't upload the photos. Please try again.") }
                return@launch
            }
            val request = ReviewRequest(
                shopId = shopId,
                rating = draft.rating,
                comment = draft.comment.trim().takeIf { it.isNotEmpty() },
                imageUrls = (draft.keptImageUrls + newUrls).takeIf { it.isNotEmpty() },
            )
            val result = if (draft.reviewId == null) reviewApi.createReview(request) else reviewApi.updateReview(draft.reviewId, request)
            when (result) {
                is ApiResult.Success -> {
                    val thanks = when {
                        result.value.status == REVIEW_IN_REVIEW -> "Thanks! Reviews with prices are checked before going live, usually within 24 hours."
                        draft.reviewId == null -> "Thanks for your review!"
                        else -> "Review updated"
                    }
                    _state.update { it.copy(draft = null, submitting = false, uploadProgress = null, message = thanks, submitted = true) }
                    load()
                }
                is ApiResult.Failure -> {
                    // A conflict means the user already has a review (e.g. written on another phone): show it to edit
                    val conflict = result.error.kind == ApiError.Kind.CONFLICT
                    _state.update {
                        it.copy(
                            draft = if (conflict) null else it.draft,
                            submitting = false,
                            uploadProgress = null,
                            message = result.error.message,
                        )
                    }
                    if (conflict) load()
                }
            }
        }
    }

    /** Deletes the user's own review (the screen asks first) */
    fun delete(reviewId: String) {
        if (_state.value.myReview?.id != reviewId) return
        viewModelScope.launch {
            when (val result = reviewApi.deleteReview(reviewId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(message = "Review deleted") }
                    load()
                }
                is ApiResult.Failure -> _state.update { it.copy(message = result.error.message) }
            }
        }
    }

    /**
     * Marks someone else's review helpful, or takes it back. The count changes at once and goes back if sending
     * fails.
     */
    /** The user is looking at a review's photo (full screen) */
    fun onPhotoShown(reviewId: String, photoUrl: String) {
        val api = activityApi ?: return
        if (!photosCounted.add("$reviewId|$photoUrl")) return
        viewModelScope.launch { api.reviewPhoto(reviewId, photoUrl) }
    }

    fun toggleHelpful(reviewId: String) {
        val review = _state.value.reviews.firstOrNull { it.id == reviewId } ?: return
        if (!_state.value.canMarkHelpful(review) || !helpfulSending.add(reviewId)) return
        val helpful = !review.helpfulByMe
        replaceReview(review.copy(helpfulByMe = helpful, helpfulCount = (review.helpfulCount + if (helpful) 1 else -1).coerceAtLeast(0)))
        viewModelScope.launch {
            val result = if (helpful) reviewApi.markHelpful(reviewId) else reviewApi.unmarkHelpful(reviewId)
            helpfulSending.remove(reviewId)
            when (result) {
                is ApiResult.Success -> replaceReview(result.value)
                is ApiResult.Failure -> {
                    replaceReview(review)
                    _state.update { it.copy(message = result.error.message) }
                }
            }
        }
    }

    private fun replaceReview(review: ReviewResponse) =
        _state.update { state -> state.copy(reviews = state.reviews.map { if (it.id == review.id) review else it }) }

    /** Flags someone else's review for a moderator, with what the user said (optional); remembered once it's sent */
    fun report(reviewId: String, note: String = "") {
        val review = _state.value.reviews.firstOrNull { it.id == reviewId } ?: return
        if (!_state.value.canReport(review)) return
        viewModelScope.launch {
            val details = note.trim().takeIf { it.isNotEmpty() }?.let { ReportDetails(note = it) }
            when (val result = reviewApi.reportReview(reviewId, details)) {
                is ApiResult.Success -> {
                    settings.markReportedReview(reviewId)
                    _state.update {
                        it.copy(reportedIds = it.reportedIds + reviewId, message = "Thanks for reporting. A moderator will take a look.")
                    }
                }
                is ApiResult.Failure -> _state.update { it.copy(message = result.error.message) }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    private fun updateDraft(change: (ReviewDraft) -> ReviewDraft) {
        if (_state.value.submitting) return
        _state.update { state -> state.copy(draft = state.draft?.let(change)) }
    }

    /** The uploaded photos' URLs, or null if any failed */
    private suspend fun upload(photos: List<PickedPhoto>): List<String>? {
        val uploader = uploadPhotos ?: return null
        _state.update { it.copy(uploadProgress = 0) }
        return try {
            uploader(photos) { progress -> _state.update { it.copy(uploadProgress = progress) } }
                .takeIf { it.size == photos.size }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val uid = currentUid()
            when (val result = reviewApi.getReviews(shopId)) {
                is ApiResult.Success -> {
                    val reviews = result.value.filter { it.isActive != false }
                    // The user's own review isn't on the page while a moderator checks it, so find it in theirs
                    val inCheck = if (uid == null || reviews.any { it.reviewerUid == uid }) null else
                        reviewApi.getMyReviews().getOrNull()?.firstOrNull { it.shopId == shopId && it.status == REVIEW_IN_REVIEW }
                    _state.update {
                        it.copy(
                            loading = false,
                            reviews = reviews,
                            myUid = uid,
                            myReviewInCheck = inCheck,
                            reportedIds = reviews.map { review -> review.id }.filter(settings::hasReportedReview).toSet(),
                        )
                    }
                }
                is ApiResult.Failure -> _state.update { it.copy(loading = false, myUid = uid, error = result.error.message) }
            }
        }
    }
}

/** A review that mentions prices, waiting for a moderator before customers see it */
const val REVIEW_IN_REVIEW = "IN_REVIEW"

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "3 Oct 2026" for the backend's "2026-10-03T16:42:24"; null when missing or unreadable */
fun formatReviewDate(timestamp: String?): String? {
    val parts = timestamp?.take(10)?.split("-")?.takeIf { it.size == 3 } ?: return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
    val day = parts[2].toIntOrNull()?.takeIf { it in 1..31 } ?: return null
    return "$day ${MONTHS[month - 1]} $year"
}
