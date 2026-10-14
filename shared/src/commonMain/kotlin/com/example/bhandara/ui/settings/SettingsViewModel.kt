package com.example.bhandara.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppPreferences
import com.example.bhandara.data.settings.NotificationKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The pages of Settings (design 1b–1e); location access opens Privacy and data */
enum class SettingsPage { MAIN, LANGUAGE, NOTIFICATIONS, APPEARANCE }

/**
 * @param query what's typed in "Search settings"
 * @param saving notification settings on their way to the backend
 * @param notificationsFailed a change didn't reach the backend, so it was undone; the screen says so once
 */
data class SettingsState(
    val page: SettingsPage = SettingsPage.MAIN,
    val query: String = "",
    val saving: Set<NotificationKind> = emptySet(),
    val notificationsFailed: Boolean = false,
)

/**
 * Settings, from the side menu. Most settings live on the phone ([AppPreferences]) and apply at once; the
 * notification settings are also kept by the backend, which sends the notifications.
 */
class SettingsViewModel(
    private val preferences: AppPreferences,
    private val userApi: UserApi,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun open(page: SettingsPage) {
        _state.update { it.copy(page = page) }
        if (page == SettingsPage.NOTIFICATIONS) refreshNotifications()
    }

    /**
     * Takes the account's notification settings (e.g. changed on another phone); settings being changed right now
     * keep the user's choice. Fails quietly: the phone's copy stays.
     */
    fun refreshNotifications() {
        viewModelScope.launch {
            val result = userApi.getNotifications() as? ApiResult.Success ?: return@launch
            val account = result.value
            val busy = _state.value.saving
            NotificationKind.entries.filterNot { it in busy }.forEach { kind ->
                account.valueOf(kind)?.let { preferences.updateNotification(kind, it) }
            }
        }
    }

    /** Back one page; false on the main page, so the screen closes */
    fun back(): Boolean {
        if (_state.value.page == SettingsPage.MAIN) return false
        _state.update { it.copy(page = SettingsPage.MAIN) }
        return true
    }

    fun onQueryChange(text: String) = _state.update { it.copy(query = text.take(40)) }

    /** Switches "New feasts near me" (also on the profile) */
    fun setNotifyNearbyFeasts(on: Boolean) = setNotification(NotificationKind.NEARBY_FEASTS, on)

    /** Switches a notification setting at once and tells the backend; if it can't be told, the switch goes back */
    fun setNotification(kind: NotificationKind, on: Boolean) {
        if (kind in _state.value.saving || on == preferences.notification(kind)) return
        preferences.updateNotification(kind, on)
        _state.update { it.copy(saving = it.saving + kind, notificationsFailed = false) }
        viewModelScope.launch {
            val failed = userApi.setNotifications(settingsWith(kind, on)) is ApiResult.Failure
            if (failed) preferences.updateNotification(kind, !on)
            _state.update { it.copy(saving = it.saving - kind, notificationsFailed = failed || it.notificationsFailed) }
        }
    }

    fun onMessageShown() = _state.update { it.copy(notificationsFailed = false) }
}

/** Just this one setting, so the others stay as the backend has them */
private fun settingsWith(kind: NotificationKind, on: Boolean) = when (kind) {
    NotificationKind.NEARBY_FEASTS -> UserApi.NotificationSettings(nearbyFeasts = on)
    NotificationKind.SAVED_FEASTS -> UserApi.NotificationSettings(savedFeasts = on)
    NotificationKind.REPORT_UPDATES -> UserApi.NotificationSettings(reportUpdates = on)
    NotificationKind.SHOP_REVIEWS -> UserApi.NotificationSettings(shopReviews = on)
    NotificationKind.CLAIM_STATUS -> UserApi.NotificationSettings(claimStatus = on)
    NotificationKind.QUIET_HOURS -> UserApi.NotificationSettings(quietHours = on)
}

private fun UserApi.NotificationSettings.valueOf(kind: NotificationKind): Boolean? = when (kind) {
    NotificationKind.NEARBY_FEASTS -> nearbyFeasts
    NotificationKind.SAVED_FEASTS -> savedFeasts
    NotificationKind.REPORT_UPDATES -> reportUpdates
    NotificationKind.SHOP_REVIEWS -> shopReviews
    NotificationKind.CLAIM_STATUS -> claimStatus
    NotificationKind.QUIET_HOURS -> quietHours
}
