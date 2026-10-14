package com.example.bhandara.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.MyReport
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ReportApi
import com.example.bhandara.data.models.api.ReportDetails
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.components.PHOTOS_FAILED
import com.example.bhandara.ui.components.uploadReportPhotos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** All reports, the ones still being checked, or the ones decided (fixed or not) */
enum class ReportsFilter { ALL, IN_REVIEW, CLOSED }

/**
 * My reports (Profile → My activity): what the user reported and where each report stands
 *
 * @param openKey the report open in detail ([key]); null on the list
 * @param withdrawn a report was just withdrawn (the screen says so); cleared with onMessageShown
 * @param message a one-off note (e.g. why withdrawing failed); cleared with onMessageShown
 */
data class MyReportsState(
    val loading: Boolean = true,
    val error: String? = null,
    val reports: List<MyReport> = emptyList(),
    val filter: ReportsFilter = ReportsFilter.ALL,
    val openKey: String? = null,
    val withdrawing: Boolean = false,
    val withdrawn: Boolean = false,
    val message: String? = null,
    /** "Add more info" is being sent */
    val addingInfo: Boolean = false,
    /** More info was just added (the sheet closes, the screen says so); cleared with onMessageShown */
    val infoAdded: Boolean = false,
) {
    val inReviewCount: Int get() = reports.count { it.isInReview }
    val closedCount: Int get() = reports.size - inReviewCount
    val fixedCount: Int get() = reports.count { it.status == STATUS_FIXED }

    fun shown(): List<MyReport> = when (filter) {
        ReportsFilter.ALL -> reports
        ReportsFilter.IN_REVIEW -> reports.filter { it.isInReview }
        ReportsFilter.CLOSED -> reports.filterNot { it.isInReview }
    }

    val open: MyReport? get() = reports.firstOrNull { it.key == openKey }

    companion object {
        const val STATUS_IN_REVIEW = "IN_REVIEW"
        const val STATUS_FIXED = "FIXED"
        const val STATUS_NO_ACTION = "NO_ACTION"
    }
}

/** Unique across kinds (a shop report and a review report can share an id) */
val MyReport.key: String get() = "$kind:$reportId"

val MyReport.isInReview: Boolean get() = status == MyReportsState.STATUS_IN_REVIEW

/**
 * @param uploadPhotos uploads photos added with "Add more info" and returns their URLs (throws if any fails); null
 *                     where photos aren't supported
 */
class MyReportsViewModel(
    private val reportApi: ReportApi,
    private val uploadPhotos: (suspend (List<PickedPhoto>) -> List<String>)? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(MyReportsState())
    val state: StateFlow<MyReportsState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = it.reports.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = reportApi.getMyReports()) {
                is ApiResult.Success -> _state.update { it.copy(loading = false, reports = result.value) }
                is ApiResult.Failure -> _state.update {
                    if (it.reports.isEmpty()) it.copy(loading = false, error = result.error.message)
                    else it.copy(loading = false, message = result.error.message)
                }
            }
        }
    }

    fun onFilter(filter: ReportsFilter) = _state.update { it.copy(filter = filter) }

    fun open(report: MyReport) = _state.update { it.copy(openKey = report.key) }

    fun close() = _state.update { it.copy(openKey = null) }

    /** Takes back a report still in review; back on the list once it's gone */
    fun withdraw(report: MyReport) {
        if (_state.value.withdrawing || !report.isInReview) return
        _state.update { it.copy(withdrawing = true) }
        viewModelScope.launch {
            when (val result = reportApi.withdraw(report)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(
                        withdrawing = false,
                        withdrawn = true,
                        reports = state.reports.filterNot { it.key == report.key },
                        openKey = state.openKey.takeIf { it != report.key },
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(withdrawing = false, message = result.error.message) }
            }
        }
    }

    /** "Add more info": a note, photos or both on a report still in review; the report shows them once sent */
    fun addInfo(report: MyReport, note: String, photos: List<PickedPhoto>) {
        val text = note.trim()
        if (_state.value.addingInfo || !report.isInReview || (text.isEmpty() && photos.isEmpty())) return
        _state.update { it.copy(addingInfo = true) }
        viewModelScope.launch {
            val photoUrls = uploadReportPhotos(uploadPhotos, photos) ?: run {
                _state.update { it.copy(addingInfo = false, message = PHOTOS_FAILED) }
                return@launch
            }
            val details = ReportDetails(note = text.ifEmpty { null }, photoUrls = photoUrls.ifEmpty { null })
            when (val result = reportApi.addInfo(report, details)) {
                is ApiResult.Success -> _state.update { state ->
                    state.copy(
                        addingInfo = false,
                        infoAdded = true,
                        reports = state.reports.map { if (it.key == report.key) result.value else it },
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(addingInfo = false, message = result.error.message) }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null, withdrawn = false, infoAdded = false) }
}
