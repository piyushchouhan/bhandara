package com.example.bhandara.ui.feasts

import com.example.bhandara.data.network.ActivityApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RiceBowl
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SoupKitchen
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.platform.ExternalLinks
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.feast_unsave
import com.example.bhandara.shared.resources.feast_save
import com.example.bhandara.shared.resources.action_call
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.action_share
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.close
import com.example.bhandara.shared.resources.feast_capacity
import com.example.bhandara.shared.resources.feast_capacity_value
import com.example.bhandara.shared.resources.feast_find_other
import com.example.bhandara.shared.resources.feast_food_type
import com.example.bhandara.shared.resources.feast_happening_now
import com.example.bhandara.shared.resources.feast_menu_label
import com.example.bhandara.shared.resources.feast_organizer_label
import com.example.bhandara.shared.resources.feast_photo
import com.example.bhandara.shared.resources.feast_report
import com.example.bhandara.shared.resources.feast_report_text
import com.example.bhandara.shared.resources.feast_report_title
import com.example.bhandara.shared.resources.feast_taken_down
import com.example.bhandara.shared.resources.feast_taken_down_info
import com.example.bhandara.shared.resources.feast_unnamed
import com.example.bhandara.shared.resources.photo_counter
import com.example.bhandara.shared.resources.report_submit
import com.example.bhandara.shared.resources.reported
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.share_shop_text
import com.example.bhandara.shared.resources.shop_distance_away
import com.example.bhandara.shared.resources.shop_location_missing
import com.example.bhandara.shared.resources.shop_near_landmark
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.shopdetails.landmarkName
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * One community feast's page, shared by Android and iOS (the redesign's 1c / 1d): an invitation ticket with the
 * date stub, whether it's on and a live countdown, where it is, then what's served as big plates, food type and
 * how many people, the organizer, and reporting a fake feast. A taken-down feast turns grey and stamped, with no
 * Directions. Tapping the photo opens it full screen with the feast's ticket (2b).
 *
 * @param onFindOtherFeasts leaves a taken-down feast for the feasts map
 */
@Composable
fun FeastDetailsScreen(feastId: String, onBack: () -> Unit, onFindOtherFeasts: () -> Unit) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        FeastDetailsViewModel(
            feastId,
            FeastApi(dependencies.apiClient),
            dependencies.settings,
            { dependencies.auth.currentUid },
            LocationService(deviceLocation),
            uploadPhotos = { photos -> dependencies.photoUploader.upload(photos) },
            activityApi = ActivityApi(dependencies.apiClient),
        )
    }
    FeastTheme { FeastDetailsPage(viewModel, onBack, onFindOtherFeasts) }
}

/** The plates under "What's being served" take turns with these */
private val PLATE_ICONS = listOf(
    Icons.Rounded.RiceBowl, Icons.Rounded.BakeryDining, Icons.Rounded.SoupKitchen,
    Icons.Rounded.LunchDining, Icons.Rounded.LocalCafe, Icons.Rounded.Icecream,
)

@Composable
private fun FeastDetailsPage(viewModel: FeastDetailsViewModel, onBack: () -> Unit, onFindOtherFeasts: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var confirmingReport by rememberSaveable { mutableStateOf(false) }
    var viewingPhoto by remember { mutableStateOf<Int?>(null) }
    val now by rememberNow()

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }

    val feast = state.feast
    val takenDown = feast != null && !state.isActive
    val timing = feast?.let { feastTiming(it, now) } ?: FeastTiming.Unknown
    val canGo = feast != null && state.isActive && timing != FeastTiming.Ended
    val directions = {
        feast?.let {
            viewModel.onDirections()
            actions.openDirections(it.latitude, it.longitude)
        }
    }
    val share = {
        feast?.let {
            scope.launch { actions.share(getString(Res.string.share_shop_text, it.organizerName ?: "", ExternalLinks.directions(it.latitude, it.longitude))) }
        }
    }
    val distanceLabel = listOfNotNull(stringResource(Res.string.action_directions), state.distanceMeters?.let(::formatDistance)).joinToString(" · ")

    Box(Modifier.fillMaxSize().background(if (takenDown) colors.fadedPage else colors.saffronSoft)) {
        when {
            state.loading && feast == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.indigo)

            feast == null -> Column(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.error.orEmpty(), color = colors.muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = viewModel::retry) { Text(stringResource(Res.string.try_again)) }
            }

            takenDown -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                FeastHero(feast.imageUrls, faded = true, onPhotoClick = {})
                TakenDownTicket(feast)
                Row(
                    Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp).fillMaxWidth().background(colors.dangerSoft, RoundedCornerShape(18.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = colors.danger, modifier = Modifier.size(22.dp))
                    Text(stringResource(Res.string.feast_taken_down_info), color = colors.dangerText, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(120.dp))
            }

            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                FeastHero(feast.imageUrls, faded = false, onPhotoClick = { viewingPhoto = it })
                FeastTicket(feast, timing, now, state.distanceMeters)
                Column(
                    Modifier.padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Served(feast.menuItems)
                    FoodTypeAndPeople(feast)
                    feast.description?.takeIf { it.isNotBlank() }?.let { text ->
                        Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(18.dp)).padding(14.dp)) {
                            Text(stringResource(Res.string.section_description), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                            Text(text.trim(), color = colors.body, fontSize = 15.sp, lineHeight = 22.sp, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Organizer(feast, canCall = state.canCall, onCall = { feast.contactPhone?.let(actions::dial) })
                    if (!state.isOrganizer) {
                        val enabled = state.canReport && !state.reporting
                        Row(
                            Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp)).clickable(enabled = enabled) { confirmingReport = true },
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val tint = colors.danger.copy(alpha = if (enabled) 1f else 0.5f)
                            Icon(Icons.Rounded.Flag, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                            Text(stringResource(if (state.hasReported) Res.string.reported else Res.string.feast_report), color = tint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }

        // Back floats over the photo, clear of the status bar
        Box(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 14.dp, vertical = 6.dp)) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.surface.copy(alpha = 0.92f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), tint = colors.ink, modifier = Modifier.size(22.dp))
            }
        }

        // Bottom: share and Directions; a taken-down feast offers other feasts instead
        if (feast != null) {
            val page = if (takenDown) colors.fadedPage else colors.saffronSoft
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(0f to page.copy(alpha = 0f), 0.3f to page))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (takenDown) {
                    FeastButton(
                        stringResource(Res.string.feast_find_other), onClick = onFindOtherFeasts, style = FeastButtonStyle.SAFFRON,
                        icon = Icons.Rounded.VolunteerActivism, height = 56.dp, modifier = Modifier.weight(1f),
                    )
                } else {
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(colors.surface).clickable { viewModel.toggleSaved() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (state.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            stringResource(if (state.isSaved) Res.string.feast_unsave else Res.string.feast_save),
                            tint = colors.saffronText, modifier = Modifier.size(22.dp),
                        )
                    }
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(colors.surface).clickable { share() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Share, stringResource(Res.string.action_share), tint = colors.accent, modifier = Modifier.size(22.dp))
                    }
                    DirectionsBar(distanceLabel, enabled = canGo, onClick = { directions() }, modifier = Modifier.weight(1f), height = 56.dp)
                }
            }
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 84.dp))
    }

    val photos = feast?.imageUrls.orEmpty()
    viewingPhoto?.takeIf { feast != null && it in photos.indices }?.let { index ->
        FeastPhotoViewer(
            feast = feast!!,
            initialIndex = index,
            now = now,
            directionsLabel = distanceLabel,
            canGo = canGo,
            onDirections = { directions() },
            onShare = { share() },
            onDismiss = { viewingPhoto = null },
        )
    }

    if (confirmingReport) {
        ReportFeastSheet(onReport = viewModel::report, onDismiss = { confirmingReport = false })
    }
}

/** The photos across the top (a striped tile when there are none), with "1 / 3"; greyed for a taken-down feast */
@Composable
private fun FeastHero(urls: List<String>, faded: Boolean, onPhotoClick: (Int) -> Unit) {
    val colors = MaterialTheme.feastColors
    val height = 250.dp
    val stripeA = if (faded) colors.fadedStripeA else colors.feastStripeA
    val stripeB = if (faded) colors.fadedStripeB else colors.feastStripeB
    if (urls.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(height).stripes(stripeA, stripeB, 14.dp), contentAlignment = Alignment.Center) {
            if (!faded) Icon(Icons.Rounded.Image, contentDescription = null, tint = colors.saffronText, modifier = Modifier.padding(bottom = 30.dp).size(40.dp))
        }
        return
    }
    val pager = rememberPagerState { urls.size }
    val grey = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    Box(Modifier.fillMaxWidth().height(height).stripes(stripeA, stripeB, 14.dp)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = stringResource(Res.string.feast_photo, page + 1),
                contentScale = ContentScale.Crop,
                colorFilter = if (faded) grey else null,
                modifier = Modifier.fillMaxSize().alpha(if (faded) 0.6f else 1f).clickable(enabled = !faded) { onPhotoClick(page) },
            )
        }
        if (!faded) {
            Row(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 44.dp)
                    .height(26.dp)
                    .background(Color(0xBF1E1B4B), CircleShape)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(
                    stringResource(Res.string.photo_counter, pager.currentPage + 1, urls.size),
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/** The white ticket overlapping the photo: stub and name, a perforation, then when and where */
@Composable
private fun TicketCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .padding(horizontal = 14.dp)
            .pullUp(30.dp)
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Color(0xFFB45309), spotColor = Color(0xFFB45309).copy(alpha = if (colors.isDark) 0.5f else 0.25f))
            .background(colors.surface, shape),
        content = content,
    )
}

/** Moves the content up by [by] over what's above it, and takes that much less room below */
private fun Modifier.pullUp(by: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val shift = by.roundToPx()
    layout(placeable.width, (placeable.height - shift).coerceAtLeast(0)) { placeable.place(0, -shift) }
}

/** The dashed tear line across the ticket, with a notch on each side in the page's colour */
@Composable
private fun Perforation(line: Color, notch: Color) {
    Canvas(Modifier.fillMaxWidth().height(22.dp)) {
        val y = size.height / 2f
        val margin = 18.dp.toPx()
        drawLine(line, Offset(margin, y), Offset(size.width - margin, y), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())))
        drawCircle(notch, radius = 11.dp.toPx(), center = Offset(0f, y))
        drawCircle(notch, radius = 11.dp.toPx(), center = Offset(size.width, y))
    }
}

@Composable
private fun FeastTicket(feast: FeastResponse, timing: FeastTiming, now: LocalDateTime, distanceMeters: Double?) {
    val colors = MaterialTheme.feastColors
    TicketCard {
        Row(Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DateStub(feast)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    feast.organizerName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.feast_unnamed),
                    color = colors.ink, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall,
                )
                FeastStatusChip(timing)
            }
        }
        Perforation(colors.saffronTrack, colors.saffronSoft)
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TicketLine(Icons.Rounded.Schedule, formatFeastHours(feast), countdownText(feast, now) ?: formatFeastDate(feast))
            feastProgress(feast, now)?.let { progress ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (progress > 0f) Box(Modifier.weight(progress).height(6.dp).background(colors.saffron, RoundedCornerShape(3.dp)))
                    if (progress < 1f) Box(Modifier.weight(1f - progress).height(6.dp).background(colors.saffronTrack, RoundedCornerShape(3.dp)))
                }
            }
            TicketLine(
                Icons.Rounded.LocationOn,
                feast.address?.trim()?.takeIf { it.isNotEmpty() } ?: stringResource(Res.string.shop_location_missing),
                listOfNotNull(
                    landmarkName(feast.landmark)?.let { stringResource(Res.string.shop_near_landmark, it) },
                    distanceMeters?.let { stringResource(Res.string.shop_distance_away, formatDistance(it)) },
                ).joinToString(" · ").takeIf { it.isNotEmpty() },
            )
        }
    }
}

@Composable
private fun TicketLine(icon: ImageVector, title: String, subtitle: String?) {
    val colors = MaterialTheme.feastColors
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.ink, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            subtitle?.let { Text(it, color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp)) }
        }
    }
}

/** "What's being served" as big plates, two to a row */
@Composable
private fun Served(items: List<String>) {
    val colors = MaterialTheme.feastColors
    val dishes = items.map { it.trim() }.filter { it.isNotEmpty() }
    if (dishes.isEmpty()) return
    Column {
        Text(
            stringResource(Res.string.feast_menu_label), color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 10.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            dishes.chunked(2).forEachIndexed { row, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEachIndexed { i, dish -> Plate(dish, PLATE_ICONS[(row * 2 + i) % PLATE_ICONS.size]) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RowScope.Plate(dish: String, icon: ImageVector) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.weight(1f).background(colors.surface, RoundedCornerShape(20.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(46.dp).background(colors.saffronSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(24.dp))
        }
        Text(dish, color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

/** Food type (with a veg / non-veg mark when it says) and how many people are expected */
@Composable
private fun FoodTypeAndPeople(feast: FeastResponse) {
    val colors = MaterialTheme.feastColors
    val foodType = feast.foodType?.trim()?.takeIf { it.isNotEmpty() }
    val people = feast.estimatedCapacity.takeIf { it > 0 }
    if (foodType == null && people == null) return
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        foodType?.let { type ->
            SmallFact(stringResource(Res.string.feast_food_type)) {
                val lower = type.lowercase()
                val mark = when {
                    "non" in lower -> colors.danger
                    "veg" in lower || "jain" in lower -> colors.success
                    else -> null
                }
                mark?.let {
                    Box(Modifier.size(12.dp).border(1.5.dp, it, RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(5.dp).background(it, CircleShape))
                    }
                }
                Text(type, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            }
        }
        people?.let {
            SmallFact(stringResource(Res.string.feast_capacity)) {
                Text(stringResource(Res.string.feast_capacity_value, it), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun RowScope.SmallFact(label: String, value: @Composable RowScope.() -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.weight(1f).background(colors.surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(label, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), content = value)
    }
}

@Composable
private fun Organizer(feast: FeastResponse, canCall: Boolean, onCall: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val name = feast.organizerName?.takeIf { it.isNotBlank() } ?: return
    Row(
        Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(40.dp).background(colors.lavender, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Groups, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.feast_organizer_label), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
            Text(name, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        }
        if (canCall) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.lavender).clickable(onClick = onCall), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Call, stringResource(Res.string.action_call), tint = colors.accent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** A taken-down feast (design 1d): the ticket greyed out and stamped, with when and where kept for reference */
@Composable
private fun TakenDownTicket(feast: FeastResponse) {
    val colors = MaterialTheme.feastColors
    TicketCard {
        Box {
            Row(Modifier.alpha(0.6f).padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DateStub(feast, faded = true)
                Text(
                    feast.organizerName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.feast_unnamed),
                    color = colors.ink, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall,
                )
            }
            // The stamp, slightly turned, over the name
            Row(
                Modifier
                    .padding(start = 96.dp, top = 74.dp)
                    .rotate(-4f)
                    .background(colors.surface, RoundedCornerShape(10.dp))
                    .border(2.5.dp, colors.danger, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Rounded.Cancel, contentDescription = null, tint = colors.danger, modifier = Modifier.size(16.dp))
                Text(stringResource(Res.string.feast_taken_down).uppercase(), color = colors.danger, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = MaterialTheme.typography.labelLarge)
            }
        }
        Canvas(Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(2.dp)) {
            drawLine(colors.border, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())))
        }
        Column(Modifier.alpha(0.6f).padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FadedLine(Icons.Rounded.CalendarToday, formatFeastDate(feast))
            FadedLine(Icons.Rounded.Schedule, formatFeastHours(feast))
            FadedLine(Icons.Rounded.LocationOn, feast.address?.trim()?.takeIf { it.isNotEmpty() } ?: stringResource(Res.string.shop_location_missing))
        }
    }
}

@Composable
private fun FadedLine(icon: ImageVector, text: String) {
    val colors = MaterialTheme.feastColors
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = colors.ink, modifier = Modifier.size(22.dp))
        Text(text, color = colors.ink, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

/** The indigo Directions button with a saffron arrow */
@Composable
private fun DirectionsBar(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(if (height < 52.dp) 16.dp else 18.dp)
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
        Icon(Icons.Rounded.Directions, contentDescription = null, tint = if (enabled) colors.saffron else colors.disabledText, modifier = Modifier.size(22.dp))
        Text(label, color = if (enabled) colors.onIndigo else colors.disabledText, fontSize = if (height < 52.dp) 15.sp else 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * A feast photo full screen (design 2b): dark, swipe between photos with a bar per photo, and at the bottom the
 * feast's ticket with its live status and Directions
 */
@Composable
private fun FeastPhotoViewer(
    feast: FeastResponse,
    initialIndex: Int,
    now: LocalDateTime,
    directionsLabel: String,
    canGo: Boolean,
    onDirections: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val photos = feast.imageUrls
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val pager = rememberPagerState(initialPage = initialIndex) { photos.size }
        Box(Modifier.fillMaxSize().background(Color(0xFF0E0D1A))) {
            HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = stringResource(Res.string.feast_photo, page + 1),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ViewerButton(Icons.Rounded.Close, stringResource(Res.string.close), onDismiss)
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
                        if (photos.size > 1) {
                            photos.indices.forEach { i ->
                                Box(Modifier.size(width = 22.dp, height = 5.dp).background(if (i == pager.currentPage) colors.saffron else Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp)))
                            }
                        }
                    }
                    ViewerButton(Icons.Rounded.Share, stringResource(Res.string.action_share), onShare)
                }
                Spacer(Modifier.weight(1f))
                ViewerTicket(feast, now, directionsLabel, canGo, onDirections)
            }
        }
    }
}

@Composable
private fun ViewerButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

/** The small ticket at the bottom of the viewer: date, name, live status with time left, and Directions */
@Composable
private fun ViewerTicket(feast: FeastResponse, now: LocalDateTime, directionsLabel: String, canGo: Boolean, onDirections: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val timing = feastTiming(feast, now)
    Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.saffronSoft)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateStub(feast, width = 52.dp, showWeekday = false)
            Column(Modifier.weight(1f)) {
                Text(
                    feast.organizerName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.feast_unnamed),
                    color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                val live = timing is FeastTiming.Now
                val status = listOfNotNull(
                    if (live) stringResource(Res.string.feast_happening_now) else timing.label().first,
                    countdownText(feast, now)?.replaceFirstChar { it.lowercase() }?.takeIf { live },
                ).joinToString(" · ")
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(7.dp).background(if (live) colors.success else colors.saffronText, CircleShape))
                    Text(status, color = if (live) colors.success else colors.saffronText, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Canvas(Modifier.padding(horizontal = 14.dp).fillMaxWidth().height(2.dp)) {
            drawLine(colors.saffronTrack, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())))
        }
        Box(Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 12.dp)) {
            DirectionsBar(directionsLabel, enabled = canGo, onClick = onDirections, modifier = Modifier.fillMaxWidth(), height = 48.dp)
        }
    }
}
