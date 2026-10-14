package com.example.bhandara.ui.components

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.bhandara.R
import com.example.bhandara.managers.UserManager
import com.example.bhandara.ui.profile.AccountSignIn
import com.example.bhandara.ui.profile.SignInOutcome
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Google sign-in on Android for the shared profile. The guest account is upgraded to the Google one so the user
 * keeps what they did as a guest; if that Google account already exists, the user switches to it.
 */
class GoogleAccountSignIn(context: Context) : AccountSignIn {

    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val googleClient = GoogleSignIn.getClient(
        appContext,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(appContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build(),
    )

    // Registering the (new) user with the backend outlives the profile screen
    private val userManager = UserManager(appContext, MainScope())

    @Composable
    override fun rememberSignIn(onResult: (SignInOutcome) -> Unit): () -> Unit {
        val scope = rememberCoroutineScope()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            scope.launch { onResult(finishSignIn(result.data)) }
        }
        return {
            scope.launch {
                // Signing out of Google first makes it show the account picker every time
                runCatching { googleClient.signOut().await() }
                launcher.launch(googleClient.signInIntent)
            }
        }
    }

    private suspend fun finishSignIn(data: android.content.Intent?): SignInOutcome = try {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        val idToken = account?.idToken ?: return SignInOutcome.Failed("No ID token")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val guest = auth.currentUser
        try {
            if (guest != null) guest.linkWithCredential(credential).await() else auth.signInWithCredential(credential).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            auth.signInWithCredential(credential).await()
        }
        // Make sure the profile photo and name are loaded
        auth.currentUser?.reload()?.await()
        userManager.initializeUser()
        SignInOutcome.SignedIn
    } catch (e: ApiException) {
        if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
            SignInOutcome.Cancelled
        } else {
            Log.e(TAG, "Google sign-in failed (code ${e.statusCode})", e)
            SignInOutcome.Failed("code ${e.statusCode}")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Sign-in failed", e)
        SignInOutcome.Failed(e.message)
    }

    override suspend fun signOut() {
        googleClient.signOut().await()
        auth.signOut()
        // Back to a guest account, registered with the backend like on first start
        userManager.initializeUser()
    }

    private companion object {
        const val TAG = "GoogleAccountSignIn"
    }
}
