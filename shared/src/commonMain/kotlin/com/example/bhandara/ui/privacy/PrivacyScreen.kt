package com.example.bhandara.ui.privacy

import com.example.bhandara.vendor.LocalVendorMode
import com.example.bhandara.shared.resources.privacy_policy
import com.example.bhandara.platform.ExternalLinks
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.network.ReportApi
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.platform.BackPress
import com.example.bhandara.ui.profile.LocalAccountSignIn
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.platform.rememberAppLanguage
import com.example.bhandara.platform.rememberPermissionStatuses
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.delete_confirm
import com.example.bhandara.shared.resources.delete_download_first
import com.example.bhandara.shared.resources.delete_gone
import com.example.bhandara.shared.resources.delete_gone_profile
import com.example.bhandara.shared.resources.delete_gone_reports
import com.example.bhandara.shared.resources.delete_gone_reviews
import com.example.bhandara.shared.resources.delete_now_note
import com.example.bhandara.shared.resources.delete_stays
import com.example.bhandara.shared.resources.delete_stays_added
import com.example.bhandara.shared.resources.delete_title
import com.example.bhandara.shared.resources.delete_type
import com.example.bhandara.shared.resources.delete_word
import com.example.bhandara.shared.resources.download_account
import com.example.bhandara.shared.resources.download_button
import com.example.bhandara.shared.resources.download_claims
import com.example.bhandara.shared.resources.download_done
import com.example.bhandara.shared.resources.download_feasts
import com.example.bhandara.shared.resources.download_intro
import com.example.bhandara.shared.resources.download_photos
import com.example.bhandara.shared.resources.download_ready_text
import com.example.bhandara.shared.resources.download_ready_title
import com.example.bhandara.shared.resources.download_reports
import com.example.bhandara.shared.resources.download_reviews
import com.example.bhandara.shared.resources.download_saved
import com.example.bhandara.shared.resources.download_share_again
import com.example.bhandara.shared.resources.location_ask
import com.example.bhandara.shared.resources.location_ask_note
import com.example.bhandara.shared.resources.location_change_note
import com.example.bhandara.shared.resources.location_never
import com.example.bhandara.shared.resources.location_never_note
import com.example.bhandara.shared.resources.location_vendor_note
import com.example.bhandara.shared.resources.location_while_using
import com.example.bhandara.shared.resources.location_while_using_note
import com.example.bhandara.shared.resources.privacy_allowed
import com.example.bhandara.shared.resources.privacy_banner_text
import com.example.bhandara.shared.resources.privacy_banner_title
import com.example.bhandara.shared.resources.privacy_camera
import com.example.bhandara.shared.resources.privacy_data
import com.example.bhandara.shared.resources.privacy_delete
import com.example.bhandara.shared.resources.privacy_download
import com.example.bhandara.shared.resources.privacy_download_note
import com.example.bhandara.shared.resources.privacy_location_access
import com.example.bhandara.shared.resources.privacy_location_allowed
import com.example.bhandara.shared.resources.privacy_location_off
import com.example.bhandara.shared.resources.privacy_name_hidden
import com.example.bhandara.shared.resources.privacy_name_hidden_note
import com.example.bhandara.shared.resources.privacy_not_allowed
import com.example.bhandara.shared.resources.privacy_notifications
import com.example.bhandara.shared.resources.privacy_open_settings
import com.example.bhandara.shared.resources.privacy_photos
import com.example.bhandara.shared.resources.privacy_photos_picked
import com.example.bhandara.shared.resources.privacy_saved_private
import com.example.bhandara.shared.resources.privacy_saved_private_note
import com.example.bhandara.shared.resources.privacy_section_data
import com.example.bhandara.shared.resources.privacy_section_location
import com.example.bhandara.shared.resources.privacy_section_permissions
import com.example.bhandara.shared.resources.privacy_section_visibility
import com.example.bhandara.shared.resources.privacy_terms
import com.example.bhandara.shared.resources.privacy_vendor_off
import com.example.bhandara.shared.resources.privacy_vendor_on
import com.example.bhandara.shared.resources.privacy_vendor_sharing
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Privacy and data (design "Privacy and Data": 1a main, 1b location, 1c download, 1d ready, 1e delete; dark 2a–2e).
// Only what's true is shown: there are no name/photo toggles, search history or usage stats, the data comes as a
// file to share rather than an email, and deleting happens at once.

/**
 * Privacy and data, from Profile → Account
 *
 * @param startAt the page to open on: DELETE from "Delete account", LOCATION from Settings → Location access
 * @param hasVendorMode the platform has Vendor Mode, which shares a cart's location
 * @param onAccountDeleted the account is gone and the user signed out; go home
 */
@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onOpenTerms: () -> Unit,
    onAccountDeleted: () -> Unit,
    startAt: PrivacyPage = PrivacyPage.MAIN,
    hasVendorMode: Boolean = LocalVendorMode.current != null,
) {
    val dependencies = LocalAppDependencies.current
    val accountSignIn = LocalAccountSignIn.current
    val viewModel = viewModel {
        PrivacyViewModel(
            ReviewApi(dependencies.apiClient),
            FeastApi(dependencies.apiClient),
            ReportApi(dependencies.apiClient),
            ClaimApi(dependencies.apiClient),
            UserApi(dependencies.apiClient),
            dependencies.settings,
            account = { dependencies.auth.account() },
            signOut = {
                accountSignIn?.signOut()
                dependencies.auth.ensureSignedIn()
            },
            startAt = startAt,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val language = rememberAppLanguage().current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }
    LaunchedEffect(state.deleted) { if (state.deleted) onAccountDeleted() }
    // The file is shared as soon as it's made (1d shows while the share sheet is up)
    LaunchedEffect(state.file) { state.file?.let { actions.shareFile("local-feast-data.json", "application/json", it) } }
    BackPress(enabled = state.page != PrivacyPage.MAIN) { viewModel.back() }

    val goBack: () -> Unit = { if (!viewModel.back()) onBack() }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
                val title = when (state.page) {
                    PrivacyPage.MAIN -> Res.string.privacy_data
                    PrivacyPage.LOCATION -> Res.string.privacy_location_access
                    PrivacyPage.DOWNLOAD -> Res.string.privacy_download
                    PrivacyPage.READY -> null
                    PrivacyPage.DELETE -> Res.string.delete_title
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SquareIconButton(
                        if (state.page == PrivacyPage.READY) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                        stringResource(Res.string.back), goBack, enabled = !state.deleting,
                    )
                    title?.let {
                        Text(stringResource(it), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    }
                }
                when (state.page) {
                    PrivacyPage.MAIN -> MainPage(
                        viewModel::open, onOpenTerms,
                        onOpenPolicy = { actions.openUrl(ExternalLinks.privacyPolicy(dependencies.apiClient.baseUrl)) },
                        hasVendorMode, dependencies.settings.isVendorModeOn, Modifier.weight(1f))
                    PrivacyPage.LOCATION -> LocationPage(hasVendorMode, onOpenSettings = actions::openAppSettings, modifier = Modifier.weight(1f))
                    PrivacyPage.DOWNLOAD -> DownloadPage(state, dependencies.settings.savedPlaceCount, viewModel, onMake = { viewModel.makeFile(language) }, modifier = Modifier.weight(1f))
                    PrivacyPage.READY -> ReadyPage(
                        onShareAgain = { state.file?.let { actions.shareFile("local-feast-data.json", "application/json", it) } },
                        onDone = { viewModel.open(PrivacyPage.MAIN) },
                        modifier = Modifier.weight(1f),
                    )
                    PrivacyPage.DELETE -> DeletePage(state, viewModel, onDownloadFirst = { viewModel.open(PrivacyPage.DOWNLOAD) }, modifier = Modifier.weight(1f))
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 140.dp))
        }
    }
}

// ==================== 1a: the main page ====================

@Composable
private fun MainPage(onOpen: (PrivacyPage) -> Unit, onOpenTerms: () -> Unit, onOpenPolicy: () -> Unit, hasVendorMode: Boolean, vendorModeOn: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    val permissions = rememberPermissionStatuses()
    val actions = LocalPlatformActions.current
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 14.dp, end = 14.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.privacy_banner_title), color = colors.onIndigo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(Res.string.privacy_banner_text), color = colors.headerSubtext, fontSize = 12.sp, lineHeight = 17.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Section(Res.string.privacy_section_visibility) {
            SettingRow(Icons.Outlined.Badge, stringResource(Res.string.privacy_name_hidden), stringResource(Res.string.privacy_name_hidden_note))
            SettingRow(Icons.Rounded.Bookmark, stringResource(Res.string.privacy_saved_private), stringResource(Res.string.privacy_saved_private_note), divider = false) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
            }
        }
        Section(Res.string.privacy_section_location) {
            SettingRow(
                Icons.Rounded.MyLocation, stringResource(Res.string.privacy_location_access),
                stringResource(if (permissions.location) Res.string.privacy_location_allowed else Res.string.privacy_location_off),
                divider = hasVendorMode, onClick = { onOpen(PrivacyPage.LOCATION) },
            ) { Chevron() }
            if (hasVendorMode) {
                SettingRow(
                    Icons.Rounded.LocalShipping, stringResource(Res.string.privacy_vendor_sharing),
                    stringResource(if (vendorModeOn) Res.string.privacy_vendor_on else Res.string.privacy_vendor_off),
                    iconTint = colors.saffronText, divider = false, onClick = { onOpen(PrivacyPage.LOCATION) },
                ) { Chevron() }
            }
        }
        Section(Res.string.privacy_section_permissions) {
            PermissionRow(Icons.Outlined.PhotoCamera, Res.string.privacy_camera, permissions.camera, actions::openAppSettings)
            // Photos are picked one by one with the system picker, which needs no access to the whole gallery
            SettingRow(Icons.Outlined.PhotoLibrary, stringResource(Res.string.privacy_photos), null, onClick = actions::openAppSettings) {
                Status(stringResource(Res.string.privacy_photos_picked), allowed = true)
            }
            PermissionRow(Icons.Outlined.Notifications, Res.string.privacy_notifications, permissions.notifications, actions::openAppSettings, divider = false)
        }
        Section(Res.string.privacy_section_data) {
            SettingRow(Icons.Outlined.FileDownload, stringResource(Res.string.privacy_download), stringResource(Res.string.privacy_download_note), onClick = { onOpen(PrivacyPage.DOWNLOAD) }) { Chevron() }
            SettingRow(Icons.Outlined.Policy, stringResource(Res.string.privacy_policy), null, onClick = onOpenPolicy) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
            }
            SettingRow(Icons.Outlined.Description, stringResource(Res.string.privacy_terms), null, divider = false, onClick = onOpenTerms) { Chevron() }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface)) {
            SettingRow(
                Icons.Outlined.DeleteForever, stringResource(Res.string.privacy_delete), null, iconTint = colors.danger, labelColor = colors.danger,
                divider = false, onClick = { onOpen(PrivacyPage.DELETE) },
            ) { Chevron() }
        }
    }
}

@Composable
private fun Section(title: StringResource, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.feastColors
    Column {
        Text(
            stringResource(title), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp,
            style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        )
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface), content = content)
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    subtitle: String?,
    divider: Boolean = true,
    iconTint: Color? = null,
    labelColor: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = if (subtitle != null) 64.dp else 56.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint ?: colors.accent, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = labelColor ?: colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let { Text(it, color = colors.muted, fontSize = 12.sp, lineHeight = 16.sp, style = MaterialTheme.typography.bodySmall) }
            }
            trailing()
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, label: StringResource, allowed: Boolean?, onClick: () -> Unit, divider: Boolean = true) {
    SettingRow(icon, stringResource(label), null, divider = divider, onClick = onClick) {
        if (allowed != null) Status(stringResource(if (allowed) Res.string.privacy_allowed else Res.string.privacy_not_allowed), allowed)
    }
}

@Composable
private fun Status(text: String, allowed: Boolean) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.height(24.dp).background(if (allowed) colors.successSoft else colors.lavender, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (allowed) colors.success else colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun Chevron() {
    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.feastColors.muted, modifier = Modifier.size(20.dp))
}

// ==================== 1b: location access ====================

/** The three levels a phone offers, with the current one marked; only the phone's settings can change it */
@Composable
private fun LocationPage(hasVendorMode: Boolean, onOpenSettings: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    val allowed = rememberPermissionStatuses().location
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LevelCard(Res.string.location_while_using, Res.string.location_while_using_note, selected = allowed, onClick = onOpenSettings)
            LevelCard(Res.string.location_ask, Res.string.location_ask_note, selected = false, onClick = onOpenSettings)
            LevelCard(Res.string.location_never, Res.string.location_never_note, selected = !allowed, onClick = onOpenSettings)
            Note(Icons.Rounded.Info, stringResource(Res.string.location_change_note), colors.lavender, colors.accent)
            if (hasVendorMode) Note(Icons.Rounded.LocalShipping, stringResource(Res.string.location_vendor_note), colors.saffronSoft, colors.saffronText)
        }
        FeastButton(
            stringResource(Res.string.privacy_open_settings), onClick = onOpenSettings, style = FeastButtonStyle.SOFT, icon = Icons.Rounded.Settings,
            height = 52.dp, modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 28.dp),
        )
    }
}

@Composable
private fun LevelCard(title: StringResource, note: StringResource, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(colors.surface).then(if (selected) Modifier.border(2.dp, colors.selectedOutline, shape) else Modifier)
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.padding(top = 1.dp).size(22.dp).then(
                if (selected) Modifier.border(6.dp, colors.selectedOutline, CircleShape) else Modifier.border(2.dp, colors.border, CircleShape),
            ),
        )
        Column {
            Text(stringResource(title), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(note), color = colors.muted, fontSize = 13.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun Note(icon: ImageVector, text: String, fill: Color, tint: Color) {
    Row(Modifier.fillMaxWidth().background(fill, RoundedCornerShape(18.dp)).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(text, color = MaterialTheme.feastColors.ink, fontSize = 13.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodySmall)
    }
}

// ==================== 1c / 1d: download my data ====================

@Composable
private fun DownloadPage(state: PrivacyState, savedCount: Int, viewModel: PrivacyViewModel, onMake: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    val data = state.data
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.download_intro), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 4.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface)) {
                val rows = listOf(
                    Triple(DataPart.ACCOUNT, Res.string.download_account, null),
                    Triple(DataPart.REVIEWS, Res.string.download_reviews, data?.reviews?.size),
                    Triple(DataPart.PHOTOS, Res.string.download_photos, data?.photos?.size),
                    Triple(DataPart.FEASTS, Res.string.download_feasts, data?.feasts?.size),
                    Triple(DataPart.SAVED, Res.string.download_saved, savedCount),
                    Triple(DataPart.REPORTS, Res.string.download_reports, data?.reports?.size),
                    Triple(DataPart.CLAIMS, Res.string.download_claims, data?.claims?.size),
                )
                rows.forEachIndexed { i, (part, label, count) ->
                    CheckRow(stringResource(label), count, checked = part in state.parts, divider = i < rows.lastIndex) { viewModel.toggle(part) }
                }
            }
            when {
                state.loadingData -> Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.muted)
                }
                state.dataError != null -> Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(state.dataError, color = colors.danger, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(stringResource(Res.string.try_again), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { viewModel.loadData() })
                }
            }
        }
        FeastButton(
            stringResource(Res.string.download_button), onClick = onMake, icon = Icons.Rounded.IosShare, iconTint = colors.saffron,
            enabled = data != null && state.parts.isNotEmpty(), height = 56.dp,
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 28.dp),
        )
    }
}

@Composable
private fun CheckRow(label: String, count: Int?, checked: Boolean, divider: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onToggle).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(24.dp).then(if (checked) Modifier.background(colors.indigo, RoundedCornerShape(8.dp)) else Modifier.border(2.dp, colors.border, RoundedCornerShape(8.dp))),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onIndigo, modifier = Modifier.size(18.dp))
            }
            Text(label, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            count?.let { Text(it.toString(), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium) }
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
    }
}

@Composable
private fun ReadyPage(onShareAgain: () -> Unit, onDone: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(112.dp).background(colors.successSoft, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.TaskAlt, contentDescription = null, tint = colors.success, modifier = Modifier.size(52.dp))
        }
        Text(stringResource(Res.string.download_ready_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.download_ready_text), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeastButton(stringResource(Res.string.download_share_again), onClick = onShareAgain, style = FeastButtonStyle.SOFT, icon = Icons.Rounded.IosShare, height = 52.dp)
            FeastButton(stringResource(Res.string.download_done), onClick = onDone, height = 52.dp)
        }
    }
}

// ==================== 1e: delete account ====================

@Composable
private fun DeletePage(state: PrivacyState, viewModel: PrivacyViewModel, onDownloadFirst: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    val data = state.data
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.delete_gone), color = colors.danger, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Lost(stringResource(Res.string.delete_gone_profile))
                if (data != null) Lost(stringResource(Res.string.delete_gone_reviews, data.reviews.size, data.photos.size))
                Lost(stringResource(Res.string.delete_gone_reports))
            }
            Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.delete_stays), color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                    Text(stringResource(Res.string.delete_stays_added), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column {
                Text(stringResource(Res.string.delete_type), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 6.dp))
                BasicTextField(
                    value = state.deleteText,
                    onValueChange = viewModel::onDeleteTextChange,
                    singleLine = true,
                    enabled = !state.deleting,
                    textStyle = TextStyle(color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    cursorBrush = SolidColor(colors.ink),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { field ->
                        Box(
                            Modifier.fillMaxWidth().height(50.dp).background(colors.surface, RoundedCornerShape(14.dp)).border(1.5.dp, colors.border, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (state.deleteText.isEmpty()) Text(stringResource(Res.string.delete_word), color = colors.disabledText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            field()
                        }
                    },
                )
            }
            Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.delete_now_note), color = colors.muted, fontSize = 12.sp, lineHeight = 17.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FeastButton(
                stringResource(Res.string.delete_confirm), onClick = viewModel::deleteAccount, style = FeastButtonStyle.DANGER, enabled = state.canDelete, height = 52.dp,
                modifier = Modifier.fillMaxWidth(),
                content = if (state.deleting) { { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White) } } else null,
            )
            FeastButton(stringResource(Res.string.delete_download_first), onClick = onDownloadFirst, style = FeastButtonStyle.SOFT, enabled = !state.deleting, height = 52.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Lost(text: String) {
    val colors = MaterialTheme.feastColors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.danger, modifier = Modifier.size(18.dp))
        Text(text, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}
