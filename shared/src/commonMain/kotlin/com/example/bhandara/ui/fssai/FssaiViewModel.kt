package com.example.bhandara.ui.fssai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.FssaiKind
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.FssaiApi
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PickedPdf
import com.example.bhandara.photos.PickedPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * An FSSAI licence being filled in (5a): the certificate's pages, and its number and expiry, read off it by AI
 * and checked by the owner.
 */
data class FssaiForm(
    val pages: List<PickedPhoto> = emptyList(),
    /** Digits only */
    val number: String = "",
    val validTill: LocalDate? = null,
) {
    val kind: String get() = if (number.startsWith("2")) FssaiKind.REGISTRATION else FssaiKind.LICENCE
}

/**
 * @param reading       the AI is reading the certificate
 * @param readFromPhoto the number or expiry were filled in from the certificate ("Check and fix if needed")
 * @param readProblem   the certificate couldn't be read (not a certificate, AI busy); the owner can type instead
 * @param done          sent for checking (or, while adding a shop, kept to send with it)
 */
data class FssaiState(
    val form: FssaiForm = FssaiForm(),
    val reading: Boolean = false,
    val readFromPhoto: Boolean = false,
    val readProblem: String? = null,
    val submitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

/**
 * The FSSAI licence screen (5a / 7a). While adding a shop ([shopId] null) "Submit for check" keeps the licence to
 * send once the shop is added; for an existing shop it sends it right away.
 *
 * @param today for checking the licence hasn't expired
 */
class FssaiViewModel(
    private val fssaiApi: FssaiApi,
    private val shopId: String?,
    initial: FssaiForm?,
    private val today: () -> LocalDate,
) : ViewModel() {

    private val _state = MutableStateFlow(FssaiState(form = initial ?: FssaiForm(), readFromPhoto = initial != null))
    val state: StateFlow<FssaiState> = _state.asStateFlow()

    private var readJob: Job? = null

    /** Adds pages (up to [MAX_PAGES]) and reads the certificate again with all of them */
    fun addPages(pages: List<PickedPhoto>) {
        if (pages.isEmpty() || _state.value.submitting) return
        _state.update { it.copy(form = it.form.copy(pages = (it.form.pages + pages).take(MAX_PAGES)), error = null) }
        readCertificate()
    }

    fun removePage(index: Int) {
        if (_state.value.submitting) return
        readJob?.cancel()
        _state.update { state ->
            val pages = state.form.pages.filterIndexed { i, _ -> i != index }
            state.copy(form = state.form.copy(pages = pages), reading = false, readProblem = null, readFromPhoto = state.readFromPhoto && pages.isNotEmpty())
        }
    }

    fun onNumberChange(text: String) = _state.update { it.copy(form = it.form.copy(number = text.filter(Char::isDigit).take(NUMBER_DIGITS)), error = null) }

    fun setValidTill(date: LocalDate) = _state.update { it.copy(form = it.form.copy(validTill = date), error = null) }

    /** What's still wrong, or null when it can be sent */
    fun problem(): String? {
        val form = _state.value.form
        return when {
            form.pages.isEmpty() -> "Add a photo or PDF of the certificate."
            form.number.length != NUMBER_DIGITS -> "The licence number has 14 digits."
            form.validTill == null -> "Add the date it's valid till."
            form.validTill < today() -> "This licence has expired."
            else -> null
        }
    }

    fun submit() {
        val state = _state.value
        if (state.submitting || state.done) return
        problem()?.let { problem -> return _state.update { it.copy(error = problem) } }
        val shopId = shopId ?: return _state.update { it.copy(done = true) }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val pages = compress(state.form.pages) ?: return@launch _state.update {
                it.copy(submitting = false, error = "Couldn't read the certificate. Take the photo again, or pick a PDF under 10 MB.")
            }
            when (val result = fssaiApi.submit(shopId, state.form.number, state.form.validTill.toString(), pages)) {
                is ApiResult.Success -> _state.update { it.copy(submitting = false, done = true) }
                is ApiResult.Failure -> _state.update { it.copy(submitting = false, error = result.error.message) }
            }
        }
    }

    /** Fills in what AI can read; what the owner already typed stays */
    private fun readCertificate() {
        readJob?.cancel()
        val pages = _state.value.form.pages
        _state.update { it.copy(reading = true, readProblem = null) }
        readJob = viewModelScope.launch {
            val compressed = compress(pages) ?: return@launch _state.update {
                it.copy(reading = false, readProblem = "Couldn't open the certificate. Take the photo again, or pick a PDF under 10 MB.")
            }
            when (val result = fssaiApi.read(compressed)) {
                is ApiResult.Success -> _state.update { state ->
                    val read = result.value
                    val number = read.licenceNumber?.takeIf { state.form.number.isEmpty() }
                    val validTill = read.validTill?.takeIf { state.form.validTill == null }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    state.copy(
                        form = state.form.copy(number = number ?: state.form.number, validTill = validTill ?: state.form.validTill),
                        reading = false,
                        readFromPhoto = state.readFromPhoto || number != null || validTill != null,
                        readProblem = if (read.licenceNumber == null && read.validTill == null) "Couldn't read the number. Type it in from the certificate." else null,
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(reading = false, readProblem = result.error.message) }
            }
        }
    }

    private suspend fun compress(pages: List<PickedPhoto>): List<CompressedPhoto>? {
        val compressed = mutableListOf<CompressedPhoto>()
        for (page in pages) {
            val one = try {
                page.compressed()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            compressed += one ?: return null
        }
        return compressed
    }

    companion object {
        const val MAX_PAGES = 2
        const val NUMBER_DIGITS = 14

        fun isPdf(page: PickedPhoto) = page is PickedPdf
    }
}
