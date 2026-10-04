package com.example.bhandara.ui.shopdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.MenuItemResponse
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.action_call
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.claim_text
import com.example.bhandara.shared.resources.claim_title
import com.example.bhandara.shared.resources.close
import com.example.bhandara.shared.resources.deactivate_title
import com.example.bhandara.shared.resources.facility_home_delivery
import com.example.bhandara.shared.resources.facility_online_payment
import com.example.bhandara.shared.resources.facility_seating
import com.example.bhandara.shared.resources.facility_takeaway
import com.example.bhandara.shared.resources.facility_wifi
import com.example.bhandara.shared.resources.info_average_cost
import com.example.bhandara.shared.resources.info_cost_for_two
import com.example.bhandara.shared.resources.info_facilities
import com.example.bhandara.shared.resources.info_shop_type
import com.example.bhandara.shared.resources.menu_add_items
import com.example.bhandara.shared.resources.menu_egg
import com.example.bhandara.shared.resources.menu_non_veg
import com.example.bhandara.shared.resources.menu_title
import com.example.bhandara.shared.resources.menu_unavailable
import com.example.bhandara.shared.resources.menu_veg
import com.example.bhandara.shared.resources.report_shop
import com.example.bhandara.shared.resources.reported
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.section_more_info
import com.example.bhandara.shared.resources.shop_distance_away
import com.example.bhandara.shared.resources.shop_inactive
import com.example.bhandara.shared.resources.shop_location_missing
import com.example.bhandara.shared.resources.shop_near_landmark
import com.example.bhandara.shared.resources.shop_open_now
import com.example.bhandara.shared.resources.shop_photo
import com.example.bhandara.shared.resources.shop_verified
import com.example.bhandara.shared.resources.suggest_delete_title
import com.example.bhandara.ui.components.CrowdBadge
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Everything on the Overview tab, top to bottom */
@Composable
internal fun ShopOverview(
    state: ShopDetailsState,
    onDeactivate: () -> Unit,
    onSuggestDelete: () -> Unit,
    onReport: () -> Unit,
    onAddMenuItems: (() -> Unit)?,
    onClaim: (() -> Unit)?,
    onDirections: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shop = state.shop ?: return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ShopHeader(state, onDeactivate = onDeactivate, onSuggestDelete = onSuggestDelete)
        InfoCard(shop, state.distanceMeters)

        // The menu with prices when the owner added one, else the plain list of names
        val names = shop.menuItems.orEmpty()
        when {
            state.menu.isNotEmpty() -> PricedMenu(state.menu, onAddMenuItems)
            names.isNotEmpty() || onAddMenuItems != null -> MenuNames(names, onAddMenuItems)
        }

        shop.description?.takeIf { it.isNotBlank() }?.let { SectionCard(Res.string.section_description) { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        MoreInfo(shop)
        onClaim?.let { ClaimCard(it) }

        // Owners manage their shop instead of reporting it
        if (!state.isOwner) {
            TextButton(
                onClick = onReport,
                enabled = state.canReport && !state.working,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (state.hasReported) Res.string.reported else Res.string.report_shop))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onDirections, enabled = state.isActive, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.action_directions))
            }
            if (state.canCall) {
                OutlinedButton(onClick = onCall, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.action_call))
                }
            }
        }
    }
}

/** The shop's photos side by side; tapping one opens them full screen */
@Composable
internal fun ShopPhotos(urls: List<String>, onPhotoClick: (Int) -> Unit) {
    if (urls.isEmpty()) {
        Box(
            Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(urls) { index, url ->
            AsyncImage(
                model = url,
                contentDescription = stringResource(Res.string.shop_photo, index + 1),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    // A single photo fills the width; with more, the next one peeks in to show there are more
                    .then(if (urls.size == 1) Modifier.fillParentMaxWidth() else Modifier.width(300.dp))
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPhotoClick(index) },
            )
        }
    }
}

/** Photos full screen, swiping between them */
@Composable
internal fun FullScreenPhotos(
    urls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    photoLabel: StringResource = Res.string.shop_photo,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            val pager = rememberPagerState(initialPage = initialIndex) { urls.size }
            HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = urls[page],
                    contentDescription = stringResource(photoLabel, page + 1),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (urls.size > 1) {
                    Text("${pager.currentPage + 1} / ${urls.size}", color = Color.White, modifier = Modifier.padding(start = 12.dp))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.close), tint = Color.White)
                }
            }
        }
    }
}

/** Name, verified badge, open / inactive, how busy, and the delete button (deactivate for the owner) */
@Composable
private fun ShopHeader(state: ShopDetailsState, onDeactivate: () -> Unit, onSuggestDelete: () -> Unit) {
    val shop = state.shop ?: return
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(shop.shopName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (shop.isVerified == true) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(Res.string.shop_verified), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!state.isActive) {
                    StatusChip(Res.string.shop_inactive, Icons.Default.Cancel, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                } else if (shop.isCurrentlyOpen == true) {
                    StatusChip(Res.string.shop_open_now, Icons.Default.CheckCircle, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                }
                // How busy it is right now; not for closed or inactive shops
                if (state.isActive && shop.isCurrentlyOpen != false) CrowdBadge(shop.crowdLevel)
            }
        }
        if (state.canDeactivate || (!state.isOwner && state.isActive)) {
            IconButton(
                onClick = if (state.isOwner) onDeactivate else onSuggestDelete,
                enabled = (state.canDeactivate || state.canSuggestDelete) && !state.working,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = stringResource(if (state.isOwner) Res.string.deactivate_title else Res.string.suggest_delete_title),
                )
            }
        }
    }
}

@Composable
private fun StatusChip(label: StringResource, icon: ImageVector, container: Color, content: Color) {
    AssistChip(
        onClick = {},
        label = { Text(stringResource(label)) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        colors = AssistChipDefaults.assistChipColors(containerColor = container, labelColor = content, leadingIconContentColor = content),
        border = null,
    )
}

/** Timings, address, landmark and distance */
@Composable
private fun InfoCard(shop: LocalShopResponse, distanceMeters: Double?) {
    val onCard = MaterialTheme.colorScheme.onPrimaryContainer
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            openingHours(shop)?.let { hours ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = onCard)
                    Text(hours, style = MaterialTheme.typography.bodyLarge, color = onCard)
                }
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = onCard)
                Column {
                    Text(shopAddress(shop) ?: stringResource(Res.string.shop_location_missing), style = MaterialTheme.typography.bodyLarge, color = onCard)
                    shop.landmark?.takeIf { it.isNotBlank() }?.let {
                        Text(stringResource(Res.string.shop_near_landmark, it.trim()), style = MaterialTheme.typography.bodyMedium, color = onCard.copy(alpha = 0.8f))
                    }
                    distanceMeters?.let {
                        Text(stringResource(Res.string.shop_distance_away, formatDistance(it)), style = MaterialTheme.typography.bodySmall, color = onCard.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

/** A titled card, like "Description" */
@Composable
private fun SectionCard(title: StringResource, action: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun AddItemsButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.menu_add_items))
    }
}

/** The menu with a veg / non-veg marker and price for each item */
@Composable
private fun PricedMenu(menu: List<MenuItemResponse>, onAddItems: (() -> Unit)?) {
    SectionCard(Res.string.menu_title, action = onAddItems?.let { { AddItemsButton(it) } }) {
        menu.forEach { item ->
            val faded = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FoodTypeMarker(item.foodType)
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.isAvailable) MaterialTheme.colorScheme.onSurface else faded,
                    modifier = Modifier.weight(1f),
                )
                val price = item.price
                when {
                    !item.isAvailable -> Text(stringResource(Res.string.menu_unavailable), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    price != null -> Text(formatPrice(price), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Older shops only have item names */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuNames(names: List<String>, onAddItems: (() -> Unit)?) {
    SectionCard(Res.string.menu_title, action = onAddItems?.let { { AddItemsButton(it) } }) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            names.forEach { name ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(name) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) },
                )
            }
        }
    }
}

/** The square veg / non-veg symbol used on Indian menus: a dot in a bordered square */
@Composable
private fun FoodTypeMarker(foodType: String) {
    val (color, label) = when (foodType) {
        "VEG", "VEGAN", "JAIN" -> Color(0xFF2E7D32) to Res.string.menu_veg
        "NON_VEG" -> Color(0xFFC62828) to Res.string.menu_non_veg
        "EGG" -> Color(0xFFF9A825) to Res.string.menu_egg
        // Unknown type: keep the names aligned without claiming veg or non-veg
        else -> {
            Spacer(Modifier.size(16.dp))
            return
        }
    }
    val description = stringResource(label)
    Box(
        Modifier
            .size(16.dp)
            .border(1.5.dp, color, RoundedCornerShape(2.dp))
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
    }
}

/** Shop type, average cost and facilities; nothing when none is known */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoreInfo(shop: LocalShopResponse) {
    val type = shopTypeLine(shop)
    val cost = shop.averageCostForTwo?.takeIf { it > 0 }
    val facilities = facilities(shop)
    if (type == null && cost == null && facilities.isEmpty()) return

    SectionCard(Res.string.section_more_info) {
        type?.let { InfoRow(Icons.Default.Store, Res.string.info_shop_type, it) }
        cost?.let { InfoRow(Icons.Default.Payments, Res.string.info_average_cost, stringResource(Res.string.info_cost_for_two, it)) }
        if (facilities.isNotEmpty()) {
            Text(stringResource(Res.string.info_facilities), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                facilities.forEach { SuggestionChip(onClick = {}, label = { Text(stringResource(it.label)) }) }
            }
        }
    }
}

private val Facility.label: StringResource
    get() = when (this) {
        Facility.SEATING -> Res.string.facility_seating
        Facility.HOME_DELIVERY -> Res.string.facility_home_delivery
        Facility.TAKEAWAY -> Res.string.facility_takeaway
        Facility.WIFI -> Res.string.facility_wifi
        Facility.ONLINE_PAYMENT -> Res.string.facility_online_payment
    }

@Composable
private fun InfoRow(icon: ImageVector, title: StringResource, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(stringResource(title), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** "Is this your shop?" for shops the platform listed and the real owner hasn't claimed yet */
@Composable
private fun ClaimCard(onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.claim_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(Res.string.claim_text), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
