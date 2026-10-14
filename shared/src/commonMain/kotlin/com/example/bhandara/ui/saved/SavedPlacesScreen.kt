package com.example.bhandara.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.ExternalLinks
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.action_call
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.action_share
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.community_feast
import com.example.bhandara.shared.resources.crowd_busy
import com.example.bhandara.shared.resources.feast_ended
import com.example.bhandara.shared.resources.feast_happening_now
import com.example.bhandara.shared.resources.my_location
import com.example.bhandara.shared.resources.saved_closed
import com.example.bhandara.shared.resources.saved_closed_opens
import com.example.bhandara.shared.resources.saved_empty_text
import com.example.bhandara.shared.resources.saved_empty_title
import com.example.bhandara.shared.resources.saved_explore
import com.example.bhandara.shared.resources.saved_filter_all
import com.example.bhandara.shared.resources.saved_filter_feasts
import com.example.bhandara.shared.resources.saved_filter_open
import com.example.bhandara.shared.resources.saved_list
import com.example.bhandara.shared.resources.saved_map
import com.example.bhandara.shared.resources.saved_no_longer_listed
import com.example.bhandara.shared.resources.saved_no_longer_text
import com.example.bhandara.shared.resources.saved_none_here
import com.example.bhandara.shared.resources.saved_open_now
import com.example.bhandara.shared.resources.saved_places
import com.example.bhandara.shared.resources.saved_remove
import com.example.bhandara.shared.resources.saved_remove_from
import com.example.bhandara.shared.resources.saved_removed
import com.example.bhandara.shared.resources.saved_sort
import com.example.bhandara.shared.resources.saved_sort_nearest
import com.example.bhandara.shared.resources.saved_sort_rating
import com.example.bhandara.shared.resources.saved_sort_recent
import com.example.bhandara.shared.resources.saved_taken_down
import com.example.bhandara.shared.resources.share_shop_text
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.shared.resources.undo
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.feasts.formatFeastHours
import com.example.bhandara.ui.feasts.formatFeastTime
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.MapPin
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.shopdetails.formatRating
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

// Saved places (design "Saved Places": 1a list, 1b map, 1c unsave + Undo, 1d place gone, 1e empty; dark 2a–2e).
// Saves stay on the phone. Left out for now: feast reminders, "Marked closed by N people" and "It's still open".

/**
 * The shops and feasts the user saved, from Profile → My activity
 *
 * @param onExploreMap "Explore the map": back to the map
 */
@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit,
    onOpenShop: (shopId: String) -> Unit,
    onOpenFeast: (feastId: String) -> Unit,
    onExploreMap: () -> Unit,
) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        SavedPlacesViewModel(
            dependencies.settings,
            ShopApi(dependencies.apiClient),
            FeastApi(dependencies.apiClient),
            LocationService(deviceLocation),
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val snackbar = remember { SnackbarHostState() }
    var sorting by rememberSaveable { mutableStateOf(false) }

    // "Removed …" with Undo (1c)
    LaunchedEffect(state.removed) {
        val removed = state.removed ?: return@LaunchedEffect
        snackbar.currentSnackbarData?.dismiss()
        val result = snackbar.showSnackbar(
            getString(Res.string.saved_removed, removed.name() ?: getString(Res.string.community_feast)),
            actionLabel = getString(Res.string.undo),
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.undo() else viewModel.onRemovedShown()
    }
    // Back from a shop or feast page: it may have been unsaved there
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.load() else shownBefore = true
    }

    val open: (SavedPlace) -> Unit = { place ->
        when (place) {
            is SavedPlace.Shop -> onOpenShop(place.id)
            is SavedPlace.Feast -> onOpenFeast(place.id)
        }
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            if (state.showMap && state.places.isNotEmpty()) {
                SavedMap(state, onSelect = viewModel::select, onOpen = open, onUnsave = viewModel::remove, onBack = onBack, onShowList = { viewModel.onShowMap(false) })
            } else {
                Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                        Text(
                            stringResource(Res.string.saved_places), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                        )
                        if (state.places.size > 1) {
                            SquareIconButton(Icons.AutoMirrored.Rounded.Sort, stringResource(Res.string.saved_sort), { sorting = true })
                        }
                    }
                    val error = state.error
                    when {
                        state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.indigo)
                        }
                        error != null -> Column(
                            Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        ) {
                            Text(error, color = colors.muted, fontSize = 15.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                            FeastButton(stringResource(Res.string.try_again), onClick = viewModel::load, style = FeastButtonStyle.SOFT, modifier = Modifier.padding(horizontal = 24.dp))
                        }
                        state.places.isEmpty() -> NothingSavedYet(onExploreMap, Modifier.weight(1f))
                        else -> {
                            ListMapToggle(showMap = false, onChange = viewModel::onShowMap, modifier = Modifier.padding(horizontal = 14.dp))
                            LazyColumn(
                                Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                item(key = "filters") { Filters(state, viewModel::onFilter) }
                                val shown = state.shown()
                                if (shown.isEmpty()) item { Nothing() }
                                items(shown, key = { it.key }) { place ->
                                    if (place.isGone) {
                                        GoneCard(place, onRemove = { viewModel.remove(place) })
                                    } else {
                                        PlaceCard(place, onOpen = { open(place) }, onUnsave = { viewModel.remove(place) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = if (state.showMap) 190.dp else 16.dp))
        }
        if (sorting) {
            SortSheet(state.sort, onSort = viewModel::onSort, onDismiss = { sorting = false })
        }
    }
}

/** The name shown for a place; null for a place that's gone without a trace */
private fun SavedPlace.name(): String? = when (this) {
    is SavedPlace.Shop -> shop?.shopName
    is SavedPlace.Feast -> feast?.organizerName?.takeIf { it.isNotBlank() }
}

@Composable
private fun SavedPlace.title(): String = name() ?: stringResource(Res.string.community_feast)

/** List / Map, as a segmented control */
@Composable
private fun ListMapToggle(showMap: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier, onSurface: Boolean = false) {
    val colors = MaterialTheme.feastColors
    Row(
        modifier.fillMaxWidth().height(48.dp).background(if (onSurface) colors.surface else colors.lavender, RoundedCornerShape(16.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Segment(Icons.AutoMirrored.Rounded.ViewList, stringResource(Res.string.saved_list), selected = !showMap, onSurface, Modifier.weight(1f)) { onChange(false) }
        Segment(Icons.Rounded.Map, stringResource(Res.string.saved_map), selected = showMap, onSurface, Modifier.weight(1f)) { onChange(true) }
    }
}

@Composable
private fun Segment(icon: ImageVector, label: String, selected: Boolean, onSurface: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    // On the list the picked side is white on lavender; over the map it's indigo on white
    val (fill, ink) = when {
        !selected -> Color.Transparent to colors.muted
        onSurface -> colors.indigo to colors.onIndigo
        else -> colors.surface to colors.ink
    }
    Row(
        modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(fill).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
        Text(label, color = ink, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Filters(state: SavedPlacesState, onFilter: (SavedFilter) -> Unit) {
    LazyRow(Modifier.padding(top = 14.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Chip(stringResource(Res.string.saved_filter_all, state.places.size), state.filter == SavedFilter.ALL) { onFilter(SavedFilter.ALL) } }
        item { Chip(stringResource(Res.string.saved_filter_open, state.openNowCount), state.filter == SavedFilter.OPEN_NOW) { onFilter(SavedFilter.OPEN_NOW) } }
        if (state.feastCount > 0) {
            item { Chip(stringResource(Res.string.saved_filter_feasts, state.feastCount), state.filter == SavedFilter.FEASTS, icon = Icons.Rounded.Celebration) { onFilter(SavedFilter.FEASTS) } }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface, shape)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = if (selected) colors.onIndigo else colors.saffronText, modifier = Modifier.size(16.dp)) }
        Text(label, color = if (selected) colors.onIndigo else colors.ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Nothing() {
    Text(
        stringResource(Res.string.saved_none_here), color = MaterialTheme.feastColors.muted, fontSize = 14.sp, textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
    )
}

/** A saved shop or feast that's still there (1a): picture or date, name, details, status, and the bookmark to unsave */
@Composable
private fun PlaceCard(place: SavedPlace, onOpen: () -> Unit, onUnsave: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val isFeast = place is SavedPlace.Feast
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(if (isFeast) colors.saffronSoft else colors.surface).clickable(onClick = onOpen).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (place) {
            is SavedPlace.Shop -> ShopThumb(place, 76.dp, faded = !place.isOpenNow)
            is SavedPlace.Feast -> FeastDateTile(place, 76.dp, live = true)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(place.title(), color = colors.ink, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Text(details(place), color = if (isFeast) colors.saffronText else colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Badges(place) }
        }
        Icon(
            Icons.Rounded.Bookmark, stringResource(Res.string.saved_remove_from), tint = if (isFeast) colors.saffronText else colors.accent,
            modifier = Modifier.align(Alignment.Top).clip(CircleShape).clickable(onClick = onUnsave).padding(4.dp).size(24.dp),
        )
    }
}

/** "Panipuri · ★ 4.6 · 110 m", or a feast's "12 PM – 3 PM · 1.2 km" */
@Composable
private fun details(place: SavedPlace): String {
    val distance = place.distanceMeters?.let(::formatDistance)
    return when (place) {
        is SavedPlace.Shop -> {
            val shop = place.shop
            listOfNotNull(
                (shop?.cuisineType ?: shop?.shopType)?.takeIf { it.isNotBlank() },
                shop?.averageRating?.takeIf { it > 0 }?.let { "★ " + formatRating(it) },
                distance,
            ).joinToString(" · ")
        }
        is SavedPlace.Feast -> listOfNotNull(place.feast?.let(::formatFeastHours), distance).joinToString(" · ")
    }
}

/** Open now / closed (with when it opens) and busy for a shop; "Happening now" for a feast on now */
@Composable
private fun Badges(place: SavedPlace) {
    val colors = MaterialTheme.feastColors
    when (place) {
        is SavedPlace.Shop -> {
            val shop = place.shop ?: return
            if (place.isOpenNow) {
                Badge(stringResource(Res.string.saved_open_now), colors.successSoft, colors.success, dot = true)
            } else if (shop.isCurrentlyOpen == false) {
                val opens = shop.openingTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                Badge(opens?.let { stringResource(Res.string.saved_closed_opens, formatFeastTime(it)) } ?: stringResource(Res.string.saved_closed), colors.lavender, colors.muted)
            }
            if (shop.crowdLevel == "BUSY") Badge(stringResource(Res.string.crowd_busy), colors.saffronSoft, colors.saffronText)
        }
        is SavedPlace.Feast -> if (place.isHappeningNow) Badge(stringResource(Res.string.feast_happening_now), colors.surface, colors.success, dot = true)
    }
}

@Composable
private fun Badge(text: String, fill: Color, ink: Color, dot: Boolean = false) {
    Row(
        Modifier.height(22.dp).background(fill, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (dot) Box(Modifier.size(6.dp).background(ink, CircleShape))
        Text(text, color = ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.labelSmall)
    }
}

/** The shop's first photo, or an icon (a truck for a cart) */
@Composable
private fun ShopThumb(place: SavedPlace.Shop, size: Dp, faded: Boolean = false) {
    val colors = MaterialTheme.feastColors
    val cart = place.shop?.isMovingCart == true
    Box(
        Modifier.size(size).clip(RoundedCornerShape(16.dp)).background(if (cart) colors.saffronSoft else colors.lavender).alpha(if (faded) 0.6f else 1f),
        contentAlignment = Alignment.Center,
    ) {
        val photo = place.shop?.imageUrls?.firstOrNull()
        if (photo != null) {
            AsyncImage(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(if (cart) Icons.Rounded.LocalShipping else Icons.Rounded.Storefront, contentDescription = null, tint = if (cart) colors.saffronText else colors.accent, modifier = Modifier.size(26.dp))
        }
    }
}

private val WEEKDAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

/** A feast's weekday and day: saffron while it's coming up, lavender once it's over */
@Composable
private fun FeastDateTile(place: SavedPlace.Feast, size: Dp, live: Boolean) {
    val colors = MaterialTheme.feastColors
    val date = place.feast?.let { runCatching { LocalDate.parse(it.feastDate) }.getOrNull() }
    Column(
        Modifier.size(size).background(if (live) colors.saffron else colors.lavender, RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (date != null) {
            Text(WEEKDAYS[date.dayOfWeek.ordinal], color = if (live) colors.onSaffron else colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, style = MaterialTheme.typography.labelSmall)
            Text(date.dayOfMonth.toString().padStart(2, '0'), color = if (live) colors.onSaffron else colors.ink, fontSize = if (size > 70.dp) 28.sp else 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        } else {
            Icon(Icons.Rounded.Celebration, contentDescription = null, tint = colors.muted, modifier = Modifier.size(26.dp))
        }
    }
}

/** A saved place that's gone (1d): a shop closed or taken off, or a feast that's over; only Remove is left */
@Composable
private fun GoneCard(place: SavedPlace, onRemove: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(22.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when (place) {
                is SavedPlace.Shop -> ShopThumb(place, 64.dp, faded = true)
                is SavedPlace.Feast -> FeastDateTile(place, 64.dp, live = false)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    place.title(), color = if (place is SavedPlace.Shop) colors.muted else colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    textDecoration = if (place is SavedPlace.Shop) TextDecoration.LineThrough else null,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall,
                )
                when (place) {
                    is SavedPlace.Shop -> Row(
                        Modifier.height(22.dp).background(colors.dangerSoft, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Block, contentDescription = null, tint = colors.danger, modifier = Modifier.size(13.dp))
                        Text(stringResource(Res.string.saved_no_longer_listed), color = colors.danger, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                    }
                    is SavedPlace.Feast -> Badge(
                        stringResource(if (place.feast?.status == "TAKEN_DOWN") Res.string.saved_taken_down else Res.string.feast_ended),
                        colors.lavender, colors.muted,
                    )
                }
            }
        }
        if (place is SavedPlace.Shop) {
            Text(stringResource(Res.string.saved_no_longer_text), color = colors.muted, fontSize = 13.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodySmall)
        }
        FeastButton(
            stringResource(if (place is SavedPlace.Shop) Res.string.saved_remove else Res.string.saved_remove_from), onClick = onRemove,
            style = FeastButtonStyle.SOFT, height = 44.dp, modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 1b: only the saved places on the map; tapping one shows its card with Directions, Call and Share */
@Composable
private fun SavedMap(
    state: SavedPlacesState,
    onSelect: (SavedPlace?) -> Unit,
    onOpen: (SavedPlace) -> Unit,
    onUnsave: (SavedPlace) -> Unit,
    onBack: () -> Unit,
    onShowList: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val actions = LocalPlatformActions.current
    val places = state.onMap()
    val selected = state.selected
    var camera by remember {
        val first = places.firstNotNullOfOrNull { it.spot() }
        mutableStateOf(CameraTarget(state.here ?: first ?: GeoLocation(18.52, 73.86), zoom = 13f))
    }
    val pins = places.mapNotNull { place ->
        val spot = place.spot() ?: return@mapNotNull null
        val picked = place.key == selected?.key
        when (place) {
            is SavedPlace.Shop -> MapPin(
                id = place.key, position = spot, label = place.title(), emoji = "🔖",
                isLiveCart = place.shop?.isMovingCart == true, selected = picked, dimmed = selected != null && !picked,
            )
            is SavedPlace.Feast -> MapPin(
                id = place.key, position = spot, label = place.title(), emoji = "🎉", selected = picked,
                dimmed = selected != null && !picked,
                feastPill = if (place.isHappeningNow) stringResource(Res.string.feast_happening_now) else place.feast?.startTime?.let { runCatching { formatFeastTime(LocalTime.parse(it)) }.getOrNull() },
                feastLive = place.isHappeningNow,
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        PlatformMap(
            camera = camera,
            pins = pins,
            showMyLocation = state.here != null,
            darkMap = colors.isDark,
            onPinClick = { pin -> onSelect(places.firstOrNull { it.key == pin.id }) },
            look = MapLook.FEAST,
            contentPadding = PaddingValues(top = 72.dp, bottom = if (selected != null) 170.dp else 0.dp),
            modifier = Modifier.fillMaxSize(),
        )
        // Top: back and the List / Map toggle, over the map
        Row(
            Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.statusBars).fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
            ListMapToggle(showMap = true, onChange = { if (!it) onShowList() }, onSurface = true, modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(16.dp)))
        }
        Column(
            Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.here?.let { here ->
                SquareIconButton(Icons.Rounded.MyLocation, stringResource(Res.string.my_location), { camera = CameraTarget(here, zoom = 15f) }, modifier = Modifier.align(Alignment.End))
            }
            if (selected != null) {
                val shareText = stringResource(Res.string.share_shop_text, selected.title(), selected.spot()?.let { ExternalLinks.directions(it.latitude, it.longitude) }.orEmpty())
                val phone = when (selected) {
                    is SavedPlace.Shop -> selected.shop?.ownerPhone
                    is SavedPlace.Feast -> selected.feast?.contactPhone
                }?.takeIf { it.isNotBlank() }
                Column(
                    Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)).background(colors.surface, RoundedCornerShape(24.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(Modifier.clip(RoundedCornerShape(16.dp)).clickable { onOpen(selected) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        when (selected) {
                            is SavedPlace.Shop -> ShopThumb(selected, 64.dp)
                            is SavedPlace.Feast -> FeastDateTile(selected, 64.dp, live = true)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(selected.title(), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                            Text(details(selected), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Badges(selected) }
                        }
                        Icon(
                            Icons.Rounded.Bookmark, stringResource(Res.string.saved_remove_from), tint = colors.accent,
                            modifier = Modifier.clip(CircleShape).clickable { onUnsave(selected) }.padding(4.dp).size(24.dp),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeastButton(
                            stringResource(Res.string.action_directions), onClick = {
                                selected.spot()?.let { actions.openDirections(it.latitude, it.longitude) }
                            },
                            icon = Icons.Rounded.Directions, iconTint = colors.saffron, height = 52.dp, modifier = Modifier.weight(1f),
                        )
                        if (phone != null) SmallAction(Icons.Outlined.Call, stringResource(Res.string.action_call)) { actions.dial(phone) }
                        SmallAction(Icons.Outlined.IosShare, stringResource(Res.string.action_share)) { actions.share(shareText) }
                    }
                }
            }
        }
    }
}

private fun SavedPlace.spot(): GeoLocation? = when (this) {
    is SavedPlace.Shop -> shop?.let { GeoLocation(it.latitude, it.longitude) }
    is SavedPlace.Feast -> feast?.let { GeoLocation(it.latitude, it.longitude) }
}

@Composable
private fun SmallAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(colors.lavender).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, description, tint = colors.ink, modifier = Modifier.size(22.dp))
    }
}

/** 1e: nothing saved yet; a sample card shows where the bookmark is */
@Composable
private fun NothingSavedYet(onExploreMap: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(112.dp).background(colors.lavender, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = colors.accent, modifier = Modifier.size(52.dp))
        }
        Text(stringResource(Res.string.saved_empty_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.saved_empty_text), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        // A sketch of a shop card with its bookmark
        Row(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(18.dp)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.size(44.dp).background(colors.lavender, RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth(0.7f).height(8.dp).background(colors.border, RoundedCornerShape(4.dp)))
                Box(Modifier.fillMaxWidth(0.45f).height(8.dp).background(colors.lavender, RoundedCornerShape(4.dp)))
            }
            Box(
                Modifier.size(40.dp).border(4.dp, colors.saffron.copy(alpha = 0.25f), CircleShape).padding(4.dp).background(colors.saffronSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(20.dp))
            }
        }
        FeastButton(
            stringResource(Res.string.saved_explore), onClick = onExploreMap, icon = Icons.Rounded.Map, iconTint = colors.saffron,
            height = 52.dp, modifier = Modifier.padding(top = 6.dp).padding(horizontal = 10.dp),
        )
    }
}

@Composable
private fun SortSheet(current: SavedSort, onSort: (SavedSort) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Text(
            stringResource(Res.string.saved_sort), color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
        )
        listOf(
            SavedSort.RECENT to Res.string.saved_sort_recent,
            SavedSort.NEAREST to Res.string.saved_sort_nearest,
            SavedSort.RATING to Res.string.saved_sort_rating,
        ).forEach { (sort, label) ->
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).clickable { close { onSort(sort) } }.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(label), color = colors.ink, fontSize = 15.sp, fontWeight = if (sort == current) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                if (sort == current) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}
