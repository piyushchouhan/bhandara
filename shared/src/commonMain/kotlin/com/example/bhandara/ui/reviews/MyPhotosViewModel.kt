package com.example.bhandara.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.ReviewRequest
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ReviewApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One of the user's photos, in the review it belongs to */
data class MyPhoto(val url: String, val review: ReviewResponse) {
    /** How many people opened it (each once a day) */
    val views: Long get() = review.photoViews?.get(url) ?: 0L
}

/** A shop's photos from the user's review of it, in the order they were added */
data class PhotoGroup(val review: ReviewResponse, val photos: List<MyPhoto>)

/**
 * My photos (Profile → My activity): the photos in the user's reviews, grouped by shop; select several to delete
 *
 * @param error why the photos couldn't be loaded, ready to show
 * @param selecting select mode is on; [selected] holds the picked photos' urls
 * @param deletedCount photos just deleted (the screen says so); cleared with onMessageShown
 * @param message a one-off note (e.g. why deleting failed); cleared with onMessageShown
 */
data class MyPhotosState(
    val loading: Boolean = true,
    val error: String? = null,
    val reviews: List<ReviewResponse> = emptyList(),
    val selecting: Boolean = false,
    val selected: Set<String> = emptySet(),
    val deleting: Boolean = false,
    val deletedCount: Int = 0,
    val message: String? = null,
) {
    /** Reviews still shown on their shop's page that have photos, newest first */
    val groups: List<PhotoGroup>
        get() = reviews
            .filter { !it.isRemoved && !it.imageUrls.isNullOrEmpty() }
            .map { review -> PhotoGroup(review, review.imageUrls.orEmpty().map { MyPhoto(it, review) }) }

    val photos: List<MyPhoto> get() = groups.flatMap { it.photos }

    /** Shops (or carts) with at least one of the user's photos */
    val placeCount: Int get() = groups.size

    val selectedPhotos: List<MyPhoto> get() = photos.filter { it.url in selected }

    /** How many times the shown photos were opened by others (each person once a day) */
    val viewCount: Long get() = photos.sumOf { it.views }
}

class MyPhotosViewModel(private val reviewApi: ReviewApi) : ViewModel() {

    private val _state = MutableStateFlow(MyPhotosState())
    val state: StateFlow<MyPhotosState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = it.reviews.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = reviewApi.getMyReviews()) {
                is ApiResult.Success -> _state.update { state ->
                    val urls = result.value.flatMap { it.imageUrls.orEmpty() }.toSet()
                    state.copy(loading = false, reviews = result.value, selected = state.selected intersect urls)
                }
                is ApiResult.Failure -> _state.update {
                    if (it.reviews.isEmpty()) it.copy(loading = false, error = result.error.message)
                    else it.copy(loading = false, message = result.error.message)
                }
            }
        }
    }

    fun startSelecting() = _state.update { it.copy(selecting = true, selected = emptySet()) }

    fun stopSelecting() = _state.update { it.copy(selecting = false, selected = emptySet()) }

    fun toggle(url: String) = _state.update {
        it.copy(selected = if (url in it.selected) it.selected - url else it.selected + url)
    }

    fun selectAll() = _state.update { state -> state.copy(selected = state.photos.map { it.url }.toSet()) }

    /** Deletes the selected photos */
    fun deleteSelected() = delete(_state.value.selected)

    /**
     * Takes these photos out of their reviews, keeping each review's stars, text and other photos. Reviews are
     * updated one by one; the ones that worked stay deleted even if a later one fails.
     */
    fun delete(urls: Set<String>) {
        if (urls.isEmpty() || _state.value.deleting) return
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            var deleted = 0
            var failure: String? = null
            val affected = _state.value.reviews.filter { review -> review.imageUrls.orEmpty().any { it in urls } }
            for (review in affected) {
                val remaining = review.imageUrls.orEmpty().filterNot { it in urls }
                val request = ReviewRequest(rating = review.rating, comment = review.comment, imageUrls = remaining)
                when (val result = reviewApi.updateReview(review.id, request)) {
                    is ApiResult.Success -> {
                        deleted += review.imageUrls.orEmpty().size - remaining.size
                        _state.update { state ->
                            state.copy(reviews = state.reviews.map { if (it.id == review.id) it.copy(imageUrls = remaining) else it })
                        }
                    }
                    is ApiResult.Failure -> failure = failure ?: result.error.message
                }
            }
            _state.update { state ->
                val left = state.selected - urls.filterTo(mutableSetOf()) { url -> state.photos.none { it.url == url } }
                state.copy(
                    deleting = false,
                    deletedCount = deleted,
                    message = failure,
                    selected = left,
                    // Done selecting once everything picked is gone
                    selecting = state.selecting && left.isNotEmpty(),
                )
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null, deletedCount = 0) }
}
