package com.example.bhandara.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ReviewApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** Which of the user's reviews are listed */
enum class MyReviewsFilter { ALL, PUBLISHED, REMOVED }

/** The order they're listed in */
enum class MyReviewsSort { NEWEST, HIGHEST, LOWEST }

/**
 * My reviews (Profile → My activity): everything the user reviewed, with a summary, filters by status, and the
 * review's own actions
 *
 * @param error why the reviews couldn't be loaded, ready to show
 * @param message a one-off note (e.g. why deleting failed); cleared with onMessageShown
 * @param deleted a review was just deleted (the screen says so); cleared with onMessageShown
 */
data class MyReviewsState(
    val loading: Boolean = true,
    val error: String? = null,
    val reviews: List<ReviewResponse> = emptyList(),
    val filter: MyReviewsFilter = MyReviewsFilter.ALL,
    val sort: MyReviewsSort = MyReviewsSort.NEWEST,
    val deletingId: String? = null,
    val message: String? = null,
    val deleted: Boolean = false,
) {
    val removedCount: Int get() = reviews.count { it.isRemoved }
    val publishedCount: Int get() = reviews.size - removedCount

    /** The average of the stars the user gave; null with no reviews */
    val averageGiven: Double? get() = reviews.takeIf { it.isNotEmpty() }?.map { it.rating }?.average()

    /** The filter in use: "Removed" falls back to all once nothing is removed */
    val activeFilter: MyReviewsFilter
        get() = if (filter == MyReviewsFilter.REMOVED && removedCount == 0) MyReviewsFilter.ALL else filter

    /** The reviews to list, filtered and sorted (the backend's order, newest first, breaks ties) */
    fun shown(): List<ReviewResponse> {
        val filtered = when (activeFilter) {
            MyReviewsFilter.ALL -> reviews
            MyReviewsFilter.PUBLISHED -> reviews.filterNot { it.isRemoved }
            MyReviewsFilter.REMOVED -> reviews.filter { it.isRemoved }
        }
        return when (sort) {
            MyReviewsSort.NEWEST -> filtered
            MyReviewsSort.HIGHEST -> filtered.sortedByDescending { it.rating }
            MyReviewsSort.LOWEST -> filtered.sortedBy { it.rating }
        }
    }
}

/** Removed by a moderator: still listed for its author, but no longer on the shop's page */
val ReviewResponse.isRemoved: Boolean get() = removedByModerator == true

class MyReviewsViewModel(private val reviewApi: ReviewApi) : ViewModel() {

    private val _state = MutableStateFlow(MyReviewsState())
    val state: StateFlow<MyReviewsState> = _state.asStateFlow()

    init {
        load()
    }

    /** Loads (or reloads, e.g. after editing a review) the user's reviews */
    fun load() {
        _state.update { it.copy(loading = it.reviews.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = reviewApi.getMyReviews()) {
                is ApiResult.Success -> _state.update { it.copy(loading = false, reviews = result.value) }
                is ApiResult.Failure -> _state.update {
                    // With reviews already shown, a failed refresh keeps them and just says why
                    if (it.reviews.isEmpty()) it.copy(loading = false, error = result.error.message)
                    else it.copy(loading = false, message = result.error.message)
                }
            }
        }
    }

    fun onFilter(filter: MyReviewsFilter) = _state.update { it.copy(filter = filter) }

    fun onSort(sort: MyReviewsSort) = _state.update { it.copy(sort = sort) }

    fun delete(reviewId: String) {
        if (_state.value.deletingId != null) return
        _state.update { it.copy(deletingId = reviewId) }
        viewModelScope.launch {
            when (val result = reviewApi.deleteReview(reviewId)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(deletingId = null, deleted = true, reviews = state.reviews.filterNot { it.id == reviewId })
                }
                is ApiResult.Failure -> _state.update { it.copy(deletingId = null, message = result.error.message) }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null, deleted = false) }
}

/** How long ago a review was written, as the list says it */
sealed interface ReviewAge {
    data object Today : ReviewAge
    data object Yesterday : ReviewAge
    data class DaysAgo(val days: Int) : ReviewAge
    data class WeeksAgo(val weeks: Int) : ReviewAge

    /** Older than a month: the date itself, e.g. "3 Oct 2026" */
    data class On(val date: String) : ReviewAge
}

/** [ReviewAge] of the backend's "2026-10-03T16:42:24" on [today]; null when missing or unreadable */
fun reviewAge(timestamp: String?, today: LocalDate): ReviewAge? {
    val date = timestamp?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val ago = (today.toEpochDays() - date.toEpochDays()).toInt()
    return when {
        ago <= 0 -> ReviewAge.Today // today, or a server clock a little ahead
        ago == 1 -> ReviewAge.Yesterday
        ago < 7 -> ReviewAge.DaysAgo(ago)
        ago < 30 -> ReviewAge.WeeksAgo(ago / 7)
        else -> formatReviewDate(timestamp)?.let { ReviewAge.On(it) }
    }
}
