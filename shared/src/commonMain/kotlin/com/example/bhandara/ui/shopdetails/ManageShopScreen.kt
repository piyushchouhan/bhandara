package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.network.ShopWeekStats
import com.example.bhandara.data.network.ActivityApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.closed_now_desc
import com.example.bhandara.shared.resources.coming_soon
import com.example.bhandara.shared.resources.danger_zone
import com.example.bhandara.shared.resources.deactivate_confirm
import com.example.bhandara.shared.resources.deactivate_desc
import com.example.bhandara.shared.resources.deactivate_text
import com.example.bhandara.shared.resources.deactivate_title
import com.example.bhandara.shared.resources.delete_shop
import com.example.bhandara.shared.resources.delete_shop_desc
import com.example.bhandara.shared.resources.manage_shop
import com.example.bhandara.shared.resources.menu_item_count
import com.example.bhandara.shared.resources.menu_with_prices
import com.example.bhandara.shared.resources.open_now_desc
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.shared.resources.review_photos
import com.example.bhandara.shared.resources.shop_closed
import com.example.bhandara.shared.resources.shop_details_row
import com.example.bhandara.shared.resources.shop_open_now
import com.example.bhandara.shared.resources.stat_calls
import com.example.bhandara.shared.resources.stat_directions
import com.example.bhandara.shared.resources.stat_views
import com.example.bhandara.shared.resources.tab_reviews
import com.example.bhandara.shared.resources.this_week
import com.example.bhandara.shared.resources.timings
import com.example.bhandara.shared.resources.view
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * The owner's controls for their shop (design 1b): open or closed, this week's numbers, its parts, and a danger
 * zone to deactivate or delete it. Numbers, timings, the open switch and deleting say "Coming soon" until the
 * backend has them.
 *
 * @param onViewShop the shop page, as customers see it (also where details, menu and photos are edited)
 * @param onDeactivated after the shop was deactivated here
 */
@Composable
fun ManageShopScreen(
    shopId: String,
    onBack: () -> Unit,
    onViewShop: () -> Unit,
    onOpenReviews: (shopName: String, photoUrl: String?) -> Unit,
    onDeactivated: () -> Unit,
) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel {
        ShopDetailsViewModel(
            shopId, ShopApi(dependencies.apiClient), dependencies.settings, { dependencies.auth.currentUid }, null,
            activityApi = ActivityApi(dependencies.apiClient), ownerView = true,
        )
    }
    // Coming back (e.g. after editing the menu on the shop page) shows the shop as it is now
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.retry() else shownBefore = true
    }
    FeastTheme {
        ManageShopPage(viewModel, onBack, onViewShop, onOpenReviews, onDeactivated)
    }
}

@Composable
private fun ManageShopPage(
    viewModel: ShopDetailsViewModel,
    onBack: () -> Unit,
    onViewShop: () -> Unit,
    onOpenReviews: (String, String?) -> Unit,
    onDeactivated: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmingDeactivate by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    var deactivateAsked by rememberSaveable { mutableStateOf(false) }
    val comingSoon: () -> Unit = {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(getString(Res.string.coming_soon))
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            // Once deactivated, the profile says so
            if (!(deactivateAsked && !state.isActive)) snackbar.showSnackbar(it)
        }
    }
    LaunchedEffect(state.isActive, deactivateAsked) {
        if (deactivateAsked && !state.isActive) onDeactivated()
    }
    val deactivate = {
        deactivateAsked = true
        viewModel.deactivate()
    }

    Box(Modifier.fillMaxSize().background(colors.page)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                Text(
                    stringResource(Res.string.manage_shop), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                )
                Row(
                    Modifier.height(36.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender).clickable(onClick = onViewShop).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Rounded.Visibility, contentDescription = null, tint = colors.ink, modifier = Modifier.size(16.dp))
                    Text(stringResource(Res.string.view), color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
            val shop = state.shop
            when {
                shop != null -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ShopHeaderCard(shop, onToggleOpen = comingSoon)
                    ThisWeek(state.weekStats)
                    PartsCard(
                        shop, menuCount = state.menu.size,
                        onDetails = onViewShop,
                        onMenu = onViewShop,
                        onTimings = comingSoon,
                        onPhotos = onViewShop,
                        onReviews = { onOpenReviews(shop.shopName, shop.imageUrls?.firstOrNull()) },
                    )
                    DangerZone(
                        canDeactivate = state.canDeactivate && !state.working,
                        onDeactivate = { confirmingDeactivate = true },
                        onDelete = { deleting = true },
                    )
                }
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accent)
                }
                else -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.error.orEmpty(), color = colors.muted, fontSize = 15.sp, style = MaterialTheme.typography.bodyLarge)
                    FeastButton(stringResource(Res.string.try_again), onClick = viewModel::retry, style = FeastButtonStyle.SOFT)
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars))
    }

    val shop = state.shop
    if (confirmingDeactivate && shop != null) {
        ConfirmShopSheet(
            icon = Icons.Rounded.VisibilityOff,
            title = Res.string.deactivate_title,
            text = Res.string.deactivate_text,
            confirm = Res.string.deactivate_confirm,
            shopName = shop.shopName,
            photoUrl = shop.imageUrls?.firstOrNull(),
            onConfirm = deactivate,
            onDismiss = { confirmingDeactivate = false },
        )
    }
    if (deleting && shop != null) {
        DeleteShopSheet(
            shopName = shop.shopName,
            onDeactivate = if (state.canDeactivate) deactivate else null,
            onDelete = comingSoon,
            onDismiss = { deleting = false },
        )
    }
}

/** The shop on indigo, with whether customers see it open */
@Composable
private fun ShopHeaderCard(shop: LocalShopResponse, onToggleOpen: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val open = shop.isCurrentlyOpen == true
    Column(
        Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                val photo = shop.imageUrls?.firstOrNull()
                if (photo != null) {
                    AsyncImage(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Rounded.Storefront, contentDescription = null, tint = colors.onIndigo, modifier = Modifier.size(26.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    shop.shopName, color = colors.onIndigo, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium,
                )
                shopAddress(shop)?.let {
                    Text(
                        it, color = colors.headerSubtext, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onToggleOpen).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.size(10.dp).background(if (open) Color(0xFF4ADE80) else colors.headerSubtext, CircleShape))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(if (open) Res.string.shop_open_now else Res.string.shop_closed), color = colors.onIndigo, fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    stringResource(if (open) Res.string.open_now_desc else Res.string.closed_now_desc), color = colors.headerSubtext,
                    fontSize = 12.sp, style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = open,
                onCheckedChange = { onToggleOpen() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.saffron,
                    checkedThumbColor = colors.indigo,
                    checkedBorderColor = Color.Transparent,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.16f),
                    uncheckedThumbColor = colors.headerSubtext,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

/** Views, directions and calls this week (each person once a day; the owner's own don't count); "–" until known */
@Composable
private fun ThisWeek(stats: ShopWeekStats?) {
    val colors = MaterialTheme.feastColors
    Column {
        Text(
            stringResource(Res.string.this_week).uppercase(), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                stats?.views to Res.string.stat_views,
                stats?.directions to Res.string.stat_directions,
                stats?.calls to Res.string.stat_calls,
            ).forEach { (count, label) ->
                Column(Modifier.weight(1f).background(colors.surface, RoundedCornerShape(18.dp)).padding(12.dp)) {
                    Text(count?.let(::compactCount) ?: "–", color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(label), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PartsCard(
    shop: LocalShopResponse,
    menuCount: Int,
    onDetails: () -> Unit,
    onMenu: () -> Unit,
    onTimings: () -> Unit,
    onPhotos: () -> Unit,
    onReviews: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.feastColors.surface)) {
        PartRow(Icons.Rounded.EditNote, stringResource(Res.string.shop_details_row), null, onDetails)
        PartRow(Icons.Rounded.RestaurantMenu, stringResource(Res.string.menu_with_prices), menuCount.takeIf { it > 0 }?.let { stringResource(Res.string.menu_item_count, it) }, onMenu)
        PartRow(Icons.Rounded.Schedule, stringResource(Res.string.timings), openingHours(shop), onTimings)
        PartRow(Icons.Rounded.PhotoLibrary, stringResource(Res.string.review_photos), shop.imageUrls?.size?.takeIf { it > 0 }?.toString(), onPhotos)
        val rating = shop.averageRating?.takeIf { (shop.totalRatings ?: shop.totalReviews ?: 0) > 0 }
        PartRow(Icons.Rounded.Reviews, stringResource(Res.string.tab_reviews), rating?.let { "★ ${formatRating(it)}" }, onReviews, divider = false)
    }
}

@Composable
private fun PartRow(icon: ImageVector, label: String, value: String?, onClick: () -> Unit, divider: Boolean = true) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Text(label, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            value?.let { Text(it, color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.labelMedium) }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
    }
}

/** Deactivating and deleting, apart from everything else and outlined in red */
@Composable
private fun DangerZone(canDeactivate: Boolean, onDeactivate: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column {
        Text(
            stringResource(Res.string.danger_zone).uppercase(), color = colors.danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 8.dp),
        )
        val shape = RoundedCornerShape(20.dp)
        Column(Modifier.fillMaxWidth().clip(shape).background(colors.surface).border(1.5.dp, colors.dangerBorder, shape)) {
            DangerRow(
                Icons.Rounded.VisibilityOff, colors.saffronSoft, colors.saffronText,
                stringResource(Res.string.deactivate_title), colors.ink, stringResource(Res.string.deactivate_desc),
                enabled = canDeactivate, onClick = onDeactivate,
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dangerSoft))
            DangerRow(
                Icons.Rounded.Delete, colors.dangerSoft, colors.danger,
                stringResource(Res.string.delete_shop), colors.danger, stringResource(Res.string.delete_shop_desc),
                enabled = true, onClick = onDelete,
            )
        }
    }
}

@Composable
private fun DangerRow(
    icon: ImageVector,
    tile: Color,
    tint: Color,
    title: String,
    titleColor: Color,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).alpha(if (enabled) 1f else 0.45f).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(40.dp).background(tile, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(text, color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
        }
    }
}
