package com.example.bhandara.ui.shopsmap

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.network.FeedApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.*
import com.example.bhandara.ui.map.PlatformMap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The shops map, shared by Android and iOS: shops (or live carts) around the user as pins, a search, and the
 * "Is this place real?" question for nearby users.
 *
 * @param onShopClick opens a shop or cart's page
 * @param onCartClick when given, a tapped live cart opens this instead of the preview card (Android's live
 *                    tracking); otherwise carts behave like shops
 */
@Composable
fun ShopsMapScreen(
    onBack: () -> Unit,
    onShopClick: (shopId: String) -> Unit,
    onCartClick: ((LocalShopResponse) -> Unit)? = null,
) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        ShopsMapViewModel(
            LocationService(deviceLocation),
            FeedApi(dependencies.apiClient),
            ShopApi(dependencies.apiClient),
            dependencies.settings,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current

    LaunchedEffect(Unit) { viewModel.start() }

    // Ask for location the first time the map needs it; afterwards only when the user taps "Allow location"
    val requestLocation = rememberLocationPermissionRequest { granted -> viewModel.onLocationPermissionResult(granted) }
    var askedForLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.location) {
        if (state.location == MapLocationState.PermissionNeeded && !askedForLocation) {
            askedForLocation = true
            requestLocation()
        }
    }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        PlatformMap(
            camera = state.camera,
            pins = state.pins(),
            showMyLocation = state.location is MapLocationState.Found,
            darkMap = isSystemInDarkTheme(),
            onPinClick = { pin ->
                val cart = state.carts.firstOrNull { it.id == pin.id }
                if (cart != null && onCartClick != null) onCartClick(cart) else viewModel.onPinClick(pin)
            },
            onZoomChanged = viewModel::onZoomChanged,
            modifier = Modifier.fillMaxSize(),
        )

        // Everything over the map stays clear of the status bar, notch and home indicator
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Column(Modifier.align(Alignment.TopCenter).padding(horizontal = 16.dp, vertical = 12.dp)) {
                MapSearchBar(query = state.searchQuery, onQueryChange = viewModel::onSearchQueryChange, onBack = onBack)
                if (state.hasMoreShopsWhenZoomedIn()) {
                    MapHint(stringResource(Res.string.map_zoom_hint), Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp))
                }
            }

            when {
                state.location == MapLocationState.Locating || state.loadingShops ->
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
                state.showMovingVendors && state.pins().isEmpty() ->
                    MapHint(stringResource(Res.string.map_no_moving_vendors), Modifier.align(Alignment.Center))
            }

            // Bottom: the tapped place, or the "Is this place real?" question; the buttons sit above it
            Column(Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                Box(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    MovingVendorsToggle(
                        isSelected = state.showMovingVendors,
                        onToggle = { viewModel.onShowMovingVendorsChange(!state.showMovingVendors) },
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                    if (state.location is MapLocationState.Found) {
                        FloatingActionButton(onClick = viewModel::recenter, modifier = Modifier.align(Alignment.BottomEnd)) {
                            Icon(painterResource(Res.drawable.explore_24px), stringResource(Res.string.my_location))
                        }
                    }
                }
                val selected = state.selectedPlace()
                val prompt = state.verificationPrompt
                when {
                    selected != null -> PlacePreviewCard(
                        selected, onOpen = { onShopClick(selected.id) }, onClose = viewModel::clearSelection,
                    )
                    prompt != null -> VerificationCard(prompt, onAnswer = viewModel::answerVerification, onDismiss = viewModel::dismissVerification)
                }
            }

            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }
}
