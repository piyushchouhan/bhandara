package com.example.bhandara.ui.addshop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AirportShuttle
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PedalBike
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.outlined.Tapas
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_stop
import com.example.bhandara.shared.resources.add_stops_instead
import com.example.bhandara.shared.resources.all_year
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cart_name
import com.example.bhandara.shared.resources.cart_on_map
import com.example.bhandara.shared.resources.cart_phone
import com.example.bhandara.shared.resources.cart_photos
import com.example.bhandara.shared.resources.cart_photos_text
import com.example.bhandara.shared.resources.cart_sells
import com.example.bhandara.shared.resources.cart_type
import com.example.bhandara.shared.resources.cart_type_cycle
import com.example.bhandara.shared.resources.cart_type_foot
import com.example.bhandara.shared.resources.cart_type_hand
import com.example.bhandara.shared.resources.cart_type_scooter
import com.example.bhandara.shared.resources.cart_type_tempo
import com.example.bhandara.shared.resources.day_letters
import com.example.bhandara.shared.resources.days_out
import com.example.bhandara.shared.resources.drag_to_reorder
import com.example.bhandara.shared.resources.go_live_ios
import com.example.bhandara.shared.resources.go_live_now
import com.example.bhandara.shared.resources.go_live_point_1
import com.example.bhandara.shared.resources.go_live_point_2
import com.example.bhandara.shared.resources.go_live_point_3
import com.example.bhandara.shared.resources.go_live_profile_hint
import com.example.bhandara.shared.resources.go_live_text
import com.example.bhandara.shared.resources.later
import com.example.bhandara.shared.resources.move_map_pin
import com.example.bhandara.shared.resources.my_location
import com.example.bhandara.shared.resources.no_fixed_route
import com.example.bhandara.shared.resources.no_route_note
import com.example.bhandara.shared.resources.pay_card
import com.example.bhandara.shared.resources.pay_cash
import com.example.bhandara.shared.resources.pay_upi
import com.example.bhandara.shared.resources.payment
import com.example.bhandara.shared.resources.photo_cart
import com.example.bhandara.shared.resources.photo_food
import com.example.bhandara.shared.resources.pick_all
import com.example.bhandara.shared.resources.remove_stop
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.save_stop
import com.example.bhandara.shared.resources.seasonal
import com.example.bhandara.shared.resources.sells_bread
import com.example.bhandara.shared.resources.sells_chai
import com.example.bhandara.shared.resources.sells_chaat
import com.example.bhandara.shared.resources.sells_eggs
import com.example.bhandara.shared.resources.sells_fruits
import com.example.bhandara.shared.resources.sells_icecream
import com.example.bhandara.shared.resources.sells_milk
import com.example.bhandara.shared.resources.sells_other
import com.example.bhandara.shared.resources.sells_sabzi
import com.example.bhandara.shared.resources.spot_hint
import com.example.bhandara.shared.resources.spot_it
import com.example.bhandara.shared.resources.stop_name
import com.example.bhandara.shared.resources.takes_orders
import com.example.bhandara.shared.resources.time_from
import com.example.bhandara.shared.resources.time_to
import com.example.bhandara.shared.resources.times_vary
import com.example.bhandara.shared.resources.times_vary_short
import com.example.bhandara.shared.resources.usual_stops
import com.example.bhandara.shared.resources.when_around
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.FeastFormField
import com.example.bhandara.ui.components.FeastSectionLabel
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.reportfeast.FeastTimeSheet
import com.example.bhandara.ui.theme.feastColors
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// The moving cart's steps (design "Add Shop Flow" 3a–3g light, 6a–6g dark). Only the frontend for now: the cart's
// type, route, payment and the rest are kept while adding but not sent to the backend yet.

internal val cartTypeLooks: Map<String, Pair<ImageVector, StringResource>> = mapOf(
    "HAND_CART" to (Icons.Outlined.ShoppingCart to Res.string.cart_type_hand),
    "CYCLE" to (Icons.Outlined.PedalBike to Res.string.cart_type_cycle),
    "SCOOTER" to (Icons.Outlined.TwoWheeler to Res.string.cart_type_scooter),
    "TEMPO" to (Icons.Outlined.AirportShuttle to Res.string.cart_type_tempo),
    "ON_FOOT" to (Icons.Outlined.ShoppingBasket to Res.string.cart_type_foot),
)

internal val cartSellsLooks: Map<String, Pair<ImageVector, StringResource>> = mapOf(
    "Sabzi" to (Icons.Outlined.Eco to Res.string.sells_sabzi),
    "Fruits" to (Icons.Outlined.LocalGroceryStore to Res.string.sells_fruits),
    "Milk & dairy" to (Icons.Outlined.WaterDrop to Res.string.sells_milk),
    "Bread & pav" to (Icons.Outlined.BakeryDining to Res.string.sells_bread),
    "Eggs" to (Icons.Outlined.Egg to Res.string.sells_eggs),
    "Chaat & snacks" to (Icons.Outlined.Tapas to Res.string.sells_chaat),
    "Chai & drinks" to (Icons.Outlined.LocalCafe to Res.string.sells_chai),
    "Ice cream" to (Icons.Outlined.Icecream to Res.string.sells_icecream),
    "Other" to (Icons.Rounded.Add to Res.string.sells_other),
)

internal val paymentLabels = mapOf("CASH" to Res.string.pay_cash, "UPI" to Res.string.pay_upi, "CARD" to Res.string.pay_card)

/**
 * A chip to pick: indigo when picked, its icon (or a tick) in saffron; outlined white otherwise
 *
 * @param pickedIcon the icon when picked; the tick when null
 */
@Composable
internal fun PickChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    pickedIcon: ImageVector? = Icons.Rounded.Check,
    height: Int = 40,
    fontSize: Int = 15,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .height(height.dp)
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = if (height < 40) 10.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        val shown = if (selected) pickedIcon ?: icon else icon
        shown?.let { Icon(it, contentDescription = null, tint = if (selected) colors.saffron else colors.muted, modifier = Modifier.size(16.dp)) }
        Text(
            label, color = if (selected) colors.onIndigo else colors.ink, fontSize = fontSize.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** A square tick box with its label, the whole row tappable */
@Composable
internal fun CheckRow(label: String, checked: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(enabled = enabled, onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val shape = RoundedCornerShape(7.dp)
        Box(
            Modifier.size(22.dp).clip(shape).background(if (checked) colors.indigo else Color.Transparent)
                .then(if (checked) Modifier else Modifier.border(2.dp, colors.border, shape)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onIndigo, modifier = Modifier.size(16.dp))
        }
        Text(label, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Lets a row run from edge to edge of a page padded by [inset] on each side, e.g. a row of chips that scrolls */
private fun Modifier.bleed(inset: Int) = layout { measurable, constraints ->
    val extra = (inset * 2).dp.roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.maxWidth + extra, maxWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-inset.dp.roundToPx(), 0) }
}

/** Step 2 for a cart (design 3c): its name, what it's on, what it sells and photos of the cart and the food */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CartBasicsStep(state: AddShopState, viewModel: AddShopViewModel) {
    val colors = MaterialTheme.feastColors
    val form = state.form
    var picking by remember { mutableStateOf<String?>(null) }

    FeastFormField(
        form.shopName, stringResource(Res.string.cart_name), viewModel::onShopNameChange, icon = Icons.Rounded.LocalShipping,
        iconTint = colors.saffronText, height = 56.dp,
    )

    Column {
        FeastSectionLabel(stringResource(Res.string.cart_type))
        Row(
            Modifier.bleed(20).horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CART_TYPES.forEach { type ->
                val (icon, label) = cartTypeLooks.getValue(type)
                PickChip(stringResource(label), form.cartType == type, { viewModel.setCartType(type) }, icon = icon, pickedIcon = icon, height = 36, fontSize = 13)
            }
        }
    }

    Column {
        Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.cart_sells), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(" " + stringResource(Res.string.pick_all), color = colors.muted, fontSize = 15.sp, style = MaterialTheme.typography.bodyMedium)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CART_SELLS.forEach { category ->
                val (icon, label) = cartSellsLooks.getValue(category)
                PickChip(stringResource(label), category in form.cartSells, { viewModel.toggleCartSells(category) }, icon = icon, height = 36, fontSize = 13)
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.cart_photos), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(Res.string.cart_photos_text), color = colors.muted, fontSize = 13.sp, lineHeight = 18.sp,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp),
            )
        }
        PhotoTile(Icons.Outlined.AddAPhoto, Res.string.photo_cart, colors.saffronSoft, colors.saffronTrack, colors.saffronText, form.cartPhotos.size, enabled = state.canAddPhotos) { picking = "cart" }
        PhotoTile(Icons.Outlined.AddPhotoAlternate, Res.string.photo_food, colors.lavenderSoft, colors.starOff, colors.accent, form.photos.size, enabled = state.canAddPhotos) { picking = "food" }
    }
    val all = form.cartPhotos.map { it to true } + form.photos.map { it to false }
    if (all.isNotEmpty()) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            all.forEach { (photo, isCart) ->
                PhotoThumb(photo, enabled = !state.submitting) { if (isCart) viewModel.removeCartPhoto(photo) else viewModel.removePhoto(photo) }
            }
        }
    }

    picking?.let { which ->
        PhotoSourceSheet(
            maxCount = MAX_SHOP_PHOTOS - form.photos.size - form.cartPhotos.size,
            onPicked = { if (which == "cart") viewModel.addCartPhotos(it) else viewModel.addPhotos(it) },
            onDismiss = { picking = null },
        )
    }
}

/** A dashed 64dp tile to add a kind of photo; shows how many were added */
@Composable
private fun PhotoTile(icon: ImageVector, label: StringResource, fill: Color, line: Color, tint: Color, count: Int, enabled: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fill)
            .drawBehind {
                drawRoundRect(line, cornerRadius = CornerRadius(18.dp.toPx()), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))))
            }
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Text(
            if (count > 0) "${stringResource(label)} · $count" else stringResource(label), color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun PhotoThumb(photo: PickedPhoto, enabled: Boolean, onRemove: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(colors.lavender)) {
        AsyncImage(photo.preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(72.dp))
        Box(
            Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)).clickable(enabled = enabled, onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

/** "4:00 – 5:30 PM" (AM / PM once when both are in the same half), or "Times vary" */
@Composable
private fun stopTimes(stop: CartStop): String = when {
    stop.timesVary || stop.from == null -> stringResource(Res.string.times_vary_short)
    stop.to == null -> clockTime(stop.from, withHalf = true)
    else -> "${clockTime(stop.from, withHalf = (stop.from.hour < 12) != (stop.to.hour < 12))} – ${clockTime(stop.to, withHalf = true)}"
}

/** "5:30", or "5:30 PM" */
internal fun clockTime(time: LocalTime, withHalf: Boolean): String {
    val hour12 = if (time.hour % 12 == 0) 12 else time.hour % 12
    val text = "$hour12:${time.minute.toString().padStart(2, '0')}"
    return if (withHalf) "$text ${if (time.hour < 12) "AM" else "PM"}" else text
}

/** Step 3 for a cart (design 3d): its usual stops on a drawn route, and the days it's out */
@Composable
internal fun RouteStep(form: ShopForm, viewModel: AddShopViewModel, onAddStop: () -> Unit, onEditStop: (CartStop) -> Unit) {
    val colors = MaterialTheme.feastColors
    RouteDrawing(form.stops.size)
    if (form.noFixedRoute) {
        Column(
            Modifier.fillMaxWidth().background(colors.saffronSoft, RoundedCornerShape(18.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Route, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
                Text(stringResource(Res.string.no_fixed_route), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            }
            Text(stringResource(Res.string.no_route_note), color = colors.saffronBody, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(Res.string.add_stops_instead), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { viewModel.setNoFixedRoute(false); onAddStop() }.padding(vertical = 4.dp),
            )
        }
        return
    }
    if (form.stops.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.usual_stops), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f),
                )
                if (form.stops.size > 1) {
                    Text(stringResource(Res.string.drag_to_reorder), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                }
            }
            StopsList(form.stops, onMove = viewModel::moveStop, onClick = onEditStop)
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(colors.starOff, cornerRadius = CornerRadius(16.dp.toPx()), style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))))
            }
            .clickable(onClick = onAddStop),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Outlined.AddLocationAlt, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Text(stringResource(Res.string.add_stop), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
    Column {
        FeastSectionLabel(stringResource(Res.string.days_out))
        val letters = stringResource(Res.string.day_letters).split(',')
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            letters.forEachIndexed { day, letter ->
                val on = day in form.daysOut
                val shape = RoundedCornerShape(12.dp)
                Box(
                    Modifier.weight(1f).height(38.dp).clip(shape).background(if (on) colors.indigo else colors.surface)
                        .then(if (on) Modifier else Modifier.border(1.5.dp, colors.border, shape))
                        .clickable { viewModel.toggleDay(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(letter.trim(), color = if (on) colors.onIndigo else colors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** A numbered dot for a stop */
@Composable
private fun StopNumber(number: Int, ring: Color? = null) {
    val colors = MaterialTheme.feastColors
    Box(
        Modifier.size(26.dp).background(colors.indigo, CircleShape).then(if (ring != null) Modifier.border(3.dp, ring, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text("$number", color = colors.onIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
    }
}

/** The stops in a white card; dragging a stop by its handle moves it in the round */
@Composable
private fun StopsList(stops: List<CartStop>, onMove: (from: Int, to: Int) -> Unit, onClick: (CartStop) -> Unit) {
    val colors = MaterialTheme.feastColors
    val current by rememberUpdatedState(stops)
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var rowHeight by remember { mutableFloatStateOf(1f) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface)) {
        stops.forEachIndexed { index, stop ->
            key(stop.id) {
                val dragging = draggingId == stop.id
                Column(
                    Modifier
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = if (dragging) dragOffset else 0f }
                        .onSizeChanged { rowHeight = it.height.toFloat() }
                        .background(if (dragging) colors.lavenderSoft else colors.surface)
                        .clickable { onClick(stop) },
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StopNumber(index + 1)
                        Column(Modifier.weight(1f)) {
                            Text(stop.name, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                            Text(stopTimes(stop), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
                        }
                        Box(
                            Modifier.size(36.dp).pointerInput(stop.id) {
                                detectVerticalDragGestures(
                                    onDragStart = { draggingId = stop.id; dragOffset = 0f },
                                    onDragEnd = { draggingId = null; dragOffset = 0f },
                                    onDragCancel = { draggingId = null; dragOffset = 0f },
                                ) { change, amount ->
                                    change.consume()
                                    dragOffset += amount
                                    val at = current.indexOfFirst { it.id == stop.id }
                                    if (dragOffset > rowHeight / 2 && at < current.lastIndex) {
                                        onMove(at, at + 1)
                                        dragOffset -= rowHeight
                                    } else if (dragOffset < -rowHeight / 2 && at > 0) {
                                        onMove(at, at - 1)
                                        dragOffset += rowHeight
                                    }
                                }
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.DragIndicator, contentDescription = null, tint = colors.disabledText, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (index < stops.lastIndex && !dragging) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
                }
            }
        }
    }
}

/** The route's dotted curve, as in the design's 320 × 130 drawing: two cubic curves */
private fun routePoint(t: Float): Offset {
    fun cubic(p0: Offset, p1: Offset, p2: Offset, p3: Offset, u: Float): Offset {
        val v = 1 - u
        return p0 * (v * v * v) + p1 * (3 * v * v * u) + p2 * (3 * v * u * u) + p3 * (u * u * u)
    }
    return if (t <= 0.5f) {
        cubic(Offset(50f, 36f), Offset(110f, 30f), Offset(150f, 60f), Offset(170f, 70f), t * 2)
    } else {
        cubic(Offset(170f, 70f), Offset(190f, 80f), Offset(250f, 98f), Offset(275f, 96f), (t - 0.5f) * 2)
    }
}

/** A drawn bit of map with the route dotted across it and a numbered dot for each stop (up to 8) */
@Composable
private fun RouteDrawing(stopCount: Int) {
    val colors = MaterialTheme.feastColors
    BoxWithConstraints(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(20.dp)).background(colors.mapLand)) {
        val scale = maxWidth.value / 320f
        Canvas(Modifier.matchParentSize()) {
            val s = size.width / 320f
            fun p(x: Float, y: Float) = Offset(x * s, y * s)
            val road = 9.dp.toPx()
            drawLine(colors.mapRoad, p(-10f, 40f), p(330f, 24f), road)
            drawLine(colors.mapRoad, p(150f, -10f), p(180f, 140f), road)
            drawPath(
                Path().apply {
                    p(-10f, 100f).let { moveTo(it.x, it.y) }
                    val (a, b, c) = Triple(p(100f, 92f), p(220f, 112f), p(330f, 90f))
                    cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
                },
                colors.mapRoad, style = Stroke(road),
            )
            drawPath(
                Path().apply {
                    p(50f, 36f).let { moveTo(it.x, it.y) }
                    cubicTo(110f * s, 30f * s, 150f * s, 60f * s, 170f * s, 70f * s)
                    cubicTo(190f * s, 80f * s, 250f * s, 98f * s, 275f * s, 96f * s)
                },
                colors.saffron,
                style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx()))),
            )
        }
        val shown = stopCount.coerceAtMost(8)
        repeat(shown) { i ->
            val point = routePoint(if (shown == 1) 0f else i.toFloat() / (shown - 1))
            Box(Modifier.offset(x = (point.x * scale).dp - 13.dp, y = (point.y * scale).dp - 13.dp)) {
                StopNumber(i + 1, ring = colors.surface)
            }
        }
    }
}

private enum class PickingTime { FROM, TO }

/**
 * Adding (or changing) a stop (design 3e): a map with a pin in the middle, the stop's name and its time window.
 * The pin's spot isn't kept yet (the map doesn't report where it was moved to).
 *
 * @param stop the stop to change; null to add one
 * @param onNoFixedRoute "It has no fixed route", offered when adding
 */
@Composable
internal fun AddStopSheet(
    stop: CartStop?,
    nextId: Long,
    onSave: (CartStop) -> Unit,
    onRemove: (CartStop) -> Unit,
    onNoFixedRoute: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    var name by remember { mutableStateOf(stop?.name.orEmpty()) }
    var from by remember { mutableStateOf(stop?.from) }
    var to by remember { mutableStateOf(stop?.to) }
    var timesVary by remember { mutableStateOf(stop?.timesVary ?: false) }
    var picking by remember { mutableStateOf<PickingTime?>(null) }
    val deviceLocation = rememberDeviceLocation()
    var here by remember { mutableStateOf<GeoLocation?>(null) }
    var camera by remember { mutableStateOf<CameraTarget?>(null) }
    LaunchedEffect(Unit) {
        here = (LocationService(deviceLocation).locate() as? LocationResult.Found)?.location
        here?.let { camera = CameraTarget(it, zoom = 16f) }
    }
    var sheetHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    Box(Modifier.fillMaxSize().background(colors.mapLand)) {
        val target = camera
        if (target != null) {
            PlatformMap(
                camera = target,
                pins = emptyList(),
                showMyLocation = false,
                darkMap = colors.isDark,
                onPinClick = {},
                look = MapLook.FEAST,
                contentPadding = PaddingValues(bottom = with(density) { sheetHeight.toDp() }),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            DrawnStreets()
        }
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) {
                SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onClose, Modifier.padding(16.dp))
                // The pin's tip marks the middle of the map
                Column(Modifier.align(Alignment.Center).padding(bottom = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.height(30.dp).background(colors.indigo, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(Res.string.move_map_pin), color = colors.onIndigo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    }
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = colors.saffron, modifier = Modifier.padding(top = 6.dp).size(44.dp))
                }
                here?.let { spot ->
                    SquareIconButton(
                        Icons.Outlined.MyLocation, stringResource(Res.string.my_location), { camera = CameraTarget(spot, zoom = 16f) },
                        Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { sheetHeight = it.height.toFloat() }
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.15f))
                    .background(colors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 40.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp)))
                Text(stringResource(Res.string.add_stop), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                FeastFormField(name, stringResource(Res.string.stop_name), { name = it.take(100) }, icon = Icons.Outlined.Place, height = 52.dp, textSize = 15)
                Row(Modifier.alpha(if (timesVary) 0.4f else 1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeCard(Res.string.time_from, from, enabled = !timesVary) { picking = PickingTime.FROM }
                    TimeCard(Res.string.time_to, to, enabled = !timesVary) { picking = PickingTime.TO }
                }
                CheckRow(stringResource(Res.string.times_vary), timesVary, { timesVary = !timesVary })
                FeastButton(
                    stringResource(Res.string.save_stop),
                    onClick = {
                        onSave(
                            CartStop(
                                id = stop?.id ?: nextId,
                                name = name.trim(),
                                from = from.takeUnless { timesVary },
                                to = to.takeUnless { timesVary },
                                timesVary = timesVary || from == null,
                            )
                        )
                    },
                    enabled = name.trim().length >= 2,
                    icon = Icons.Outlined.AddLocationAlt,
                    iconTint = colors.saffron,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (stop != null) {
                    Text(
                        stringResource(Res.string.remove_stop), color = colors.danger, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onRemove(stop) }.padding(vertical = 4.dp),
                    )
                } else {
                    Text(
                        stringResource(Res.string.no_fixed_route), color = colors.saffronText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onNoFixedRoute).padding(vertical = 4.dp),
                    )
                }
            }
        }
    }

    picking?.let { which ->
        val current = if (which == PickingTime.FROM) from else to
        FeastTimeSheet(
            initial = current ?: if (which == PickingTime.FROM) LocalTime(16, 0) else (from?.let { LocalTime((it.hour + 1) % 24, it.minute) } ?: LocalTime(18, 0)),
            onPicked = { time ->
                if (which == PickingTime.FROM) from = time else to = time
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun RowScope.TimeCard(label: StringResource, time: LocalTime?, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.weight(1f).height(52.dp).clip(shape).border(1.5.dp, colors.border, shape).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Column {
            Text(stringResource(label), color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
            Text(time?.let { clockTime(it, withHalf = true) } ?: "–", color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** Streets, a park and a river, drawn while the real map can't be shown (no location yet) */
@Composable
private fun DrawnStreets() {
    val colors = MaterialTheme.feastColors
    Canvas(Modifier.fillMaxSize().clipToBounds()) {
        val s = size.width / 360f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)
        drawLine(colors.mapWater, p(300f, -10f), p(340f, 400f), 40 * s)
        val road = 12 * s
        drawLine(colors.mapRoad, p(-10f, 160f), p(370f, 130f), road)
        drawLine(colors.mapRoad, p(190f, -10f), p(220f, 400f), road)
        drawPath(
            Path().apply {
                p(-10f, 300f).let { moveTo(it.x, it.y) }
                cubicTo(120 * s, 280 * s, 240 * s, 320 * s, 370 * s, 290 * s)
            },
            colors.mapRoad, style = Stroke(road),
        )
        drawRoundRect(colors.mapPark, topLeft = p(40f, 190f), size = Size(100 * s, 70 * s), cornerRadius = CornerRadius(14 * s))
    }
}

/** Step 5 for a cart (design 3f): contact for orders, payment, how to spot it, and when it's around */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CartDetailsStep(form: ShopForm, viewModel: AddShopViewModel) {
    val colors = MaterialTheme.feastColors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeastFormField(form.phone, stringResource(Res.string.cart_phone), viewModel::onPhoneChange, icon = Icons.Outlined.Call, height = 52.dp, keyboardType = KeyboardType.Phone)
        CheckRow(stringResource(Res.string.takes_orders), form.takesOrders, { viewModel.setTakesOrders(!form.takesOrders) })
    }
    Column {
        FeastSectionLabel(stringResource(Res.string.payment))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CART_PAYMENTS.forEach { way ->
                PickChip(stringResource(paymentLabels.getValue(way)), way in form.payments, { viewModel.togglePayment(way) }, fontSize = 14)
            }
        }
    }
    Column {
        FeastSectionLabel(stringResource(Res.string.spot_it))
        FeastFormField(form.spotHint, stringResource(Res.string.spot_hint), viewModel::onSpotHintChange, icon = Icons.Outlined.Visibility, height = 52.dp, textSize = 15)
    }
    Column {
        FeastSectionLabel(stringResource(Res.string.when_around))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickChip(stringResource(Res.string.all_year), !form.seasonal, { viewModel.setSeasonal(false) }, pickedIcon = null, height = 44, fontSize = 14, modifier = Modifier.weight(1f))
            SeasonalChip(form.seasonal) { viewModel.setSeasonal(true) }
        }
    }
}

/** "Seasonal", with a sun in amber (saffron once picked) */
@Composable
private fun RowScope.SeasonalChip(selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.weight(1f).height(44.dp).clip(shape).background(if (selected) colors.indigo else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape)).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = if (selected) colors.saffron else colors.saffronText, modifier = Modifier.size(16.dp))
        Text(stringResource(Res.string.seasonal), color = if (selected) colors.onIndigo else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * After a cart is added (design 3g): it's on the map, and Vendor Mode is explained before it asks to share the
 * cart's location
 *
 * @param onGoLive turns Vendor Mode on; null where there's none yet (iPhone), which then says so
 */
@Composable
internal fun GoLiveScreen(cartName: String, onLater: () -> Unit, onGoLive: (() -> Unit)?) {
    val colors = MaterialTheme.feastColors
    var notOnThisPhone by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(colors.indigo)) {
        Canvas(Modifier.fillMaxWidth().height(360.dp).padding(top = 60.dp)) {
            val s = size.width / 360f
            drawPath(
                Path().apply {
                    moveTo(-10 * s, 250 * s)
                    cubicTo(70 * s, 220 * s, 120 * s, 120 * s, 200 * s, 130 * s)
                    cubicTo(280 * s, 140 * s, 320 * s, 60 * s, 380 * s, 40 * s)
                },
                colors.saffron.copy(alpha = 0.6f),
                style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 9.dp.toPx()))),
            )
        }
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    stringResource(Res.string.later), color = colors.headerSubtext, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onLater).padding(horizontal = 6.dp, vertical = 10.dp),
                )
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        Modifier.size(120.dp).background(colors.saffron.copy(alpha = 0.18f), RoundedCornerShape(42.dp)).padding(12.dp)
                            .background(colors.saffron, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(48.dp))
                    }
                    Text(
                        stringResource(Res.string.cart_on_map, cartName), color = colors.onIndigo, fontSize = 24.sp, lineHeight = 29.sp,
                        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        stringResource(Res.string.go_live_text), color = colors.headerSubtext, fontSize = 15.sp, lineHeight = 22.sp,
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Column(
                    Modifier.padding(start = 20.dp, end = 20.dp, top = 26.dp).fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    listOf(
                        Icons.Rounded.ToggleOn to Res.string.go_live_point_1,
                        Icons.Outlined.TimerOff to Res.string.go_live_point_2,
                        Icons.Outlined.BatterySaver to Res.string.go_live_point_3,
                    ).forEachIndexed { i, (icon, text) ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                        Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(icon, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(22.dp))
                            Text(stringResource(text), color = colors.onIndigo, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeastButton(
                    stringResource(Res.string.go_live_now),
                    onClick = { if (onGoLive != null) onGoLive() else notOnThisPhone = true },
                    style = FeastButtonStyle.SAFFRON,
                    icon = Icons.Rounded.Sensors,
                    height = 56.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(if (notOnThisPhone) Res.string.go_live_ios else Res.string.go_live_profile_hint),
                    color = colors.headerSubtext, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
