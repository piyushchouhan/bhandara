package com.example.bhandara.data.saved

import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import com.example.bhandara.platform.LocalAppDependencies
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import com.example.bhandara.auth.UserAccount
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.SavedApi
import com.example.bhandara.data.network.SavedPlaceDto
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.data.settings.SavedChange
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps the places saved on this phone and the ones kept with the account the same. The phone's list works
 * offline; changes wait in a queue and go out with the next sync, which brings back everything the account has.
 *
 * When the account changes: a guest's places go with them into the account they sign in to, but one account's
 * places never go to another (signing out, or switching accounts, starts from what the new account has).
 */
class SavedPlacesSync(
    private val settings: AppSettings,
    private val api: SavedApi,
    private val account: () -> UserAccount?,
) {
    private val lock = Mutex()

    /** Syncs now; false when nobody is signed in or the backend can't be reached (the queue waits for next time) */
    suspend fun sync(): Boolean = lock.withLock {
        val who = account() ?: return false
        val last = settings.savedSyncedUid
        if (last != who.uid) {
            if (last != null && !settings.savedSyncedAsGuest) settings.clearSavedPlaces() else settings.queueAllSaved()
        }
        val sent = settings.pendingSavedChanges()
        val result = api.sync(
            saved = sent.filter { it.saved }.map { SavedPlaceDto(it.kind, it.id, it.savedAt.takeIf { at -> at > 0 }) },
            removed = sent.filterNot { it.saved }.map { SavedPlaceDto(it.kind, it.id) },
        )
        if (result !is ApiResult.Success) return false
        settings.applySyncedSaved(result.value.map { SavedChange(it.kind, it.id, saved = true, savedAt = it.savedAt ?: 0L) }, sent)
        settings.savedSyncedUid = who.uid
        settings.savedSyncedAsGuest = who.isAnonymous
        true
    }
}

/**
 * Keeps saved places synced while the app runs: at start and each time it comes back, a moment after each save or
 * unsave, and when the user signs in or out (from whichever screen)
 */
@OptIn(FlowPreview::class)
@Composable
fun KeepSavedPlacesSynced() {
    val dependencies = LocalAppDependencies.current
    val sync = remember { SavedPlacesSync(dependencies.settings, SavedApi(dependencies.apiClient)) { dependencies.auth.account() } }
    val nudges = remember { MutableStateFlow(0) }
    LifecycleResumeEffect(Unit) {
        nudges.value++
        onPauseOrDispose {}
    }
    LaunchedEffect(Unit) {
        // Sign-in happens in platform code, so the account is looked at now and then; nothing goes out unless it changed
        var seen = dependencies.auth.currentUid
        while (true) {
            delay(2_000)
            val uid = dependencies.auth.currentUid
            if (uid != seen) {
                seen = uid
                nudges.value++
            }
        }
    }
    LaunchedEffect(sync) {
        combine(dependencies.settings.savedChanges, nudges) { changes, nudge -> changes to nudge }
            .debounce(1_000)
            .collectLatest { if (dependencies.auth.currentUid != null) sync.sync() }
    }
}
