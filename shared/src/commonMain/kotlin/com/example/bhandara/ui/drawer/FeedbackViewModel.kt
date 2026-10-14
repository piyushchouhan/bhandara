package com.example.bhandara.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.SupportApi
import com.example.bhandara.photos.PickedPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Reporting a problem or suggesting a feature */
enum class FeedbackKind { BUG, FEATURE }

/**
 * What a problem is about, picked as a chip; sent in English at the start of the report so the team can sort them
 * (the backend keeps one text per report)
 */
enum class FeedbackTopic(val tag: String) { MAP("Map"), SHOP("Shop details"), FEASTS("Feasts"), LOGIN("Login"), OTHER("Other") }

/** Longest description the backend accepts */
const val MAX_FEEDBACK_LENGTH = 2000

/**
 * @param topic      what a problem is about, if the user picked one
 * @param screenshot a screenshot to send with a problem, uploaded when it's sent
 */
data class FeedbackState(
    val text: String = "",
    val topic: FeedbackTopic? = null,
    val screenshot: PickedPhoto? = null,
    val sending: Boolean = false,
    val sent: Boolean = false,
    /** Why it couldn't be sent, ready to show */
    val error: String? = null,
) {
    val canSend: Boolean get() = text.isNotBlank() && !sending && !sent
}

/**
 * "Report an issue" / "Suggest a feature", shared by Android and iOS
 *
 * @param uploadPhotos uploads a screenshot and returns its link (see [com.example.bhandara.photos.PhotoUploader]);
 *                     null where screenshots aren't supported
 */
class FeedbackViewModel(
    private val kind: FeedbackKind,
    private val supportApi: SupportApi,
    private val uploadPhotos: (suspend (photos: List<PickedPhoto>, onProgress: (Int) -> Unit) -> List<String>)? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackState())
    val state: StateFlow<FeedbackState> = _state.asStateFlow()

    fun onTextChange(text: String) {
        if (!_state.value.sending) _state.update { it.copy(text = text.take(MAX_FEEDBACK_LENGTH), error = null) }
    }

    /** Picks a topic, or clears it when it's tapped again */
    fun onTopicClick(topic: FeedbackTopic) {
        if (!_state.value.sending) _state.update { it.copy(topic = if (it.topic == topic) null else topic) }
    }

    fun onScreenshot(photo: PickedPhoto?) {
        if (!_state.value.sending && uploadPhotos != null) _state.update { it.copy(screenshot = photo, error = null) }
    }

    fun send() {
        val state = _state.value
        if (!state.canSend) return
        _state.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val link = state.screenshot?.let { photo ->
                upload(photo) ?: run {
                    _state.update { it.copy(sending = false, error = "Couldn't upload the screenshot. Please try again.") }
                    return@launch
                }
            }
            val text = feedbackText(state.text, state.topic, link)
            val result = when (kind) {
                FeedbackKind.BUG -> supportApi.reportBug(text)
                FeedbackKind.FEATURE -> supportApi.suggestFeature(text)
            }
            when (result) {
                is ApiResult.Success -> _state.update { it.copy(sending = false, sent = true) }
                is ApiResult.Failure -> _state.update { it.copy(sending = false, error = result.error.message) }
            }
        }
    }

    private suspend fun upload(photo: PickedPhoto): String? = try {
        uploadPhotos?.invoke(listOf(photo)) {}?.firstOrNull()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

/**
 * The text sent to the backend: "[Topic] description", then the screenshot's link on its own line; the description
 * is shortened if needed so the whole stays within the backend's limit
 */
fun feedbackText(description: String, topic: FeedbackTopic?, screenshotLink: String?): String {
    val prefix = topic?.let { "[${it.tag}] " }.orEmpty()
    val suffix = screenshotLink?.let { "\n\nScreenshot: $it" }.orEmpty()
    return prefix + description.trim().take(MAX_FEEDBACK_LENGTH - prefix.length - suffix.length) + suffix
}
