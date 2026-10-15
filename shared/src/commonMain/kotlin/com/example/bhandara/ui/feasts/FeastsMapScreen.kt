package com.example.bhandara.ui.feasts

import com.example.bhandara.platform.rememberPermissionStatuses
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.RiceBowl
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.button_report_feast
import com.example.bhandara.shared.resources.feast_pin_now
import com.example.bhandara.shared.resources.feast_unnamed
import com.example.bhandara.shared.resources.feasts_empty_text
import com.example.bhandara.shared.resources.feasts_none_nearby
import com.example.bhandara.shared.resources.feasts_section_coming_up
import com.example.bhandara.shared.resources.feasts_section_later_today
import com.example.bhandara.shared.resources.feasts_section_now
import com.example.bhandara.shared.resources.feasts_section_tomorrow
import com.example.bhandara.shared.resources.feasts_title
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.map_location_denied
import com.example.bhandara.shared.resources.map_location_needed
import com.example.bhandara.shared.resources.map_location_unavailable
import com.example.bhandara.shared.resources.map_open_settings
import com.example.bhandara.shared.resources.my_location
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.shopsmap.MapLocationState
import com.example.bhandara.ui.shopsmap.MapNotice
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.stringResource

/**
 * "Find community feasts" (the redesign's 1a / 1b), shared by Android and iOS: feasts around the user as pills
 * on the map saying when they start, a sheet with what's happening now and later, and "Post a Feast". With
 * nothing nearby the sheet becomes an invitation to post one. Tapping a pill or a row opens the feast.
 */
@Composable
fun FeastsMapScreen(onBack: () -> Unit, onFeastClick: (feastId: String) -> Unit, onPostFeast: () -> Unit) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        FeastsMapViewModel(
            LocationService(deviceLocation), FeastApi(dependencies.apiClient),
            radiusMeters = { dependencies.settings.preferences.searchRadiusMeters },
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val unnamed = stringResource(Res.string.feast_unnamed)
    val nowLabel = stringResource(Res.string.feast_pin_now)

    LaunchedEffect(Unit) { viewModel.start() }

    // Ask for location the first time it's needed; afterwards only when the user taps "Allow location"
    val requestLocation = rememberLocationPermissionRequest { granted -> viewModel.onLocationPermissionResult(granted) }
    // Allowed after all (the dialog closed, or the user came back from Settings): find the user
    val locationAllowed = rememberPermissionStatuses().location
    LaunchedEffect(locationAllowed) { viewModel.onLocationAccessChanged(locationAllowed) }
    var askedForLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.location) {
        if (state.location == MapLocationState.PermissionNeeded && !askedForLocation) {
            askedForLocation = true
            requestLocation()
        }
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        val density = LocalDensity.current
        var topBarHeight by remember { mutableStateOf(0.dp) }
        var bottomHeight by remember { mutableStateOf(0.dp) }
        val measureBottom = Modifier.onSizeChanged { bottomHeight = with(density) { it.height.toDp() } }

        Box(Modifier.fillMaxSize().background(colors.mapLand)) {
            PlatformMap(
                camera = state.camera,
                pins = state.pins(unnamed) { timing -> pillText(timing, nowLabel) },
                showMyLocation = state.location is MapLocationState.Found,
                darkMap = colors.isDark,
                onPinClick = { pin -> onFeastClick(pin.id) },
                look = MapLook.FEAST,
                contentPadding = PaddingValues(top = topBarHeight, bottom = bottomHeight),
                modifier = Modifier.fillMaxSize(),
            )

            // Location and loading notices, in the middle of the uncovered map
            Box(Modifier.fillMaxSize().padding(top = topBarHeight, bottom = bottomHeight)) {
                when {
                    state.location == MapLocationState.Locating || state.loading ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.indigo)
                    state.location == MapLocationState.PermissionNeeded -> MapNotice(
                        stringResource(Res.string.map_location_needed), stringResource(Res.string.map_allow_location),
                        onAction = requestLocation, modifier = Modifier.align(Alignment.Center),
                    )
                    state.location == MapLocationState.PermissionDenied -> MapNotice(
                        stringResource(Res.string.map_location_denied), stringResource(Res.string.map_open_settings),
                        onAction = actions::openAppSettings, modifier = Modifier.align(Alignment.Center),
                    )
                    state.location == MapLocationState.Unavailable -> MapNotice(
                        stringResource(Res.string.map_location_unavailable), stringResource(Res.string.try_again),
                        onAction = viewModel::retry, modifier = Modifier.align(Alignment.Center),
                    )
                    state.error != null -> MapNotice(
                        state.error!!, stringResource(Res.string.try_again),
                        onAction = viewModel::retry, modifier = Modifier.align(Alignment.Center),
                    )
                }
            }

            // Top: back and the title on one white card
            Row(
                Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } }
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.3f))
                    .background(colors.surface, RoundedCornerShape(18.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), tint = colors.ink, modifier = Modifier.size(24.dp))
                }
                Text(stringResource(Res.string.feasts_title), color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.titleMedium)
            }

            // Bottom: the list of feasts, or the invitation to post one; my-location sits above either
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                if (state.location is MapLocationState.Found) {
                    NearMeButton(viewModel::recenter, Modifier.align(Alignment.End).padding(end = 16.dp, bottom = 12.dp))
                }
                val sections = state.sections()
                when {
                    state.noFeastsNearby -> NoFeastsCard(
                        onPostFeast,
                        measureBottom
                            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
                    )
                    else -> FeastsSheet(
                        sections = sections,
                        onFeastClick = onFeastClick,
                        onDirections = { actions.openDirections(it.latitude, it.longitude) },
                        onPostFeast = onPostFeast,
                        modifier = measureBottom,
                    )
                }
            }
        }
    }
}

/** What a feast's pill on the map says: "Now", or when it starts */
private fun pillText(timing: FeastTiming, now: String): String = when (timing) {
    is FeastTiming.Now -> now
    is FeastTiming.Today -> formatFeastTime(timing.startsAt)
    is FeastTiming.Tomorrow -> formatFeastTime(timing.startsAt)
    is FeastTiming.Later -> formatFeastDay(timing.date).substringAfter(' ')
    else -> "•"
}

@Composable
private fun NearMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .size(52.dp)
            .shadow(8.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.3f))
            .background(colors.surface, shape)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.NearMe, stringResource(Res.string.my_location), tint = colors.accent, modifier = Modifier.size(24.dp))
    }
}

/** The sheet: "Happening now" as saffron cards with Directions, the rest as rows with when they start */
@Composable
private fun FeastsSheet(
    sections: List<FeastSection>,
    onFeastClick: (String) -> Unit,
    onDirections: (FeastResponse) -> Unit,
    onPostFeast: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    // The list scrolls inside the sheet so the map stays in view
    val maxList = 300.dp
    Column(
        modifier
            .fillMaxWidth()
            .shadow(24.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.3f))
            .background(colors.surface, shape)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(width = 38.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp)))
        if (sections.isNotEmpty()) {
            Column(Modifier.heightIn(max = maxList).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sections.forEach { section ->
                    SectionTitle(section.group)
                    section.feasts.forEach { feast ->
                        if (section.group == FeastGroup.NOW) {
                            LiveFeastCard(feast, onClick = { onFeastClick(feast.id) }, onDirections = { onDirections(feast) })
                        } else {
                            LaterFeastRow(feast, section.group, onClick = { onFeastClick(feast.id) })
                        }
                    }
                }
            }
        }
        FeastButton(
            stringResource(Res.string.button_report_feast), onClick = onPostFeast, style = FeastButtonStyle.SAFFRON,
            icon = Icons.Rounded.Add, height = 52.dp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

@Composable
private fun SectionTitle(group: FeastGroup) {
    val colors = MaterialTheme.feastColors
    val label = stringResource(
        when (group) {
            FeastGroup.NOW -> Res.string.feasts_section_now
            FeastGroup.LATER_TODAY -> Res.string.feasts_section_later_today
            FeastGroup.TOMORROW -> Res.string.feasts_section_tomorrow
            FeastGroup.COMING_UP -> Res.string.feasts_section_coming_up
        },
    ).uppercase()
    Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (group == FeastGroup.NOW) Box(Modifier.size(7.dp).background(colors.saffron, CircleShape))
        Text(
            label, color = if (group == FeastGroup.NOW) colors.saffronText else colors.muted, fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, style = MaterialTheme.typography.labelMedium,
        )
    }
}

/** "Khichdi, Sheera · 460 m" */
@Composable
private fun feastLine(feast: FeastResponse): String =
    listOfNotNull(feast.menuItems.joinToString(", ").takeIf { it.isNotBlank() }, feast.distance?.let(::formatDistance)).joinToString(" · ")

@Composable
private fun feastName(feast: FeastResponse): String = feast.organizerName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.feast_unnamed)

/** A feast on now: its photo (or a striped plate), name, what's served and how far, and a Directions button */
@Composable
private fun LiveFeastCard(feast: FeastResponse, onClick: () -> Unit, onDirections: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(colors.saffronSoft).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FeastThumb(feast)
        Column(Modifier.weight(1f)) {
            Text(feastName(feast), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Text(feastLine(feast), color = colors.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.indigo).clickable(onClick = onDirections),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Directions, stringResource(Res.string.action_directions), tint = colors.saffron, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun FeastThumb(feast: FeastResponse) {
    val colors = MaterialTheme.feastColors
    val photo = feast.imageUrls.firstOrNull()
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).stripes(colors.feastStripeA, colors.feastStripeB, 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.RiceBowl, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(24.dp))
        if (photo != null) AsyncImage(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

/** A feast later on: a tile with when it starts ("6 PM", or the day for further out), name and what's served */
@Composable
private fun LaterFeastRow(feast: FeastResponse, group: FeastGroup, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val timing = feastTiming(feast, localNow())
    val (big, small) = when (timing) {
        is FeastTiming.Today -> timeParts(formatFeastTime(timing.startsAt))
        is FeastTiming.Tomorrow -> timeParts(formatFeastTime(timing.startsAt))
        is FeastTiming.Later -> formatFeastDay(timing.date).split(' ').let { (it.getOrNull(1) ?: "") to (it.getOrNull(2) ?: "").uppercase() }
        else -> "•" to ""
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            Modifier.size(52.dp).background(colors.lavender, RoundedCornerShape(14.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(big, color = colors.ink, fontSize = 15.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            if (small.isNotEmpty()) Text(small, color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
        }
        Column(Modifier.weight(1f)) {
            Text(feastName(feast), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Text(feastLine(feast), color = colors.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** "6:30 PM" → "6:30" over "PM" */
private fun timeParts(time: String): Pair<String, String> = time.substringBeforeLast(' ') to time.substringAfterLast(' ', "")

/** Nothing nearby (design 1b): the message turns into an invitation to post a feast */
@Composable
private fun NoFeastsCard(onPostFeast: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(20.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.3f))
            .background(colors.surface, shape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(64.dp).background(colors.saffronSoft, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.VolunteerActivism, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(32.dp))
        }
        Text(stringResource(Res.string.feasts_none_nearby), color = colors.ink, fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        Text(stringResource(Res.string.feasts_empty_text), color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        FeastButton(
            stringResource(Res.string.button_report_feast), onClick = onPostFeast, style = FeastButtonStyle.SAFFRON,
            icon = Icons.Rounded.Add, height = 52.dp, modifier = Modifier.fillMaxWidth(),
        )
    }
}
