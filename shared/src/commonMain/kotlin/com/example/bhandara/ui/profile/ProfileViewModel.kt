package com.example.bhandara.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.auth.AuthService
import com.example.bhandara.auth.UserAccount
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How a sign-in the platform ran (e.g. Google's account picker) ended */
sealed interface SignInOutcome {
    data object SignedIn : SignInOutcome

    /** The user closed the picker; not an error */
    data object Cancelled : SignInOutcome

    data class Failed(val reason: String? = null) : SignInOutcome
}

data class ProfileState(
    val account: UserAccount? = null,
    val busy: Boolean = false,
    /** A one-time note (e.g. "Signed in"), cleared with onMessageShown() */
    val message: String? = null,
    val error: String? = null,
) {
    /** Not signed in with an account: the app uses an anonymous one */
    val isGuest: Boolean get() = account == null || account.isAnonymous

    /** The account's name as the title, and its email under it; without a name, the email is the title alone */
    val accountTitle: String? get() = account?.name ?: account?.email
    val accountSubtitle: String? get() = account?.email?.takeIf { account.name != null }
}

/**
 * The profile, shared by Android and iOS: who is signed in, and signing in with an account or out again.
 *
 * @param signOut signs out of the account on this platform (e.g. Firebase and Google); null where signing in
 *                with an account isn't available yet
 */
class ProfileViewModel(
    private val auth: AuthService,
    private val signOut: (suspend () -> Unit)?,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(account = auth.account()))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    /** The platform's sign-in screen is about to open */
    fun onSignInStarted() = _state.update { it.copy(busy = true, message = null, error = null) }

    fun onSignInResult(outcome: SignInOutcome) {
        _state.update {
            when (outcome) {
                SignInOutcome.SignedIn -> it.copy(busy = false, account = auth.account(), message = "Signed in successfully!")
                SignInOutcome.Cancelled -> it.copy(busy = false)
                is SignInOutcome.Failed -> it.copy(busy = false, error = "Couldn't sign in. Please check your connection and try again.")
            }
        }
    }

    /** Signs out, then back in anonymously, since the app needs someone signed in to talk to the backend */
    fun signOut() {
        val signOut = signOut ?: return
        if (_state.value.busy || _state.value.isGuest) return
        _state.update { it.copy(busy = true, message = null, error = null) }
        viewModelScope.launch {
            val signedOut = try {
                signOut()
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            if (signedOut) auth.ensureSignedIn()
            _state.update {
                if (signedOut) {
                    it.copy(busy = false, account = auth.account(), message = "Signed out")
                } else {
                    it.copy(busy = false, error = "Couldn't sign out. Please try again.")
                }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }
}
