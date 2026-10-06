package com.example.bhandara.ui.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.photos.rememberPhotoPicker
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.claim_approved
import com.example.bhandara.shared.resources.claim_checking
import com.example.bhandara.shared.resources.claim_go_to_shop
import com.example.bhandara.shared.resources.claim_how
import com.example.bhandara.shared.resources.claim_in_review
import com.example.bhandara.shared.resources.claim_in_review_text
import com.example.bhandara.shared.resources.claim_intro
import com.example.bhandara.shared.resources.claim_locating
import com.example.bhandara.shared.resources.claim_no_camera
import com.example.bhandara.shared.resources.claim_phone
import com.example.bhandara.shared.resources.claim_phone_hint
import com.example.bhandara.shared.resources.claim_photos
import com.example.bhandara.shared.resources.claim_rejected
import com.example.bhandara.shared.resources.claim_screen_title
import com.example.bhandara.shared.resources.claim_sign_in_reason
import com.example.bhandara.shared.resources.claim_step1
import com.example.bhandara.shared.resources.claim_step2
import com.example.bhandara.shared.resources.claim_step3
import com.example.bhandara.shared.resources.claim_submit
import com.example.bhandara.shared.resources.claim_try_again
import com.example.bhandara.shared.resources.done
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.photo_take
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.ui.profile.SignInGate
import org.jetbrains.compose.resources.stringResource

/**
 * Claiming a shop the platform listed, shared by Android and iOS. Only for users signed in with an account; the
 * photos are taken with the camera at the shop. [onDone] leaves, back to the shop's page (which shows the new
 * owner's options once approved).
 */
@Composable
fun ClaimShopScreen(shopId: String, shopName: String, onDone: () -> Unit) {
    SignInGate(reason = stringResource(Res.string.claim_sign_in_reason), onBack = onDone) {
        val dependencies = LocalAppDependencies.current
        val deviceLocation = rememberDeviceLocation()
        val viewModel = viewModel {
            ClaimShopViewModel(shopId, ClaimApi(dependencies.apiClient), dependencies.photoUploader::upload, LocationService(deviceLocation))
        }
        ClaimShopPage(viewModel, shopName, onDone)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaimShopPage(viewModel: ClaimShopViewModel, shopName: String, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val camera = rememberPhotoPicker(viewModel::addPhotos)
    val requestLocation = rememberLocationPermissionRequest { granted -> if (granted) viewModel.submit() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.claim_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onDone, enabled = !state.submitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val outcome = state.outcome
            if (outcome != null) {
                Decision(outcome, state.decision?.decisionReason, onTryAgain = viewModel::tryAgain, onDone = onDone)
                return@Column
            }
            Text(stringResource(Res.string.claim_intro, shopName), style = MaterialTheme.typography.bodyLarge)
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(Res.string.claim_how), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(Res.string.claim_step1))
                    Text(stringResource(Res.string.claim_step2, MAX_CLAIM_PHOTOS))
                    Text(stringResource(Res.string.claim_step3))
                }
            }

            Text(stringResource(Res.string.claim_photos), style = MaterialTheme.typography.titleMedium)
            if (!camera.canTakePhoto) {
                Text(stringResource(Res.string.claim_no_camera), color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.photos.forEach { photo ->
                    Box {
                        AsyncImage(
                            model = photo.preview,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                        )
                        IconButton(
                            onClick = { viewModel.removePhoto(photo) },
                            enabled = !state.submitting,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        ) {
                            Icon(Icons.Default.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                if (state.canAddPhotos && camera.canTakePhoto) {
                    OutlinedButton(
                        onClick = camera::takePhoto,
                        enabled = !state.submitting,
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(28.dp))
                            Text(stringResource(Res.string.photo_take), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text(stringResource(Res.string.claim_phone)) },
                supportingText = { Text(stringResource(Res.string.claim_phone_hint)) },
                singleLine = true,
                enabled = !state.submitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.needsLocation) {
                OutlinedButton(onClick = requestLocation, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.map_allow_location))
                }
            }
            Button(onClick = viewModel::submit, enabled = state.photos.isNotEmpty() && !state.submitting, modifier = Modifier.fillMaxWidth()) {
                when (state.progress) {
                    null -> Text(stringResource(Res.string.claim_submit))
                    else -> {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when (state.progress) {
                                ClaimProgress.LOCATING -> stringResource(Res.string.claim_locating)
                                ClaimProgress.UPLOADING -> stringResource(Res.string.review_uploading, "${state.uploadProgress ?: 0}%")
                                else -> stringResource(Res.string.claim_checking)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** What the backend decided: owned now, being reviewed, or not approved (with why, and a way to try again) */
@Composable
private fun Decision(outcome: ClaimOutcome, reason: String?, onTryAgain: () -> Unit, onDone: () -> Unit) {
    val (icon, title, color) = when (outcome) {
        ClaimOutcome.APPROVED -> Triple(Icons.Default.CheckCircle, Res.string.claim_approved, MaterialTheme.colorScheme.primaryContainer)
        ClaimOutcome.IN_REVIEW -> Triple(Icons.Default.HourglassTop, Res.string.claim_in_review, MaterialTheme.colorScheme.secondaryContainer)
        ClaimOutcome.REJECTED -> Triple(Icons.Default.Cancel, Res.string.claim_rejected, MaterialTheme.colorScheme.errorContainer)
    }
    Surface(color = color, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (outcome == ClaimOutcome.IN_REVIEW) Text(stringResource(Res.string.claim_in_review_text), textAlign = TextAlign.Center)
            reason?.takeIf { it.isNotBlank() }?.let { Text(it, textAlign = TextAlign.Center) }
        }
    }
    if (outcome == ClaimOutcome.REJECTED) {
        OutlinedButton(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.claim_try_again)) }
    }
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(if (outcome == ClaimOutcome.APPROVED) Res.string.claim_go_to_shop else Res.string.done))
    }
}
