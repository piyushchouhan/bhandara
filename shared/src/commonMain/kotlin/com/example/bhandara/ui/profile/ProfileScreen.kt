package com.example.bhandara.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.profile_connecting
import com.example.bhandara.shared.resources.profile_dialog_guest_mode
import com.example.bhandara.shared.resources.profile_dialog_guest_session
import com.example.bhandara.shared.resources.profile_dialog_guest_user
import com.example.bhandara.shared.resources.profile_dialog_linked_google
import com.example.bhandara.shared.resources.profile_dialog_sign_in
import com.example.bhandara.shared.resources.profile_dialog_sign_out
import com.example.bhandara.shared.resources.profile_dialog_title
import com.example.bhandara.shared.resources.profile_sign_in_later
import com.example.bhandara.shared.resources.profile_signing_out
import com.example.bhandara.ui.components.ScreenViewModels
import org.jetbrains.compose.resources.stringResource

/**
 * Signing in with an account (Google), as each platform does it. Android implements it; iPhone gets it with the
 * other sign-in work (phase 2), until then the profile shows the guest account only.
 */
interface AccountSignIn {

    /** Returns what opens the platform's sign-in (e.g. Google's account picker); [onResult] gets how it ended */
    @Composable
    fun rememberSignIn(onResult: (SignInOutcome) -> Unit): () -> Unit

    /** Signs out of the account (Firebase and the provider); throws if it couldn't */
    suspend fun signOut()
}

/** The profile, shared by Android and iOS: who is signed in, and signing in with Google or out */
@Composable
fun ProfileScreen(onBack: () -> Unit, accountSignIn: AccountSignIn?) = ScreenViewModels {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { ProfileViewModel(dependencies.auth, accountSignIn?.let { it::signOut }) }
    ProfilePage(viewModel, accountSignIn, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePage(viewModel: ProfileViewModel, accountSignIn: AccountSignIn?, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val startSignIn = accountSignIn?.rememberSignIn(viewModel::onSignInResult)

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.profile_dialog_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val account = state.account
            Box(Modifier.size(104.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                val photo = account?.photoUrl?.takeIf { !state.isGuest }
                if (photo != null) {
                    AsyncImage(model = photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(72.dp))
                }
            }
            Text(
                if (state.isGuest) stringResource(Res.string.profile_dialog_guest_user) else account?.name ?: account?.email.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (state.isGuest) stringResource(Res.string.profile_dialog_guest_session) else account?.email.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                color = if (state.isGuest) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    stringResource(if (state.isGuest) Res.string.profile_dialog_guest_mode else Res.string.profile_dialog_linked_google),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }

            state.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            Spacer(Modifier.size(12.dp))
            when {
                startSignIn == null -> Text(
                    stringResource(Res.string.profile_sign_in_later),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.isGuest -> OutlinedButton(
                    onClick = {
                        viewModel.onSignInStarted()
                        startSignIn()
                    },
                    enabled = !state.busy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.busy) BusyLabel(stringResource(Res.string.profile_connecting))
                    else Text(stringResource(Res.string.profile_dialog_sign_in))
                }
                else -> Button(
                    onClick = viewModel::signOut,
                    enabled = !state.busy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.busy) BusyLabel(stringResource(Res.string.profile_signing_out))
                    else Text(stringResource(Res.string.profile_dialog_sign_out))
                }
            }
        }
    }
}

@Composable
private fun BusyLabel(text: String) {
    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
    Spacer(Modifier.width(8.dp))
    Text(text)
}
