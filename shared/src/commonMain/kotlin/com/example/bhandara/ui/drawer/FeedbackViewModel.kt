package com.example.bhandara.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.SupportApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Reporting a problem or suggesting a feature */
enum class FeedbackKind { BUG, FEATURE }

/** Longest description the backend accepts */
const val MAX_FEEDBACK_LENGTH = 2000

data class FeedbackState(
    val text: String = "",
    val sending: Boolean = false,
    val sent: Boolean = false,
    /** Why it couldn't be sent, ready to show */
    val error: String? = null,
) {
    val canSend: Boolean get() = text.isNotBlank() && !sending && !sent
}

/** The "Report an issue" / "Suggest a feature" dialog, shared by Android and iOS */
class FeedbackViewModel(private val kind: FeedbackKind, private val supportApi: SupportApi) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackState())
    val state: StateFlow<FeedbackState> = _state.asStateFlow()

    fun onTextChange(text: String) {
        if (!_state.value.sending) _state.update { it.copy(text = text.take(MAX_FEEDBACK_LENGTH), error = null) }
    }

    fun send() {
        if (!_state.value.canSend) return
        val text = _state.value.text.trim()
        _state.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
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
}
