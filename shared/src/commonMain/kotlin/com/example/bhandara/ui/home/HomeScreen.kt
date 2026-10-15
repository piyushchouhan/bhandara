package com.example.bhandara.ui.home

import com.example.bhandara.platform.rememberPermissionStatuses
import com.example.bhandara.vendor.VendorModeBanner
import com.example.bhandara.vendor.VendorModeDrawerRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.bhandara.platform.rememberAppLanguage
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_shop
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.button_report_feast
import com.example.bhandara.shared.resources.clear_search
import com.example.bhandara.shared.resources.community_feast
import com.example.bhandara.shared.resources.home_carts_nearby
import com.example.bhandara.shared.resources.home_no_carts
import com.example.bhandara.shared.resources.home_showing_carts
import com.example.bhandara.shared.resources.live
import com.example.bhandara.shared.resources.local_shops
import com.example.bhandara.shared.resources.logo_mask
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.map_location_denied
import com.example.bhandara.shared.resources.map_location_needed
import com.example.bhandara.shared.resources.map_location_unavailable
import com.example.bhandara.shared.resources.map_no_moving_vendors
import com.example.bhandara.shared.resources.map_open_settings
import com.example.bhandara.shared.resources.map_zoom_hint
import com.example.bhandara.shared.resources.menu
import com.example.bhandara.shared.resources.moving_vendors
import com.example.bhandara.shared.resources.my_location
import com.example.bhandara.shared.resources.search_local_feast
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.drawer.AppDrawer
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.shopsmap.MapHint
import com.example.bhandara.ui.shopsmap.MapLocationState
import com.example.bhandara.ui.shopsmap.MapNotice
import com.example.bhandara.ui.shopsmap.ShopsMapViewModel
import com.example.bhandara.ui.shopsmap.VerificationCard
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The first screen, shared by Android and iOS: the map of shops and live carts around the user, with the side
 * menu, a search, and a sheet at the bottom to add a shop or switch to community feasts.
 *
 * @param onShopClick opens a shop or cart's page
 * @param drawerExtraItems extra rows at the top of the side menu (by default Vendor Mode, where the platform has it)
 * @param overlay drawn over the screen (by default the "Vendor Mode is on" banner)
 */
@Composable
fun HomeScreen(
    onShopClick: (shopId: String) -> Unit,
    onAddShopClick: () -> Unit,
    onFeastsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onTermsClick: () -> Unit,
    onReportIssueClick: () -> Unit,
    onSuggestFeatureClick: () -> Unit,
    onHowToUseClick: () -> Unit,
    drawerExtraItems: @Composable ColumnScope.(closeDrawer: () -> Unit) -> Unit = { VendorModeDrawerRow() },
    overlay: @Composable BoxScope.() -> Unit = { VendorModeBanner(Modifier.align(Alignment.TopCenter)) },
) {
    FeastTheme {
        AppDrawer(
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
            onAboutClick = onAboutClick,
            onSupportClick = onSupportClick,
            onTermsClick = onTermsClick,
            onReportIssueClick = onReportIssueClick,
            onSuggestFeatureClick = onSuggestFeatureClick,
            onHowToUseClick = onHowToUseClick,
            extraItems = drawerExtraItems,
            overlay = overlay,
        ) { openDrawer ->
            HomeMap(
                onMenuClick = openDrawer,
                onShopClick = onShopClick,
                onAddShopClick = onAddShopClick,
                onFeastsClick = onFeastsClick,
            )
        }
    }
}

@Composable
private fun HomeMap(
    onMenuClick: () -> Unit,
    onShopClick: (shopId: String) -> Unit,
    onAddShopClick: () -> Unit,
    onFeastsClick: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
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
    val selected = state.selectedPlace()
    val language = rememberAppLanguage().current

    LaunchedEffect(Unit) { viewModel.start() }

    // Ask for location the first time the map needs it; afterwards only when the user taps "Allow location"
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

    var trackedCart by remember { mutableStateOf<LocalShopResponse?>(null) }
    trackedCart?.let { cart ->
        LiveCartSheet(cart, onDismiss = { trackedCart = null }, onViewDetails = { onShopClick(cart.id) })
    }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    // What covers the map at the top and bottom, so it centres in the rest and keeps the Google logo in view
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var sheetHeight by remember { mutableStateOf(0.dp) }

    Box(Modifier.fillMaxSize().background(colors.mapLand)) {
        PlatformMap(
            camera = state.camera,
            pins = state.pins(language),
            showMyLocation = state.location is MapLocationState.Found,
            darkMap = colors.isDark,
            onPinClick = { pin ->
                // A live cart opens its live tracking; anything else its preview card
                val cart = state.carts.firstOrNull { it.id == pin.id }
                if (cart != null) trackedCart = cart else viewModel.onPinClick(pin)
            },
            onZoomChanged = viewModel::onZoomChanged,
            look = MapLook.FEAST,
            contentPadding = PaddingValues(top = topBarHeight, bottom = sheetHeight),
            zones = state.mapZones(),
            onZoneClick = viewModel::onZoneClick,
            modifier = Modifier.fillMaxSize(),
        )

        // Notices sit in the middle of the uncovered map
        Box(Modifier.fillMaxSize().padding(top = topBarHeight, bottom = sheetHeight)) {
            when {
                state.location == MapLocationState.Locating || state.loadingShops ->
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
                state.showMovingVendors && state.pins().isEmpty() ->
                    MapHint(stringResource(Res.string.map_no_moving_vendors), Modifier.align(Alignment.Center))
            }
        }

        // Top: the menu button and the search, clear of the status bar and notch
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } }
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // With a place open, the button closes it (design 1a); otherwise it opens the side menu
            if (selected != null) {
                MapTopButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), filled = false, onClick = viewModel::clearSelection)
            } else {
                MapTopButton(Icons.Rounded.Menu, stringResource(Res.string.menu), filled = true, onClick = onMenuClick)
            }
            SearchField(query = state.searchQuery, onQueryChange = viewModel::onSearchQueryChange, modifier = Modifier.weight(1f))
        }
        if (state.hasMoreShopsWhenZoomedIn()) {
            MapHint(stringResource(Res.string.map_zoom_hint), Modifier.align(Alignment.TopCenter).padding(top = topBarHeight + 4.dp))
        }

        // Bottom: the tapped place's card in place of the sheet (design 1a); otherwise "Is this place real?" over
        // the sheet. On the right, stacked above either: my location, then (with no place open) moving vendors
        // and "Add shop".
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.align(Alignment.End), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.location is MapLocationState.Found) {
                        MyLocationButton(viewModel::recenter)
                    }
                    if (selected == null) {
                        MovingVendorsButton(
                            cartCount = state.carts.size,
                            showingCarts = state.showMovingVendors,
                            onClick = { viewModel.onShowMovingVendorsChange(!state.showMovingVendors) },
                        )
                        AddShopButton(onAddShopClick)
                    }
                }
                val prompt = state.verificationPrompt
                if (selected == null && prompt != null) {
                    VerificationCard(prompt, onAnswer = viewModel::answerVerification, onDismiss = viewModel::dismissVerification)
                }
            }
            val measureBottom = Modifier.onSizeChanged { sheetHeight = with(density) { it.height.toDp() } }
            if (selected != null) {
                ShopPreviewCard(
                    place = selected,
                    onOpen = { onShopClick(selected.id) },
                    onDirections = { actions.openDirections(selected.latitude, selected.longitude) },
                    onCall = selected.ownerPhone?.takeIf { it.isNotBlank() && selected.isActive != false }?.let { phone -> { actions.dial(phone) } },
                    modifier = measureBottom
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                        .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
                )
            } else {
                HomeSheet(onFeastsClick = onFeastsClick, modifier = measureBottom)
            }
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = sheetHeight))
    }
}

/** The square button left of the search: indigo for the menu, white for back */
@Composable
private fun MapTopButton(icon: ImageVector, description: String, filled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .size(50.dp)
            .then(if (filled) Modifier else Modifier.shadow(8.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.35f)))
            .background(if (filled) colors.indigo else colors.surface, shape)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = if (filled) colors.onIndigo else colors.ink, modifier = Modifier.size(24.dp))
    }
}

/** "Search Local Feast": filters the pins by name, cuisine or menu */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp)
    Row(
        modifier
            .height(50.dp)
            .shadow(8.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.35f))
            .background(colors.surface, shape)
            .padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painterResource(Res.drawable.logo_mask),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.saffron),
            modifier = Modifier.width(22.dp).height(20.dp),
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = textStyle.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.ink),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(stringResource(Res.string.search_local_feast), style = textStyle, color = colors.muted, maxLines = 1)
                    }
                    field()
                }
            },
        )
        if (query.isNotEmpty()) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).clickable { onQueryChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Clear, stringResource(Res.string.clear_search), tint = colors.muted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** A round-cornered map button on the right, above the sheet */
@Composable
private fun MapSideButton(
    icon: ImageVector,
    description: String,
    background: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outline: Color? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .size(52.dp)
            .shadow(8.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.3f))
            .background(background, shape)
            .then(if (outline != null) Modifier.border(2.dp, outline, shape) else Modifier)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun MyLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    // The same arrow as on the feasts map
    MapSideButton(Icons.Rounded.NearMe, stringResource(Res.string.my_location), colors.surface, colors.accent, onClick, modifier)
}

/**
 * Switches the map between shops and live carts: a truck on soft saffron, solid saffron while carts are shown.
 * A small count says how many carts are live nearby.
 */
@Composable
private fun MovingVendorsButton(cartCount: Int, showingCarts: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val description = when {
        showingCarts -> stringResource(Res.string.home_showing_carts)
        cartCount == 0 -> stringResource(Res.string.moving_vendors) + ". " + stringResource(Res.string.home_no_carts)
        else -> stringResource(Res.string.moving_vendors) + ". " + pluralStringResource(Res.plurals.home_carts_nearby, cartCount, cartCount)
    }
    Box {
        MapSideButton(
            Icons.Rounded.LocalShipping,
            description,
            background = if (showingCarts) colors.saffron else colors.saffronSoft,
            tint = if (showingCarts) colors.onSaffron else colors.saffronText,
            onClick = onClick,
            outline = if (showingCarts) colors.surface else null,
        )
        if (cartCount > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .background(colors.indigo, CircleShape)
                    .border(1.5.dp, colors.surface, CircleShape)
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (cartCount > 99) "99+" else cartCount.toString(),
                    color = colors.onIndigo, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/** "Add shop": a plus on indigo */
@Composable
private fun AddShopButton(onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    MapSideButton(Icons.Rounded.Add, stringResource(Res.string.add_shop), colors.indigo, colors.onIndigo, onClick)
}

/** The sheet at the bottom: the switch to community feasts, within thumb reach (back returns here) */
@Composable
private fun HomeSheet(onFeastsClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(24.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.3f))
            .background(colors.surface, shape)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(width = 38.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp)))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)) {
            TabLabel(stringResource(Res.string.local_shops), selected = true) {}
            TabLabel(stringResource(Res.string.community_feast), selected = false, onClick = onFeastsClick)
        }
    }
}

/** A tab name; the selected one is underlined in saffron */
@Composable
private fun RowScope.TabLabel(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val underline = 2.5.dp
    Box(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                if (selected) {
                    val y = size.height - underline.toPx() / 2f
                    drawLine(colors.saffron, Offset(0f, y), Offset(size.width, y), strokeWidth = underline.toPx())
                }
            }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) colors.ink else colors.muted,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
