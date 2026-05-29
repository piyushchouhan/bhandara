package com.example.bhandara.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.bhandara.R
import com.example.bhandara.services.VendorLocationManager
import kotlinx.coroutines.launch
import java.util.Locale

private const val VENDOR_PREFS = "VendorPrefs"
private const val KEY_SHOP_ID = "vendor_shop_id"
private const val KEY_OWNER_UID = "vendor_owner_uid"
private const val KEY_VENDOR_MODE = "vendor_mode_active"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerMenu(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val prefs = remember { context.getSharedPreferences(VENDOR_PREFS, Context.MODE_PRIVATE) }
    val vendorShopId = remember { prefs.getLong(KEY_SHOP_ID, -1L) }
    val vendorOwnerUid = remember { prefs.getString(KEY_OWNER_UID, null) }
    val hasMovingCartShop = vendorShopId > 0 && vendorOwnerUid != null

    var currentLanguage by remember { mutableStateOf(Locale.getDefault().language) }
    var vendorModeOn by remember { mutableStateOf(prefs.getBoolean(KEY_VENDOR_MODE, false)) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showReportIssueDialog by remember { mutableStateOf(false) }
    var showSuggestFeatureDialog by remember { mutableStateOf(false) }
    var showHowToUseDialog by remember { mutableStateOf(false) }
    var showShareAppDialog by remember { mutableStateOf(false) }

    val vendorLocationManager = remember { VendorLocationManager(context) }

    // Restore vendor mode if it was on
    if (vendorModeOn && hasMovingCartShop && !vendorLocationManager.isActive()) {
        vendorLocationManager.start(vendorShopId, vendorOwnerUid!!)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // ─── Header: Logo + App Name ──────────────────────────────
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.localfeast),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // ─── Feature Items ────────────────────────────────────────
                    Spacer(modifier = Modifier.height(8.dp))

                    // Language
                    var showLanguageMenu by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageMenu = true }
                                .height(56.dp)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.language),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = when (currentLanguage) {
                                    "hi" -> "हिंदी"
                                    "mr" -> "मराठी"
                                    else -> "English"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            offset = androidx.compose.ui.unit.DpOffset(x = 24.dp, y = 0.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    currentLanguage = "en"
                                    showLanguageMenu = false
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags("en")
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("हिंदी") },
                                onClick = {
                                    currentLanguage = "hi"
                                    showLanguageMenu = false
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags("hi")
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("मराठी") },
                                onClick = {
                                    currentLanguage = "mr"
                                    showLanguageMenu = false
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags("mr")
                                    )
                                }
                            )
                        }
                    }

                    // Vendor Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (!hasMovingCartShop) 72.dp else 56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = if (vendorModeOn) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.vendor_mode),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!hasMovingCartShop) {
                                Text(
                                    text = stringResource(R.string.vendor_mode_no_shop),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Switch(
                            checked = vendorModeOn,
                            onCheckedChange = { enabled ->
                                if (!hasMovingCartShop) return@Switch
                                vendorModeOn = enabled
                                prefs.edit().putBoolean(KEY_VENDOR_MODE, enabled).apply()
                                if (enabled) {
                                    vendorLocationManager.start(vendorShopId, vendorOwnerUid!!)
                                } else {
                                    vendorLocationManager.stop()
                                }
                            },
                            enabled = hasMovingCartShop,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Vendor mode active info
                    if (vendorModeOn && hasMovingCartShop) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(40.dp)) // indent to align with text above
                            Text(
                                text = stringResource(R.string.vendor_mode_active),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // About
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.about),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // About Dialog
                    if (showAboutDialog) {
                        AboutDialog(
                            onDismissRequest = { showAboutDialog = false }
                        )
                    }

                    // Support
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSupportDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.support),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Support Dialog
                    if (showSupportDialog) {
                        SupportDialog(
                            onDismissRequest = { showSupportDialog = false }
                        )
                    }

                    // Terms & Conditions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.terms),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Terms & Conditions Dialog
                    if (showTermsDialog) {
                        TermsAndConditionsDialog(
                            onDismissRequest = { showTermsDialog = false }
                        )
                    }

                    // Report Issue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReportIssueDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.report_issue),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Report Issue Dialog
                    if (showReportIssueDialog) {
                        ReportIssueDialog(
                            onDismissRequest = { showReportIssueDialog = false }
                        )
                    }

                    // Suggest a Feature
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSuggestFeatureDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.suggest_feature),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Suggest a Feature Dialog
                    if (showSuggestFeatureDialog) {
                        SuggestFeatureDialog(
                            onDismissRequest = { showSuggestFeatureDialog = false }
                        )
                    }

                    // How to Use
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHowToUseDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.how_to_use),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // How to Use Dialog
                    if (showHowToUseDialog) {
                        HowToUseDialog(
                            onDismissRequest = { showHowToUseDialog = false }
                        )
                    }

                    // Share App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showShareAppDialog = true }
                            .height(56.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.share_app),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share App Dialog
                    if (showShareAppDialog) {
                        ShareAppDialog(
                            onDismissRequest = { showShareAppDialog = false }
                        )
                    }

                    // ─── Spacer pushes footer to the bottom ───────────────────
                    Spacer(modifier = Modifier.weight(1f))

                    // ─── Footer: Divider + Branding ───────────────────────────
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Upalio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Box {
            // Main content column (not affected by the warning banner)
            Column {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )

                content()
            }

            // Vendor mode warning banner overlaps on top without pushing content
            if (vendorModeOn && hasMovingCartShop) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    Text(
                        text = stringResource(R.string.vendor_mode_active),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
