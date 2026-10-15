package com.example.bhandara.ui.shopdetails

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TakeoutDining
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.MenuItemResponse
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.action_call
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.action_review
import com.example.bhandara.shared.resources.action_save
import com.example.bhandara.shared.resources.action_saved
import com.example.bhandara.shared.resources.action_share
import com.example.bhandara.shared.resources.claim_text
import com.example.bhandara.shared.resources.claim_title
import com.example.bhandara.shared.resources.copy_address
import com.example.bhandara.shared.resources.facility_home_delivery
import com.example.bhandara.shared.resources.facility_online_payment
import com.example.bhandara.shared.resources.facility_seating
import com.example.bhandara.shared.resources.facility_takeaway
import com.example.bhandara.shared.resources.facility_wifi
import com.example.bhandara.shared.resources.info_cost_for_two
import com.example.bhandara.shared.resources.info_facilities
import com.example.bhandara.shared.resources.menu_add_items
import com.example.bhandara.shared.resources.menu_egg
import com.example.bhandara.shared.resources.menu_filter_all
import com.example.bhandara.shared.resources.menu_non_veg
import com.example.bhandara.shared.resources.menu_see_full
import com.example.bhandara.shared.resources.menu_show_less
import com.example.bhandara.shared.resources.menu_title
import com.example.bhandara.shared.resources.menu_unavailable
import com.example.bhandara.shared.resources.menu_veg
import com.example.bhandara.shared.resources.photo_counter
import com.example.bhandara.shared.resources.reviews_count
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.shop_closed
import com.example.bhandara.shared.resources.shop_distance_away
import com.example.bhandara.shared.resources.shop_hours_unknown
import com.example.bhandara.shared.resources.shop_inactive
import com.example.bhandara.shared.resources.shop_location_missing
import com.example.bhandara.shared.resources.shop_near_landmark
import com.example.bhandara.shared.resources.shop_open_now
import com.example.bhandara.shared.resources.shop_photo
import com.example.bhandara.shared.resources.shop_verified
import com.example.bhandara.ui.components.crowdLabel
import com.example.bhandara.ui.theme.FeastColors
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** How many menu items show before "See full menu" */
private const val MENU_PREVIEW = 5

/**
 * The photos across the top, swiping between them, with "1 / 4" and dots; a plain tile when there are none.
 * Runs under the status bar; [overlap] is how far the page below covers its bottom.
 */
@Composable
internal fun ShopHero(urls: List<String>, height: Dp, overlap: Dp, onPhotoClick: (Int) -> Unit) {
    val colors = MaterialTheme.feastColors
    if (urls.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(height).background(colors.mapLand), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Rounded.Storefront, contentDescription = null, tint = colors.muted.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = overlap).size(64.dp),
            )
        }
        return
    }
    val pager = rememberPagerState { urls.size }
    Box(Modifier.fillMaxWidth().height(height)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = stringResource(Res.string.shop_photo, page + 1),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(colors.lavender).clickable { onPhotoClick(page) },
            )
        }
        // Darker at the top and bottom, so the buttons and the counter read over any photo
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0x73141032), 0.3f to Color.Transparent, 0.7f to Color.Transparent, 1f to Color(0x59141032),
                ),
            ),
        )
        if (urls.size > 1) {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = overlap + 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                urls.indices.forEach { index ->
                    val current = index == pager.currentPage
                    Box(
                        Modifier
                            .size(width = if (current) 18.dp else 6.dp, height = 6.dp)
                            .background(Color.White.copy(alpha = if (current) 1f else 0.55f), RoundedCornerShape(3.dp)),
                    )
                }
            }
        }
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = overlap + 8.dp)
                .height(28.dp)
                .background(Color(0xBF1E1B4B), CircleShape)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Text(
                stringResource(Res.string.photo_counter, pager.currentPage + 1, urls.size),
                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/** The shop's name, and "Verified shop · Street food · ★ 3.0 (1)" under it */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShopTitle(shop: LocalShopResponse, onFssai: () -> Unit = {}) {
    val colors = MaterialTheme.feastColors
    val meta = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.muted)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            shop.shopName,
            color = colors.ink, fontSize = 25.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp,
            style = MaterialTheme.typography.headlineSmall,
        )
        val parts = buildList<@Composable () -> Unit> {
            if (shop.isVerified == true) add {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    Text(stringResource(Res.string.shop_verified), style = meta.copy(color = colors.accent, fontWeight = FontWeight.SemiBold))
                }
            }
            shopKind(shop)?.let { kind -> add { Text(kind, style = meta) } }
            shop.averageRating?.takeIf { (shop.totalReviews ?: shop.totalRatings ?: 0) > 0 }?.let { rating ->
                add {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(16.dp))
                        Text(formatRating(rating), style = meta.copy(color = colors.ink, fontWeight = FontWeight.SemiBold))
                        (shop.totalReviews ?: shop.totalRatings)?.let { Text("($it)", style = meta) }
                    }
                }
            }
        }
        if (parts.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                parts.forEachIndexed { index, part ->
                    if (index > 0) Text("·", style = meta, modifier = Modifier.align(Alignment.CenterVertically))
                    Box(Modifier.align(Alignment.CenterVertically)) { part() }
                }
            }
        }
        // A checked licence on a fixed shop (5a); carts never show it
        if (shop.fssai != null && shop.isMovingCart != true) Box(Modifier.padding(top = 4.dp)) { FssaiBadgeChip(onFssai) }
    }
}

/** Two cards side by side: open or closed with the timings, and how busy it is right now */
@Composable
internal fun StatusCards(state: ShopDetailsState) {
    val shop = state.shop ?: return
    val colors = MaterialTheme.feastColors
    val hours = openingHours(shop) ?: stringResource(Res.string.shop_hours_unknown)
    val crowd = crowdLabel(shop.crowdLevel)?.takeIf { state.isActive && shop.isCurrentlyOpen != false }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            !state.isActive -> InfoTile(colors.dangerSoft, Modifier.weight(1f)) {
                TileTitle(Icons.Rounded.Block, stringResource(Res.string.shop_inactive), colors.danger)
            }
            shop.isCurrentlyOpen == true -> InfoTile(colors.successSoft, Modifier.weight(1f)) {
                TileTitle(Icons.Rounded.Schedule, stringResource(Res.string.shop_open_now), colors.success)
                TileText(hours, colors.successText)
            }
            shop.isCurrentlyOpen == false -> InfoTile(colors.dangerSoft, Modifier.weight(1f)) {
                TileTitle(Icons.Rounded.Schedule, stringResource(Res.string.shop_closed), colors.danger)
                TileText(hours, colors.muted)
            }
            else -> InfoTile(colors.lavender, Modifier.weight(1f)) {
                TileTitle(Icons.Rounded.Schedule, hours, colors.ink, iconTint = colors.accent)
            }
        }
        if (crowd != null) {
            InfoTile(colors.lavender, Modifier.weight(1f)) {
                TileTitle(Icons.Rounded.Groups, stringResource(crowd), colors.ink, iconTint = colors.accent)
                val lit = crowdBars(shop.crowdLevel)
                val litColor = when (shop.crowdLevel) {
                    "BUSY" -> colors.danger
                    "MODERATE" -> colors.saffron
                    else -> colors.success
                }
                Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(4) { bar ->
                        Box(Modifier.weight(1f).height(5.dp).background(if (bar < lit) litColor else colors.border, RoundedCornerShape(3.dp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTile(background: Color, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.heightIn(min = 64.dp).background(background, RoundedCornerShape(16.dp)).padding(12.dp), content = content)
}

@Composable
private fun TileTitle(icon: ImageVector, text: String, color: Color, iconTint: Color = color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Text(text, color = color, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TileText(text: String, color: Color) {
    Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
}

/** Call (when there's a number), Share, Review and Save, as white tiles */
@Composable
internal fun ActionTiles(
    canCall: Boolean,
    isSaved: Boolean,
    onCall: () -> Unit,
    onShare: () -> Unit,
    onReview: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canCall) ActionTile(Icons.Rounded.Call, stringResource(Res.string.action_call), onCall)
        ActionTile(Icons.Rounded.Share, stringResource(Res.string.action_share), onShare)
        ActionTile(Icons.Rounded.RateReview, stringResource(Res.string.action_review), onReview)
        ActionTile(
            if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            stringResource(if (isSaved) Res.string.action_saved else Res.string.action_save),
            onToggleSaved,
        )
    }
}

@Composable
private fun RowScope.ActionTile(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.weight(1f).height(64.dp).clip(shape).background(colors.surface, shape).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Text(label, color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

/** A small drawn map, the address with landmark and distance, and copying the address; tapping opens directions */
@Composable
internal fun LocationCard(shop: LocalShopResponse, distanceMeters: Double?, onDirections: () -> Unit, onCopy: ((String) -> Unit)?) {
    val colors = MaterialTheme.feastColors
    val address = shopAddress(shop)
    val details = listOfNotNull(
        landmarkName(shop.landmark)?.let { stringResource(Res.string.shop_near_landmark, it) },
        distanceMeters?.let { stringResource(Res.string.shop_distance_away, formatDistance(it)) },
    ).joinToString(" · ")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface).clickable(onClick = onDirections)) {
        MiniMap(colors, Modifier.fillMaxWidth().height(84.dp))
        Row(
            Modifier.padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    address ?: stringResource(Res.string.shop_location_missing),
                    color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall,
                )
                if (details.isNotEmpty()) {
                    Text(details, color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (address != null && onCopy != null) {
                Box(Modifier.size(44.dp).clip(CircleShape).clickable { onCopy(address) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ContentCopy, stringResource(Res.string.copy_address), tint = colors.muted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/** A few streets, a dotted path from "you" to the shop's saffron pin: a picture, not the real map */
@Composable
private fun MiniMap(colors: FeastColors, modifier: Modifier) {
    Canvas(modifier.background(colors.mapLand)) {
        val sx = size.width / 324f
        val sy = size.height / 84f
        fun p(x: Float, y: Float) = Offset(x * sx, y * sy)
        val road = Stroke(width = 9.dp.toPx())
        drawLine(colors.mapRoad, p(-10f, 30f), p(340f, 14f), strokeWidth = road.width)
        drawLine(colors.mapRoad, p(150f, -10f), p(180f, 100f), strokeWidth = road.width)
        drawPath(
            Path().apply {
                p(-10f, 70f).let { moveTo(it.x, it.y) }
                val (a, b, c) = listOf(p(100f, 60f), p(220f, 84f), p(340f, 60f))
                cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
            },
            colors.mapRoad, style = road,
        )
        drawPath(
            Path().apply {
                p(80f, 66f).let { moveTo(it.x, it.y) }
                val (a, b, c) = listOf(p(110f, 50f), p(140f, 44f), p(160f, 40f))
                cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
            },
            colors.accent,
            style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 6.dp.toPx()))),
        )
        val ring = if (colors.isDark) colors.surface else Color.White
        val me = p(79f, 65f)
        drawCircle(ring, radius = 7.dp.toPx(), center = me)
        drawCircle(colors.accent, radius = 4.dp.toPx(), center = me)
        // The shop: a saffron drop, its tip on the end of the path
        val pinSize = 28.dp.toPx()
        val pin = p(166f, 36f)
        rotate(-45f, pivot = pin) {
            drawRoundRect(ring, topLeft = pin - Offset(pinSize / 2, pinSize / 2), size = Size(pinSize, pinSize), cornerRadius = CornerRadius(pinSize / 2))
            val inner = pinSize - 4.dp.toPx()
            drawRoundRect(colors.saffron, topLeft = pin - Offset(inner / 2, inner / 2), size = Size(inner, inner), cornerRadius = CornerRadius(inner / 2))
        }
    }
}

/** A white card on the page, like the menu or the description */
@Composable
internal fun PageCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(16.dp), content = content)
}

@Composable
internal fun CardTitle(text: String, action: (@Composable () -> Unit)? = null) {
    val colors = MaterialTheme.feastColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
private fun AddItemsLink(onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
        Text(stringResource(Res.string.menu_add_items), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * The menu: with prices and veg marks when the shop has one (a "Veg" filter for mixed menus, the first few items
 * until "See full menu"), else the plain list of dish names older shops have
 */
@Composable
internal fun MenuCard(menu: List<MenuItemResponse>, names: List<String>, onAddItems: (() -> Unit)?) {
    if (menu.isEmpty() && names.isEmpty() && onAddItems == null) return
    PageCard {
        CardTitle(stringResource(Res.string.menu_title), action = onAddItems?.let { { AddItemsLink(it) } })
        if (menu.isNotEmpty()) PricedMenu(menu) else if (names.isNotEmpty()) MenuNames(names)
    }
}

@Composable
private fun PricedMenu(allItems: List<MenuItemResponse>) {
    val colors = MaterialTheme.feastColors
    // Settings: "Veg only" starts the menu on veg items; egg items can be hidden altogether
    val preferences = LocalAppDependencies.current.settings.preferences
    val menu = menuWithPreferences(allItems, preferences.showEggItems)
    var vegOnly by rememberSaveable { mutableStateOf(preferences.vegOnly) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val filtered = if (vegOnly) menu.filter { isVeg(it.foodType) } else menu
    val shown = if (showAll) filtered else filtered.take(MENU_PREVIEW)

    if (showsVegFilter(menu)) {
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(stringResource(Res.string.menu_filter_all, menu.size), selected = !vegOnly) { vegOnly = false }
            FilterChip(stringResource(Res.string.menu_veg), selected = vegOnly, leading = { FoodTypeMarker("VEG", 10.dp) }) { vegOnly = true }
        }
    }
    Column(Modifier.padding(top = 6.dp)) {
        shown.forEachIndexed { index, item ->
            MenuRow(item, divider = index < shown.lastIndex)
        }
    }
    if (filtered.size > MENU_PREVIEW) {
        Box(
            Modifier.padding(top = 8.dp).fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavenderSoft).clickable { showAll = !showAll },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(if (showAll) Res.string.menu_show_less else Res.string.menu_see_full),
                color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, leading: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier
            .height(30.dp)
            .clip(CircleShape)
            .background(if (selected) colors.indigo else colors.lavender)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading?.invoke()
        Text(label, color = if (selected) colors.onIndigo else colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

/** One dish: veg mark, name and price, with a dashed line under it */
@Composable
private fun MenuRow(item: MenuItemResponse, divider: Boolean) {
    val colors = MaterialTheme.feastColors
    val faded = if (item.isAvailable) 1f else 0.4f
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .drawBehind {
                if (divider) {
                    drawLine(
                        colors.border, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                    )
                }
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FoodTypeMarker(item.foodType, 14.dp)
        Text(
            item.name,
            color = colors.ink.copy(alpha = faded), fontSize = 17.sp, fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
        )
        val price = item.price
        when {
            !item.isAvailable -> Text(stringResource(Res.string.menu_unavailable), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
            price != null -> Text(formatPrice(price, item.unit), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** Older shops only have dish names */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuNames(names: List<String>) {
    val colors = MaterialTheme.feastColors
    FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        names.forEach { name ->
            Row(
                Modifier.height(34.dp).background(colors.lavender, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                Text(name, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** The square veg / non-veg symbol used on Indian menus: a dot in a bordered square */
@Composable
private fun FoodTypeMarker(foodType: String, size: Dp) {
    val colors = MaterialTheme.feastColors
    val (color, label) = when {
        isVeg(foodType) -> colors.success to Res.string.menu_veg
        foodType == "NON_VEG" -> colors.danger to Res.string.menu_non_veg
        foodType == "EGG" -> colors.saffron to Res.string.menu_egg
        // Unknown type: keep the names aligned without claiming veg or non-veg
        else -> {
            Spacer(Modifier.size(size))
            return
        }
    }
    val description = stringResource(label)
    Box(
        Modifier.size(size).border(1.5.dp, color, RoundedCornerShape(2.dp)).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(size * 0.42f).background(color, CircleShape))
    }
}

/** What the shop offers besides food, and the cost for two, as saffron chips */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FacilitiesCard(shop: LocalShopResponse) {
    val colors = MaterialTheme.feastColors
    val list = facilities(shop)
    val cost = shop.averageCostForTwo?.takeIf { it > 0 }
    if (list.isEmpty() && cost == null) return
    PageCard {
        CardTitle(stringResource(Res.string.info_facilities))
        FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            list.forEach { FacilityChip(it.icon, stringResource(it.label)) }
            cost?.let { FacilityChip(Icons.Rounded.Payments, stringResource(Res.string.info_cost_for_two, it)) }
        }
    }
}

@Composable
internal fun FacilityChip(icon: ImageVector, label: String) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.height(34.dp).background(colors.saffronSoft, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(18.dp))
        Text(label, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
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

private val Facility.icon: ImageVector
    get() = when (this) {
        Facility.SEATING -> Icons.Rounded.Chair
        Facility.HOME_DELIVERY -> Icons.Rounded.DeliveryDining
        Facility.TAKEAWAY -> Icons.Rounded.TakeoutDining
        Facility.WIFI -> Icons.Rounded.Wifi
        Facility.ONLINE_PAYMENT -> Icons.Rounded.QrCode2
    }

@Composable
internal fun DescriptionCard(text: String) {
    val colors = MaterialTheme.feastColors
    PageCard {
        CardTitle(stringResource(Res.string.section_description))
        Text(text, color = colors.body, fontSize = 15.sp, lineHeight = 22.sp, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
    }
}

/** "Is this your shop?": a dashed saffron card for shops the real owner hasn't claimed yet */
@Composable
internal fun ClaimCard(onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.claimFill)
            .drawBehind {
                drawRoundRect(
                    colors.claimBorder, cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(42.dp).background(colors.saffron, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Storefront, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.claim_title), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(Res.string.claim_text), color = colors.muted, fontSize = 13.sp, lineHeight = 17.sp, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
    }
}

/** The floating bar at the bottom: "Reviews (n)" and the big Directions button, over a fade into the page */
@Composable
internal fun BoxScope.ShopBottomBar(reviewCount: Int?, directionsEnabled: Boolean, onReviews: () -> Unit, onDirections: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    Row(
        modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to colors.page.copy(alpha = 0f), 0.3f to colors.page))
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val reviewsShape = RoundedCornerShape(18.dp)
        Row(
            Modifier.weight(1f).height(56.dp).clip(reviewsShape).background(colors.lavender).clickable(onClick = onReviews),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Icon(Icons.Rounded.Reviews, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            Text(
                reviewCount?.let { stringResource(Res.string.reviews_count, it) } ?: stringResource(Res.string.action_review),
                color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        DirectionsButton(stringResource(Res.string.action_directions), enabled = directionsEnabled, onClick = onDirections, modifier = Modifier.weight(1.5f))
    }
}

/** The indigo Directions button with a saffron arrow, used on the page and on the map's preview card */
@Composable
internal fun DirectionsButton(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 56.dp) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .height(height)
            .shadow(if (enabled) 10.dp else 0.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.4f))
            .background(if (enabled) colors.indigo else colors.border, shape)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Rounded.Directions, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(22.dp))
        Text(label, color = colors.onIndigo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
    }
}
