package com.example.bhandara.ui.feasts

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.explore_24px
import com.example.bhandara.shared.resources.feast_unnamed
import com.example.bhandara.shared.resources.feasts_none_nearby
import com.example.bhandara.shared.resources.feasts_title
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.map_location_denied
import com.example.bhandara.shared.resources.map_location_needed
import com.example.bhandara.shared.resources.map_location_unavailable
import com.example.bhandara.shared.resources.map_open_settings
import com.example.bhandara.shared.resources.my_location
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.shopsmap.MapHint
import com.example.bhandara.ui.shopsmap.MapLocationState
import com.example.bhandara.ui.shopsmap.MapNotice
import com.example.bhandara.ui.shopsmap.MapPreviewCard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * "I'm hungry": community feasts on now or coming up around the user, shared by Android and iOS. Tapping a pin
 * shows when the feast is; tapping that card opens it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeastsMapScreen(onBack: () -> Unit, onFeastClick: (feastId: String) -> Unit) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel { FeastsMapViewModel(LocationService(deviceLocation), FeastApi(dependencies.apiClient)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val unnamed = stringResource(Res.string.feast_unnamed)

    LaunchedEffect(Unit) { viewModel.start() }

    // Ask for location the first time it's needed; afterwards only when the user taps "Allow location"
    val requestLocation = rememberLocationPermissionRequest { granted -> viewModel.onLocationPermissionResult(granted) }
    var askedForLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.location) {
        if (state.location == MapLocationState.PermissionNeeded && !askedForLocation) {
            askedForLocation = true
            requestLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feasts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            PlatformMap(
                camera = state.camera,
                pins = state.pins(unnamed),
                showMyLocation = state.location is MapLocationState.Found,
                darkMap = isSystemInDarkTheme(),
                onPinClick = viewModel::onPinClick,
                modifier = Modifier.fillMaxSize(),
            )

            when {
                state.location == MapLocationState.Locating || state.loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                state.noFeastsNearby ->
                    MapHint(stringResource(Res.string.feasts_none_nearby), Modifier.align(Alignment.TopCenter).padding(top = 16.dp))
            }

            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
                if (state.location is MapLocationState.Found) {
                    FloatingActionButton(onClick = viewModel::recenter, modifier = Modifier.padding(bottom = 12.dp)) {
                        Icon(painterResource(Res.drawable.explore_24px), stringResource(Res.string.my_location))
                    }
                }
                state.selectedFeast()?.let { feast ->
                    val (status, color) = state.timing(feast).label()
                    MapPreviewCard(
                        emoji = FeastsMapState.FEAST_EMOJI,
                        title = feast.organizerName?.takeIf { it.isNotBlank() } ?: unnamed,
                        subtitle = feast.menuItems.joinToString(", ").ifBlank { feast.address ?: unnamed },
                        status = status,
                        statusColor = color,
                        details = feast.distance?.let(::formatDistance).orEmpty(),
                        onOpen = { onFeastClick(feast.id) },
                        onClose = viewModel::clearSelection,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
