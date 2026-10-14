package com.example.bhandara.ui.privacy

import com.example.bhandara.navigation.Route
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.auth.UserAccount
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.models.api.MyReport
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.models.api.ShopClaimResponse
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.network.ReportApi
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** The pages of Privacy and data (design 1a–1e) */
enum class PrivacyPage { MAIN, LOCATION, DOWNLOAD, READY, DELETE }

/** The page a Privacy and data route opens on */
fun privacyStart(route: Route.Privacy): PrivacyPage = when {
    route.startAtDelete -> PrivacyPage.DELETE
    route.startAtLocation -> PrivacyPage.LOCATION
    else -> PrivacyPage.MAIN
}

/** What can go in the user's data file */
enum class DataPart { ACCOUNT, REVIEWS, PHOTOS, FEASTS, SAVED, REPORTS, CLAIMS }

/** The user's data as the backend has it, fetched once for the counts, the file and the delete page */
data class UserData(
    val reviews: List<ReviewResponse> = emptyList(),
    val feasts: List<FeastResponse> = emptyList(),
    val reports: List<MyReport> = emptyList(),
    val claims: List<ShopClaimResponse> = emptyList(),
) {
    val photos: List<String> get() = reviews.flatMap { it.imageUrls.orEmpty() }
}

/**
 * Privacy and data (Profile → Account): what the app keeps and shows, the user's data as a file, and deleting the
 * account
 *
 * @param data null until fetched; [dataError] says why it couldn't be
 * @param file the data file just made, to share again
 * @param deleted the account was deleted and the user signed out; the screen goes home
 */
data class PrivacyState(
    val page: PrivacyPage = PrivacyPage.MAIN,
    val data: UserData? = null,
    val loadingData: Boolean = false,
    val dataError: String? = null,
    val parts: Set<DataPart> = setOf(DataPart.ACCOUNT, DataPart.REVIEWS, DataPart.PHOTOS, DataPart.FEASTS),
    val file: String? = null,
    val deleteText: String = "",
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val message: String? = null,
) {
    /** Deleting is only offered once DELETE is typed */
    val canDelete: Boolean get() = deleteText.trim().equals(DELETE_WORD, ignoreCase = true) && !deleting

    companion object {
        const val DELETE_WORD = "DELETE"
    }
}

/** The file the user downloads; sections they left out are absent */
@Serializable
data class DataExport(
    val exportedAt: String,
    val account: ExportAccount? = null,
    val reviews: List<ReviewResponse>? = null,
    val photos: List<String>? = null,
    val feastsPosted: List<FeastResponse>? = null,
    val savedPlaces: ExportSaved? = null,
    val reports: List<MyReport>? = null,
    val shopClaims: List<ShopClaimResponse>? = null,
)

@Serializable
data class ExportAccount(val uid: String?, val name: String?, val email: String?, val appLanguage: String)

@Serializable
data class ExportSaved(val shopIds: List<String>, val feastIds: List<String>)

class PrivacyViewModel(
    private val reviewApi: ReviewApi,
    private val feastApi: FeastApi,
    private val reportApi: ReportApi,
    private val claimApi: ClaimApi,
    private val userApi: UserApi,
    private val settings: AppSettings,
    private val account: () -> UserAccount?,
    /** Signs out of the account on this platform, then back in as a guest */
    private val signOut: suspend () -> Unit,
    startAt: PrivacyPage = PrivacyPage.MAIN,
) : ViewModel() {

    private val _state = MutableStateFlow(PrivacyState(page = startAt))
    val state: StateFlow<PrivacyState> = _state.asStateFlow()

    private val json = Json { prettyPrint = true; explicitNulls = false; encodeDefaults = true }

    init {
        if (startAt != PrivacyPage.MAIN) loadData()
    }

    fun open(page: PrivacyPage) {
        _state.update { it.copy(page = page) }
        if ((page == PrivacyPage.DOWNLOAD || page == PrivacyPage.DELETE) && _state.value.data == null) loadData()
    }

    /** Back one page; false on the main page, so the screen closes */
    fun back(): Boolean {
        val page = _state.value.page
        if (page == PrivacyPage.MAIN || _state.value.deleting) return page != PrivacyPage.MAIN
        _state.update { it.copy(page = if (page == PrivacyPage.READY) PrivacyPage.DOWNLOAD else PrivacyPage.MAIN) }
        return true
    }

    /** Fetches the user's reviews, feasts, reports and claims at once; any failure fails it (to retry) */
    fun loadData() {
        if (_state.value.loadingData) return
        _state.update { it.copy(loadingData = true, dataError = null) }
        viewModelScope.launch {
            val reviews = async { reviewApi.getMyReviews() }
            val feasts = async { feastApi.getMyFeasts() }
            val reports = async { reportApi.getMyReports() }
            val claims = async { claimApi.myClaims() }
            val results = listOf(reviews.await(), feasts.await(), reports.await(), claims.await())
            val failure = results.filterIsInstance<ApiResult.Failure>().firstOrNull()
            _state.update {
                if (failure != null) {
                    it.copy(loadingData = false, dataError = failure.error.message)
                } else {
                    it.copy(
                        loadingData = false,
                        data = UserData(
                            reviews = (reviews.await() as ApiResult.Success).value,
                            feasts = (feasts.await() as ApiResult.Success).value,
                            reports = (reports.await() as ApiResult.Success).value,
                            claims = (claims.await() as ApiResult.Success).value,
                        ),
                    )
                }
            }
        }
    }

    fun toggle(part: DataPart) = _state.update { it.copy(parts = if (part in it.parts) it.parts - part else it.parts + part) }

    /** Makes the data file with the chosen parts; the screen shares it */
    fun makeFile(appLanguage: String) {
        val state = _state.value
        val data = state.data ?: return
        if (state.parts.isEmpty()) return
        val parts = state.parts
        val who = account()
        val export = DataExport(
            exportedAt = Clock.System.now().toString(),
            account = if (DataPart.ACCOUNT in parts) ExportAccount(who?.uid, who?.name, who?.email, appLanguage) else null,
            reviews = data.reviews.takeIf { DataPart.REVIEWS in parts },
            photos = data.photos.takeIf { DataPart.PHOTOS in parts },
            feastsPosted = data.feasts.takeIf { DataPart.FEASTS in parts },
            savedPlaces = if (DataPart.SAVED in parts) ExportSaved(settings.savedShopIds, settings.savedFeastIds) else null,
            reports = data.reports.takeIf { DataPart.REPORTS in parts },
            shopClaims = data.claims.takeIf { DataPart.CLAIMS in parts },
        )
        _state.update { it.copy(file = json.encodeToString(export), page = PrivacyPage.READY) }
    }

    fun onDeleteTextChange(text: String) = _state.update { it.copy(deleteText = text.take(20)) }

    /** Deletes the account and data, forgets what's kept on this phone, and signs out */
    fun deleteAccount() {
        if (!_state.value.canDelete) return
        _state.update { it.copy(deleting = true, message = null) }
        viewModelScope.launch {
            when (val result = userApi.deleteMyAccount()) {
                is ApiResult.Success -> {
                    settings.clearSavedPlaces()
                    try {
                        signOut()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // The account is gone either way; the next sign-in starts fresh
                    }
                    _state.update { it.copy(deleting = false, deleted = true) }
                }
                is ApiResult.Failure -> _state.update { it.copy(deleting = false, message = result.error.message) }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }
}
