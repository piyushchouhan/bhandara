package com.example.bhandara.ui.feasts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FeastApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The tabs of Feasts I posted */
enum class MyFeastsTab { UPCOMING, PAST, TAKEN_DOWN }

/** What the organizer just did, for the screen to confirm */
enum class MyFeastsDone { ENDED, TAKEN_DOWN }

/**
 * Feasts I posted (Profile → My activity): the user's feasts by status, and what they can do with them
 *
 * @param tab the tab the user picked; null until they pick one ([activeTab] picks a sensible one)
 * @param busyId a feast being ended or taken down
 * @param error why the feasts couldn't be loaded, ready to show
 * @param message a one-off note (e.g. why ending failed); cleared with onMessageShown
 */
data class MyFeastsState(
    val loading: Boolean = true,
    val error: String? = null,
    val feasts: List<FeastResponse> = emptyList(),
    val tab: MyFeastsTab? = null,
    val busyId: String? = null,
    val message: String? = null,
    val done: MyFeastsDone? = null,
) {
    /** Not over yet, soonest first */
    val upcoming: List<FeastResponse> get() = feasts.filter { it.status == STATUS_UPCOMING }.reversed()

    /** Ended (on time or early), latest first */
    val past: List<FeastResponse> get() = feasts.filter { it.status == STATUS_PAST }

    /** Hidden from the map after reports */
    val takenDown: List<FeastResponse> get() = feasts.filter { it.status == STATUS_TAKEN_DOWN }

    /** The picked tab, or the first one with something in it */
    val activeTab: MyFeastsTab
        get() = tab ?: when {
            upcoming.isNotEmpty() -> MyFeastsTab.UPCOMING
            past.isNotEmpty() -> MyFeastsTab.PAST
            takenDown.isNotEmpty() -> MyFeastsTab.TAKEN_DOWN
            else -> MyFeastsTab.UPCOMING
        }

    companion object {
        const val STATUS_UPCOMING = "UPCOMING"
        const val STATUS_PAST = "PAST"
        const val STATUS_TAKEN_DOWN = "TAKEN_DOWN"
    }
}

class MyFeastsViewModel(private val feastApi: FeastApi) : ViewModel() {

    private val _state = MutableStateFlow(MyFeastsState())
    val state: StateFlow<MyFeastsState> = _state.asStateFlow()

    init {
        load()
    }

    /** Loads (or reloads, e.g. after editing a feast) the user's feasts */
    fun load() {
        _state.update { it.copy(loading = it.feasts.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = feastApi.getMyFeasts()) {
                is ApiResult.Success -> _state.update { it.copy(loading = false, feasts = result.value) }
                is ApiResult.Failure -> _state.update {
                    if (it.feasts.isEmpty()) it.copy(loading = false, error = result.error.message)
                    else it.copy(loading = false, message = result.error.message)
                }
            }
        }
    }

    fun onTab(tab: MyFeastsTab) = _state.update { it.copy(tab = tab) }

    /** The food ran out: the feast leaves the map now and moves to Past */
    fun endEarly(feastId: String) {
        if (_state.value.busyId != null) return
        _state.update { it.copy(busyId = feastId) }
        viewModelScope.launch {
            when (val result = feastApi.endFeastEarly(feastId)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(
                        busyId = null,
                        done = MyFeastsDone.ENDED,
                        feasts = state.feasts.map { if (it.id == feastId) result.value else it },
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(busyId = null, message = result.error.message) }
            }
        }
    }

    /** The feast leaves the map and this list for good */
    fun takeDown(feastId: String) {
        if (_state.value.busyId != null) return
        _state.update { it.copy(busyId = feastId) }
        viewModelScope.launch {
            when (val result = feastApi.takeDownFeast(feastId)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(busyId = null, done = MyFeastsDone.TAKEN_DOWN, feasts = state.feasts.filterNot { it.id == feastId })
                }
                is ApiResult.Failure -> _state.update { it.copy(busyId = null, message = result.error.message) }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null, done = null) }
}
