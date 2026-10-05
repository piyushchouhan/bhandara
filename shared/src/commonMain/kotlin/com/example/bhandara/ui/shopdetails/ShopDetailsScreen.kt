package com.example.bhandara.ui.shopdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.models.api.ShopReportReason
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.deactivate_confirm
import com.example.bhandara.shared.resources.deactivate_text
import com.example.bhandara.shared.resources.deactivate_title
import com.example.bhandara.shared.resources.report_prompt
import com.example.bhandara.shared.resources.report_reason_closed
import com.example.bhandara.shared.resources.report_reason_inappropriate
import com.example.bhandara.shared.resources.report_reason_not_food_shop
import com.example.bhandara.shared.resources.report_reason_wrong_location
import com.example.bhandara.shared.resources.report_submit
import com.example.bhandara.shared.resources.report_title
import com.example.bhandara.shared.resources.shop_details_title
import com.example.bhandara.shared.resources.suggest_delete_confirm
import com.example.bhandara.shared.resources.suggest_delete_text
import com.example.bhandara.shared.resources.suggest_delete_title
import com.example.bhandara.shared.resources.tab_overview
import com.example.bhandara.shared.resources.tab_reviews
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.menu.AddMenuItemsScreen
import com.example.bhandara.ui.reviews.ReviewsSection
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * One shop's page, shared by Android and iOS: photos, whether it's open and how busy, timings and address, the menu
 * with prices, facilities, directions and call, and reporting or (for its owner) deactivating the shop.
 *
 * @param onClaim       the real owner claims a shop the platform listed; null hides "Is this your shop?"
 */
@Composable
fun ShopDetailsScreen(
    shopId: String,
    onBack: () -> Unit,
    onClaim: ((shopName: String) -> Unit)? = null,
) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        ShopDetailsViewModel(
            shopId,
            ShopApi(dependencies.apiClient),
            dependencies.settings,
            { dependencies.auth.currentUid },
            LocationService(deviceLocation),
        )
    }
    var addingMenuItems by rememberSaveable { mutableStateOf(false) }

    if (addingMenuItems) {
        // The owner adding items, in place of the page; the menu reloads when they're saved
        val state by viewModel.state.collectAsStateWithLifecycle()
        AddMenuItemsScreen(
            shopId = shopId,
            existingNames = state.menu.map { it.name },
            onSaved = {
                addingMenuItems = false
                viewModel.refreshMenu()
            },
            onBack = { addingMenuItems = false },
        )
    } else {
        ShopDetailsPage(
            shopId = shopId,
            viewModel = viewModel,
            onBack = onBack,
            onClaim = onClaim,
            onAddMenuItems = { addingMenuItems = true },
        )
    }
}

private enum class ShopDialog { REPORT, SUGGEST_DELETE, DEACTIVATE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopDetailsPage(
    shopId: String,
    viewModel: ShopDetailsViewModel,
    onBack: () -> Unit,
    onClaim: ((shopName: String) -> Unit)?,
    onAddMenuItems: (() -> Unit)?,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val snackbar = remember { SnackbarHostState() }
    var dialog by rememberSaveable { mutableStateOf<ShopDialog?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var fullScreenPhoto by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.shop_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val shop = state.shop
        when {
            state.loading && shop == null -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }

            shop == null -> LoadFailed(state.error, onRetry = viewModel::retry, modifier = Modifier.padding(padding))

            else -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ShopPhotos(shop.imageUrls.orEmpty(), onPhotoClick = { fullScreenPhoto = it })
                Spacer(Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background) {
                    listOf(Res.string.tab_overview, Res.string.tab_reviews).forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    stringResource(title),
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (selectedTab == 1) {
                    ReviewsSection(
                        shopId = shopId,
                        showMessage = { snackbar.showSnackbar(it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                } else {
                    ShopOverview(
                        state = state,
                        onDeactivate = { dialog = ShopDialog.DEACTIVATE },
                        onSuggestDelete = { dialog = ShopDialog.SUGGEST_DELETE },
                        onReport = { dialog = ShopDialog.REPORT },
                        onAddMenuItems = onAddMenuItems?.takeIf { state.canEditMenu },
                        onClaim = onClaim?.takeIf { state.canClaim }?.let { claim -> { claim(shop.shopName) } },
                        onDirections = { actions.openDirections(shop.latitude, shop.longitude) },
                        onCall = { shop.ownerPhone?.let(actions::dial) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                Spacer(Modifier.height(32.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }

    val photos = state.shop?.imageUrls.orEmpty()
    fullScreenPhoto?.takeIf { it in photos.indices }?.let { index ->
        FullScreenPhotos(photos, initialIndex = index, onDismiss = { fullScreenPhoto = null })
    }

    when (dialog) {
        ShopDialog.REPORT -> ReportDialog(
            onReport = { reason ->
                dialog = null
                viewModel.report(reason)
            },
            onDismiss = { dialog = null },
        )

        ShopDialog.SUGGEST_DELETE -> ConfirmDialog(
            title = Res.string.suggest_delete_title,
            text = Res.string.suggest_delete_text,
            confirm = Res.string.suggest_delete_confirm,
            onConfirm = {
                dialog = null
                viewModel.suggestDelete()
            },
            onDismiss = { dialog = null },
        )

        ShopDialog.DEACTIVATE -> ConfirmDialog(
            title = Res.string.deactivate_title,
            text = Res.string.deactivate_text,
            confirm = Res.string.deactivate_confirm,
            onConfirm = {
                dialog = null
                viewModel.deactivate()
            },
            onDismiss = { dialog = null },
        )

        null -> {}
    }
}

@Composable
private fun LoadFailed(error: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(error.orEmpty(), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(Res.string.try_again)) }
    }
}

private val ShopReportReason.label: StringResource
    get() = when (this) {
        ShopReportReason.NOT_FOOD_SHOP -> Res.string.report_reason_not_food_shop
        ShopReportReason.WRONG_LOCATION -> Res.string.report_reason_wrong_location
        ShopReportReason.INAPPROPRIATE_CONTENT -> Res.string.report_reason_inappropriate
        ShopReportReason.PERMANENTLY_CLOSED -> Res.string.report_reason_closed
    }

/** "Why are you reporting this shop?" with one reason to pick */
@Composable
private fun ReportDialog(onReport: (ShopReportReason) -> Unit, onDismiss: () -> Unit) {
    var reason by rememberSaveable { mutableStateOf<ShopReportReason?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.report_title)) },
        text = {
            Column {
                Text(stringResource(Res.string.report_prompt))
                Spacer(Modifier.height(8.dp))
                ShopReportReason.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = option == reason, onClick = { reason = option }, role = Role.RadioButton)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == reason, onClick = null)
                        Spacer(Modifier.padding(start = 8.dp))
                        Text(stringResource(option.label))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { reason?.let(onReport) },
                enabled = reason != null,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(Res.string.report_submit)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}

@Composable
private fun ConfirmDialog(
    title: StringResource,
    text: StringResource,
    confirm: StringResource,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(text)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
