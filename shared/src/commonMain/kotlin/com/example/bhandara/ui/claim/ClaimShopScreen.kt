package com.example.bhandara.ui.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
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
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.profile.SignInGate
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
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
        FeastTheme { ClaimShopPage(viewModel, shopName, onDone) }
    }
}

/**
 * The claim form (design 2d): the steps as a checklist on an indigo card, three photo slots (the first one takes
 * a photo), the owner's phone, and "Submit claim" at the bottom, locked until there's a photo
 */
@Composable
private fun ClaimShopPage(viewModel: ClaimShopViewModel, shopName: String, onDone: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val camera = rememberPhotoPicker(viewModel::addPhotos)
    val requestLocation = rememberLocationPermissionRequest { granted -> if (granted) viewModel.submit() }
    val outcome = state.outcome

    Column(Modifier.fillMaxSize().background(colors.page).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onDone, enabled = !state.submitting)
            Text(stringResource(Res.string.claim_screen_title), color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        }

        if (outcome != null) {
            Decision(outcome, state.decision?.decisionReason, onTryAgain = viewModel::tryAgain, onDone = onDone, modifier = Modifier.weight(1f))
            return@Column
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                intro(stringResource(Res.string.claim_intro, shopName), shopName, colors.ink),
                color = colors.body, fontSize = 15.sp, lineHeight = 22.sp, style = MaterialTheme.typography.bodyLarge,
            )
            HowItWorks()

            Column {
                Text(
                    stringResource(Res.string.claim_photos), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp),
                )
                if (!camera.canTakePhoto) {
                    Text(stringResource(Res.string.claim_no_camera), color = colors.danger, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                }
                PhotoSlots(
                    photos = state.photos.map { it.preview },
                    canTake = state.canAddPhotos && camera.canTakePhoto && !state.submitting,
                    canRemove = !state.submitting,
                    onTake = camera::takePhoto,
                    onRemove = { index -> viewModel.removePhoto(state.photos[index]) },
                )
            }

            PhoneField(state.phone, onChange = viewModel::onPhoneChange, enabled = !state.submitting)

            state.error?.let { Text(it, color = colors.danger, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium) }
            if (state.needsLocation) {
                FeastButton(
                    stringResource(Res.string.map_allow_location), onClick = requestLocation,
                    style = FeastButtonStyle.SOFT, icon = Icons.Rounded.MyLocation, modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Submit stays at the thumb, on a white bar
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(12.dp, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.15f))
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
        ) {
            val progress = state.progress
            FeastButton(
                label = stringResource(Res.string.claim_submit),
                onClick = viewModel::submit,
                enabled = state.photos.isNotEmpty() || progress != null,
                height = 56.dp,
                modifier = Modifier.fillMaxWidth(),
                content = progress?.let {
                    {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onIndigo)
                        Text(
                            when (it) {
                                ClaimProgress.LOCATING -> stringResource(Res.string.claim_locating)
                                ClaimProgress.UPLOADING -> stringResource(Res.string.review_uploading, "${state.uploadProgress ?: 0}%")
                                ClaimProgress.CHECKING -> stringResource(Res.string.claim_checking)
                            },
                            color = colors.onIndigo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
            )
        }
    }
}

/** "Do you own **Shop**? Claim it…", with the shop's name in bold ink */
private fun intro(text: String, shopName: String, nameColor: Color): AnnotatedString = buildAnnotatedString {
    val at = text.indexOf(shopName)
    if (at < 0 || shopName.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, at))
    withStyle(SpanStyle(color = nameColor, fontWeight = FontWeight.SemiBold)) { append(shopName) }
    append(text.substring(at + shopName.length))
}

/** The three steps as a numbered checklist on indigo; the first, "stand at your shop", is lit */
@Composable
private fun HowItWorks() {
    val colors = MaterialTheme.feastColors
    Column(Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(16.dp)) {
        Text(
            stringResource(Res.string.claim_how), color = colors.saffron, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp, style = MaterialTheme.typography.labelLarge,
        )
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                stringResource(Res.string.claim_step1),
                stringResource(Res.string.claim_step2, MAX_CLAIM_PHOTOS),
                stringResource(Res.string.claim_step3),
            ).forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val first = index == 0
                    Box(
                        Modifier.size(28.dp).background(if (first) colors.saffron else colors.barTrackOnIndigo, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}", color = if (first) colors.onSaffron else colors.onIndigo, fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Text(
                        step, color = if (index == 2) colors.headerSubtext else colors.onIndigo, fontSize = 14.sp, lineHeight = 20.sp,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** Three slots: photos taken so far, then a saffron "Take photo" tile, then dashed empty slots up to the limit */
@Composable
private fun PhotoSlots(photos: List<Any>, canTake: Boolean, canRemove: Boolean, onTake: () -> Unit, onRemove: (Int) -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        photos.forEachIndexed { index, preview ->
            Box(Modifier.size(90.dp).clip(shape).background(colors.lavender)) {
                AsyncImage(preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                if (canRemove) {
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(5.dp).size(26.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)).clickable { onRemove(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        var slots = MAX_CLAIM_PHOTOS - photos.size
        if (canTake && slots > 0) {
            slots--
            Column(
                Modifier.size(90.dp).clip(shape).background(colors.saffron).clickable(onClick = onTake),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(28.dp))
                Text(stringResource(Res.string.photo_take), color = colors.onSaffron, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
        }
        repeat(slots) {
            Box(
                Modifier.size(90.dp).drawBehind {
                    drawRoundRect(
                        colors.border, cornerRadius = CornerRadius(18.dp.toPx()),
                        style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                    )
                },
            )
        }
    }
}

/** "Your phone number (optional)", with what it's for underneath */
@Composable
private fun PhoneField(phone: String, onChange: (String) -> Unit, enabled: Boolean) {
    val colors = MaterialTheme.feastColors
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = colors.ink)
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.5.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.Call, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            BasicTextField(
                value = phone,
                onValueChange = onChange,
                enabled = enabled,
                singleLine = true,
                textStyle = style,
                cursorBrush = SolidColor(colors.ink),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f),
                decorationBox = { field ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (phone.isEmpty()) Text(stringResource(Res.string.claim_phone), style = style.copy(color = colors.placeholder), maxLines = 1)
                        field()
                    }
                },
            )
        }
        Text(
            stringResource(Res.string.claim_phone_hint), color = colors.muted, fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp),
        )
    }
}

/** What the backend decided: owned now, being reviewed, or not approved (with why, and a way to try again) */
@Composable
private fun Decision(outcome: ClaimOutcome, reason: String?, onTryAgain: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    data class Look(val icon: ImageVector, val title: org.jetbrains.compose.resources.StringResource, val tint: Color, val soft: Color)
    val look = when (outcome) {
        ClaimOutcome.APPROVED -> Look(Icons.Rounded.CheckCircle, Res.string.claim_approved, colors.success, colors.successSoft)
        ClaimOutcome.IN_REVIEW -> Look(Icons.Rounded.HourglassTop, Res.string.claim_in_review, colors.saffronText, colors.saffronSoft)
        ClaimOutcome.REJECTED -> Look(Icons.Rounded.Cancel, Res.string.claim_rejected, colors.danger, colors.dangerSoft)
    }
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        ) {
            Box(Modifier.size(110.dp).background(look.soft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(look.icon, contentDescription = null, tint = look.tint, modifier = Modifier.size(52.dp))
            }
            Text(stringResource(look.title), color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
            if (outcome == ClaimOutcome.IN_REVIEW) {
                Text(stringResource(Res.string.claim_in_review_text), color = colors.body, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            }
            reason?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = colors.body, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Column(
            Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (outcome == ClaimOutcome.REJECTED) {
                FeastButton(stringResource(Res.string.claim_try_again), onClick = onTryAgain, style = FeastButtonStyle.SOFT, modifier = Modifier.fillMaxWidth())
            }
            FeastButton(
                stringResource(if (outcome == ClaimOutcome.APPROVED) Res.string.claim_go_to_shop else Res.string.done),
                onClick = onDone, height = 56.dp, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
