package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.network.ActivityApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.models.api.ShopReportReason
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.ExternalLinks
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.address_copied
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.claim_screen_title
import com.example.bhandara.shared.resources.claim_title
import com.example.bhandara.shared.resources.deactivate_confirm
import com.example.bhandara.shared.resources.deactivate_text
import com.example.bhandara.shared.resources.deactivate_title
import com.example.bhandara.shared.resources.more_options
import com.example.bhandara.shared.resources.report_prompt
import com.example.bhandara.shared.resources.report_reason_closed
import com.example.bhandara.shared.resources.report_reason_inappropriate
import com.example.bhandara.shared.resources.report_reason_not_food_shop
import com.example.bhandara.shared.resources.report_reason_wrong_location
import com.example.bhandara.shared.resources.report_submit
import com.example.bhandara.shared.resources.report_title
import com.example.bhandara.shared.resources.reported
import com.example.bhandara.shared.resources.share_shop_text
import com.example.bhandara.shared.resources.suggest_delete_confirm
import com.example.bhandara.shared.resources.suggest_delete_text
import com.example.bhandara.shared.resources.suggest_delete_title
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.menu.AddMenuItemsScreen
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * One shop's page, shared by Android and iOS (the redesign's full details): photos across the top, whether it's
 * open and how busy, call / share / review, where it is, the menu with prices, facilities and description,
 * "Is this your shop?", and Reviews and Directions always at the thumb. The ⋮ menu claims, reports or suggests
 * deleting the shop (for its owner: deactivates it).
 *
 * @param onClaim       the real owner claims a shop the platform listed; null hides claiming
 * @param onOpenReviews the shop's reviews
 * @param onWriteReview writing a review of the shop (the "Review" button)
 */
@Composable
fun ShopDetailsScreen(
    shopId: String,
    onBack: () -> Unit,
    onOpenReviews: (shopName: String, photoUrl: String?) -> Unit,
    onWriteReview: (shopName: String, photoUrl: String?) -> Unit,
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
            activityApi = ActivityApi(dependencies.apiClient),
            uploadPhotos = { photos -> dependencies.photoUploader.upload(photos) },
        )
    }
    var addingMenuItems by rememberSaveable { mutableStateOf(false) }
    // Coming back to the page (e.g. after claiming the shop or writing a review) shows it as it is now
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.retry() else shownBefore = true
    }

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
        FeastTheme {
            ShopDetailsPage(
                viewModel = viewModel,
                onBack = onBack,
                onClaim = onClaim,
                onOpenReviews = onOpenReviews,
                onWriteReview = onWriteReview,
                onAddMenuItems = { addingMenuItems = true },
            )
        }
    }
}

private enum class ShopDialog { REPORT, SUGGEST_DELETE, DEACTIVATE }

@Composable
private fun ShopDetailsPage(
    viewModel: ShopDetailsViewModel,
    onBack: () -> Unit,
    onClaim: ((shopName: String) -> Unit)?,
    onOpenReviews: (shopName: String, photoUrl: String?) -> Unit,
    onWriteReview: (shopName: String, photoUrl: String?) -> Unit,
    onAddMenuItems: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var dialog by rememberSaveable { mutableStateOf<ShopDialog?>(null) }
    var fullScreenPhoto by remember { mutableStateOf<Int?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }

    Box(Modifier.fillMaxSize().background(colors.page)) {
        val shop = state.shop
        when {
            state.loading && shop == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.indigo)

            shop == null -> LoadFailed(state.error, onRetry = viewModel::retry)

            else -> {
                val photos = shop.imageUrls.orEmpty()
                val openReviews = { onOpenReviews(shop.shopName, photos.firstOrNull()) }
                val directions = {
                    viewModel.onDirections()
                    actions.openDirections(shop.latitude, shop.longitude)
                }
                val overlap = 22.dp
                val statusBar = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
                val heroHeight = if (photos.isEmpty()) 200.dp + statusBar else 300.dp

                val scroll = rememberScrollState()
                Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                    Box {
                        ShopHero(photos, heroHeight, overlap, onPhotoClick = { fullScreenPhoto = it })
                        Column(
                            Modifier
                                .padding(top = heroHeight - overlap)
                                .fillMaxWidth()
                                .background(colors.page, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ShopTitle(shop)
                            StatusCards(state)
                            ActionTiles(
                                canCall = state.canCall,
                                isSaved = state.isSaved,
                                onToggleSaved = viewModel::toggleSaved,
                                onCall = {
                                    shop.ownerPhone?.let { phone ->
                                        viewModel.onCall()
                                        actions.dial(phone)
                                    }
                                },
                                onShare = {
                                    scope.launch {
                                        actions.share(getString(Res.string.share_shop_text, shop.shopName, ExternalLinks.directions(shop.latitude, shop.longitude)))
                                    }
                                },
                                onReview = { onWriteReview(shop.shopName, photos.firstOrNull()) },
                            )
                            LocationCard(
                                shop, state.distanceMeters,
                                onDirections = directions,
                                onCopy = { address ->
                                    clipboard.setText(AnnotatedString(address))
                                    scope.launch { snackbar.showSnackbar(getString(Res.string.address_copied)) }
                                },
                            )
                            MenuCard(state.menu, shop.menuItems.orEmpty(), onAddItems = onAddMenuItems.takeIf { state.canEditMenu })
                            // A cart's seating and the like don't apply; its own details do
                            if (shop.isMovingCart == true) shop.cartDetails?.let { CartDetailsCard(it) } else FacilitiesCard(shop)
                            shop.description?.takeIf { it.isNotBlank() }?.let { DescriptionCard(it.trim()) }
                            onClaim?.takeIf { state.canClaim }?.let { claim -> ClaimCard { claim(shop.shopName) } }
                            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        }
                    }
                }

                // Once the photos scroll away, the status bar gets the page's colour so the clock never sits on text
                val density = LocalDensity.current
                Box(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .graphicsLayer {
                            val end = with(density) { (heroHeight - statusBar).toPx() }
                            val start = end - with(density) { 80.dp.toPx() }
                            alpha = ((scroll.value - start) / (end - start)).coerceIn(0f, 1f)
                        }
                        .background(colors.page),
                )

                ShopBottomBar(
                    reviewCount = shop.totalReviews,
                    directionsEnabled = state.isActive,
                    onReviews = openReviews,
                    onDirections = directions,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                )
            }
        }

        // The page dims while the ⋮ menu is open (design 2a)
        if (menuOpen) Box(Modifier.fillMaxSize().background(colors.menuScrim))

        // Back and ⋮ float over the photos, clear of the status bar
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FloatingIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onClick = onBack)
            val shop = state.shop
            if (shop != null) {
                ShopMoreMenu(
                    state = state,
                    open = menuOpen,
                    onOpenChange = { menuOpen = it },
                    onClaim = onClaim?.takeIf { state.canClaim }?.let { claim -> { claim(shop.shopName) } },
                    onReport = { dialog = ShopDialog.REPORT },
                    onSuggestDelete = { dialog = ShopDialog.SUGGEST_DELETE },
                    onDeactivate = { dialog = ShopDialog.DEACTIVATE },
                )
            }
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 84.dp))
    }

    val photos = state.shop?.imageUrls.orEmpty()
    fullScreenPhoto?.takeIf { it in photos.indices }?.let { index ->
        FullScreenPhotos(photos, initialIndex = index, onDismiss = { fullScreenPhoto = null })
    }

    val shop = state.shop
    when (dialog) {
        ShopDialog.REPORT -> ReportShopSheet(onReport = viewModel::report, onDismiss = { dialog = null })

        ShopDialog.SUGGEST_DELETE -> ConfirmShopSheet(
            icon = Icons.Rounded.Delete,
            title = Res.string.suggest_delete_title,
            text = Res.string.suggest_delete_text,
            confirm = Res.string.suggest_delete_confirm,
            shopName = shop?.shopName.orEmpty(),
            photoUrl = photos.firstOrNull(),
            onConfirm = viewModel::suggestDelete,
            onDismiss = { dialog = null },
        )

        ShopDialog.DEACTIVATE -> ConfirmShopSheet(
            icon = Icons.Rounded.VisibilityOff,
            title = Res.string.deactivate_title,
            text = Res.string.deactivate_text,
            confirm = Res.string.deactivate_confirm,
            shopName = shop?.shopName.orEmpty(),
            photoUrl = photos.firstOrNull(),
            onConfirm = viewModel::deactivate,
            onDismiss = { dialog = null },
        )

        null -> {}
    }
}

/** A translucent white rounded button over the photos; [solid] while its menu is open */
@Composable
private fun FloatingIconButton(icon: ImageVector, description: String, solid: Boolean = false, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.size(44.dp).clip(shape).background(colors.surface.copy(alpha = if (solid) 1f else 0.92f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = colors.ink, modifier = Modifier.size(22.dp))
    }
}

/**
 * ⋮: claim first, report, then the destructive action last in red (suggest deleting; for the owner, deactivate).
 * Hidden when none applies.
 */
@Composable
private fun ShopMoreMenu(
    state: ShopDetailsState,
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onClaim: (() -> Unit)?,
    onReport: () -> Unit,
    onSuggestDelete: () -> Unit,
    onDeactivate: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val showReport = !state.isOwner
    val destructive = state.canSuggestDelete || state.canDeactivate
    if (onClaim == null && !showReport && !destructive) return
    Box {
        FloatingIconButton(Icons.Rounded.MoreVert, stringResource(Res.string.more_options), solid = open) { onOpenChange(true) }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { onOpenChange(false) },
            shape = RoundedCornerShape(22.dp),
            containerColor = colors.surface,
            shadowElevation = 12.dp,
            modifier = Modifier.width(256.dp),
        ) {
            onClaim?.let {
                MenuAction(Icons.Rounded.Storefront, colors.saffronSoft, colors.saffronText, stringResource(Res.string.claim_screen_title), stringResource(Res.string.claim_title)) {
                    onOpenChange(false)
                    it()
                }
            }
            if (showReport) {
                MenuAction(
                    Icons.Rounded.Flag, colors.lavender, colors.accent,
                    stringResource(if (state.hasReported) Res.string.reported else Res.string.report_title),
                    enabled = state.canReport && !state.working,
                ) {
                    onOpenChange(false)
                    onReport()
                }
            }
            if (destructive) {
                if (onClaim != null || showReport) {
                    Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp).fillMaxWidth().height(1.dp).background(colors.lavender))
                }
                if (state.canDeactivate) {
                    MenuAction(Icons.Rounded.VisibilityOff, colors.dangerSoft, colors.danger, stringResource(Res.string.deactivate_title), danger = true, enabled = !state.working) {
                        onOpenChange(false)
                        onDeactivate()
                    }
                } else {
                    MenuAction(Icons.Rounded.Delete, colors.dangerSoft, colors.danger, stringResource(Res.string.suggest_delete_title), danger = true, enabled = !state.working) {
                        onOpenChange(false)
                        onSuggestDelete()
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuAction(
    icon: ImageVector,
    tile: Color,
    tint: Color,
    title: String,
    subtitle: String? = null,
    danger: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val alpha = if (enabled) 1f else 0.45f
    Row(
        Modifier
            .padding(horizontal = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(38.dp).background(tile.copy(alpha = tile.alpha * alpha), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint.copy(alpha = alpha), modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, color = (if (danger) colors.danger else colors.ink).copy(alpha = alpha), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            subtitle?.let { Text(it, color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun LoadFailed(error: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(error.orEmpty(), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(Res.string.try_again)) }
    }
}
