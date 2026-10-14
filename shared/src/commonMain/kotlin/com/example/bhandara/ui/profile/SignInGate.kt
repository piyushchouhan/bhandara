package com.example.bhandara.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.profile_connecting
import com.example.bhandara.shared.resources.profile_dialog_sign_in
import com.example.bhandara.shared.resources.sign_in_required_title
import org.jetbrains.compose.resources.stringResource

/** The platform's account sign-in (Google), for any screen that needs it; null where it isn't available */
val LocalAccountSignIn = staticCompositionLocalOf<AccountSignIn?> { null }

/**
 * Shows [content] to users signed in with an account; guests are asked to sign in first, with [reason] saying
 * why (e.g. "so customers know who listed it"), and a way back. Signing in here shows [content] straight away.
 */
@Composable
fun SignInGate(reason: String, onBack: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dependencies = LocalAppDependencies.current
    val accountSignIn = LocalAccountSignIn.current
    val viewModel = viewModel(key = "sign-in-gate") { ProfileViewModel(dependencies.auth, accountSignIn?.let { it::signOut }) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.isGuest) {
        content()
        return
    }
    val startSignIn = accountSignIn?.rememberSignIn(viewModel::onSignInResult)
    Box(modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        IconButton(onClick = onBack, modifier = Modifier.padding(4.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
        }
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
            Text(stringResource(Res.string.sign_in_required_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(reason, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
            if (startSignIn != null) {
                OutlinedButton(
                    onClick = {
                        viewModel.onSignInStarted()
                        startSignIn()
                    },
                    enabled = !state.busy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.busy) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.profile_connecting))
                    } else {
                        Text(stringResource(Res.string.profile_dialog_sign_in))
                    }
                }
            }
        }
    }
}
