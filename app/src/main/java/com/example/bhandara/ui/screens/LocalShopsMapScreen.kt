package com.example.bhandara.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bhandara.R
import com.example.bhandara.data.api.NetworkModule
import com.example.bhandara.data.models.api.CrowdPingRequest
import com.example.bhandara.data.models.api.HeatmapPoint
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.ui.components.MovingCartTracker
import com.example.bhandara.ui.components.CrowdHeatmapToggle
import com.example.bhandara.ui.components.MapSearchBar
import com.example.bhandara.ui.components.MovingVendorsToggle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Crowd heatmap constants ──────────────────────────────────────────────────

/** How often we refresh the heatmap data from the server (ms) */
private const val HEATMAP_REFRESH_INTERVAL_MS = 60_000L

/** How often we send our own location ping to the server (ms) */
private const val CROWD_PING_INTERVAL_MS = 45_000L

/**
 * Snapchat-style heatmap gradient: green (sparse) → yellow → red (dense).
 * Colors and starting points mirror what Snapchat uses for their Snap Map heat layer.
 */
private val HEATMAP_GRADIENT = Gradient(
    intArrayOf(
        android.graphics.Color.argb(0, 0, 255, 0),   // transparent green  (0 %)
        android.graphics.Color.rgb(0, 255, 0),        // green              (10%)
        android.graphics.Color.rgb(255, 255, 0),      // yellow             (50%)
        android.graphics.Color.rgb(255, 128, 0),      // orange             (75%)
        android.graphics.Color.rgb(255, 0, 0),        // red                (100%)
    ),
    floatArrayOf(0f, 0.1f, 0.5f, 0.75f, 1f)
)

// ─────────────────────────────────────────────────────────────────────────────
// this screen is for local shops map to show shops and heatmap
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalShopsMapScreen(
    onBackClick: () -> Unit = {},
    onShopClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val isDarkTheme = isSystemInDarkTheme()
    val coroutineScope = rememberCoroutineScope()

    // ── Location & shop state ─────────────────────────────────────────────────
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var nearbyShops by remember { mutableStateOf<List<LocalShopResponse>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    // ── Crowd heatmap state ───────────────────────────────────────────────────
    var heatmapProvider by remember { mutableStateOf<HeatmapTileProvider?>(null) }
    var showCrowdHeatmap by remember { mutableStateOf(true) }

    // ── Moving vendor state ───────────────────────────────────────────────────
    var showMovingVendors by remember { mutableStateOf(false) }
    var movingCarts by remember { mutableStateOf<List<LocalShopResponse>>(emptyList()) }

    // ── Verification prompt state ────────────────────────────────────────────
    var verificationPrompt by remember { mutableStateOf<LocalShopResponse?>(null) }
    val verifyPrefs = remember { context.getSharedPreferences("VerificationPrefs", android.content.Context.MODE_PRIVATE) }

    // Cart tracking bottom sheet state
    var trackedCartShopId by remember { mutableLongStateOf(-1L) }
    var trackedCartName by remember { mutableStateOf("") }
    var trackedCartLat by remember { mutableStateOf(0.0) }
    var trackedCartLng by remember { mutableStateOf(0.0) }
    var showCartTracker by remember { mutableStateOf(false) }

    val apiService = NetworkModule.apiService
    val repository = remember { com.example.bhandara.data.repository.BackendRepository() }

    val defaultLocation = LatLng(28.6139, 77.2090)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation ?: defaultLocation, 14f)
    }

    // ── Permission launcher ───────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ── Location → fetch feed (shops + moving carts + verification) ─────────
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@LaunchedEffect

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener

            val latLng = LatLng(location.latitude, location.longitude)
            currentLocation = latLng
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)

            coroutineScope.launch {
                val feed = repository.getFeed(location.latitude, location.longitude)
                if (feed != null) {
                    nearbyShops = feed.activeShops.filter { it.isMovingCart != true }
                    movingCarts = feed.activeShops.filter { it.isMovingCart == true }
                    // Show verification prompt if not already voted
                    val prompt = feed.verificationPrompt
                    if (prompt != null && !verifyPrefs.getBoolean("verified_${prompt.id}", false)) {
                        verificationPrompt = prompt
                    }
                }
            }
        }
    }

    // ── Crowd heatmap: fetch around each shop, refresh every 60 s ─────────────
    // Keys on nearbyShops and showCrowdHeatmap so it (re)starts once shops are loaded,
    // and toggles off/on based on user preference.
    LaunchedEffect(nearbyShops, showCrowdHeatmap) {
        if (!showCrowdHeatmap || nearbyShops.isEmpty()) {
            heatmapProvider = null
            return@LaunchedEffect
        }

        while (true) {
            val allPoints = mutableListOf<HeatmapPoint>()

            nearbyShops.forEach { shop ->
                runCatching {
                    val response = apiService.getCrowdHeatmap(
                        lat = shop.latitude,
                        lng = shop.longitude,
                        radius = 20   // crowd within 20 m of this specific shop
                    )
                    if (response.isSuccessful) {
                        allPoints.addAll(response.body() ?: emptyList())
                    }
                }
            }

            // Setting a new provider causes Compose to re-render TileOverlay,
            // which resets the tile cache automatically. No manual call needed.
            heatmapProvider = buildHeatmapProvider(allPoints)

            delay(HEATMAP_REFRESH_INTERVAL_MS)
        }
    }

    // ── Crowd ping: send our own location every 45 s if near any shop ──────────
    LaunchedEffect(currentLocation) {
        val loc = currentLocation ?: return@LaunchedEffect

        while (true) {
            val isNearAnyShop = nearbyShops.any { shop ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    shop.latitude, shop.longitude,
                    results
                )
                results[0] <= 20f
            }

            if (isNearAnyShop) {
                runCatching {
                    apiService.crowdPing(
                        CrowdPingRequest(
                            latitude = loc.latitude,
                            longitude = loc.longitude
                        )
                    )
                }
            }
            delay(CROWD_PING_INTERVAL_MS)
        }
    }

    // ── Re-centre FAB ─────────────────────────────────────────────────────────
    val recenterToCurrentLocation: () -> Unit = {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLocation = latLng
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 14f),
                            durationMs = 1000
                        )
                    }
                }
            }
        }
    }

    // ── Map styles (unchanged) ────────────────────────────────────────────────
    val darkMapStyle = remember {
        MapStyleOptions("""
            [
                { "elementType": "geometry", "stylers": [{"color": "#212121"}] },
                { "elementType": "labels.icon", "stylers": [{"visibility": "off"}] },
                { "elementType": "labels.text.fill", "stylers": [{"color": "#757575"}] },
                { "elementType": "labels.text.stroke", "stylers": [{"color": "#212121"}] },
                { "featureType": "administrative", "elementType": "geometry", "stylers": [{"color": "#757575"}] },
                { "featureType": "poi", "stylers": [{"visibility": "off"}] },
                { "featureType": "road", "elementType": "geometry.fill", "stylers": [{"color": "#2c2c2c"}] },
                { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{"color": "#8a8a8a"}] },
                { "featureType": "road.arterial", "elementType": "geometry", "stylers": [{"color": "#373737"}] },
                { "featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#3c3c3c"}] },
                { "featureType": "road.highway.controlled_access", "elementType": "geometry", "stylers": [{"color": "#4e4e4e"}] },
                { "featureType": "road.local", "elementType": "labels.text.fill", "stylers": [{"color": "#616161"}] },
                { "featureType": "transit", "stylers": [{"visibility": "off"}] },
                { "featureType": "water", "elementType": "geometry", "stylers": [{"color": "#000000"}] },
                { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{"color": "#3d3d3d"}] }
            ]
        """.trimIndent())
    }

    val lightMapStyle = remember {
        MapStyleOptions("""
            [
                { "featureType": "poi", "elementType": "labels", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.business", "stylers": [{"visibility": "off"}] },
                { "featureType": "transit", "elementType": "labels.icon", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.attraction", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.government", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.medical", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.park", "elementType": "labels", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.place_of_worship", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.school", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.sports_complex", "stylers": [{"visibility": "off"}] }
            ]
        """.trimIndent())
    }

    val mapProperties = MapProperties(
        mapType = MapType.NORMAL,
        isMyLocationEnabled = hasLocationPermission,
        mapStyleOptions = if (isDarkTheme) darkMapStyle else lightMapStyle
    )

    val uiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        myLocationButtonEnabled = false,
        compassEnabled = true,
        mapToolbarEnabled = false
    )

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        floatingActionButton = {
            if (hasLocationPermission) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Re-center FAB
                    FloatingActionButton(
                        onClick = recenterToCurrentLocation,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.explore_24px),
                            contentDescription = "My Location"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // ── Filtered shops and moving carts ───────────────────────────────────
        val filteredShops = remember(nearbyShops, searchQuery) {
            if (searchQuery.isBlank()) {
                nearbyShops
            } else {
                nearbyShops.filter { shop ->
                    shop.shopName.contains(searchQuery, ignoreCase = true) ||
                            (shop.cuisineType?.contains(searchQuery, ignoreCase = true) == true) ||
                            (shop.shopType?.contains(searchQuery, ignoreCase = true) == true) ||
                            (shop.menuItems?.any { it.contains(searchQuery, ignoreCase = true) } == true)
                }
            }
        }

        val filteredMovingCarts = remember(movingCarts, searchQuery) {
            if (searchQuery.isBlank()) {
                movingCarts
            } else {
                movingCarts.filter { cart ->
                    cart.shopName.contains(searchQuery, ignoreCase = true) ||
                            (cart.cuisineType?.contains(searchQuery, ignoreCase = true) == true) ||
                            (cart.shopType?.contains(searchQuery, ignoreCase = true) == true) ||
                            (cart.menuItems?.any { it.contains(searchQuery, ignoreCase = true) } == true)
                }
            }
        }

        // Progressive reveal: show top-scored shops, more on zoom
        val zoom = cameraPositionState.position.zoom
        val maxShopsForZoom = when {
            zoom >= 16f -> Int.MAX_VALUE
            zoom >= 15f -> 10
            else -> 5
        }
        val visibleShops = filteredShops.take(maxShopsForZoom)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                // ── Crowd heatmap layer ───────────────────────────────────────
                // Only rendered when we have actual data from the server and enabled.
                if (showCrowdHeatmap) {
                    heatmapProvider?.let { provider ->
                        TileOverlay(
                            tileProvider = provider,
                            transparency = 0.2f  // 80% opaque — visible but not blocking the map
                        )
                    }
                }

                // ── Shop markers (hidden when moving vendor mode is on) ───────
                if (!showMovingVendors) {
                    visibleShops.forEach { shop ->
                        val icon = remember(shop.id) {
                            emojiToBitmapDescriptor(mapIconToEmoji(shop.isMovingCart), sizeDp = 72)
                        }
                        MarkerInfoWindow(
                            state = MarkerState(position = LatLng(shop.latitude, shop.longitude)),
                            icon = icon,
                            onInfoWindowClick = {
                                onShopClick(shop.id)
                            }
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular light-pink background with food emoji
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = Color(0xFFFFE3E8),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val foodEmoji = remember(shop.id) { getFoodEmojiForShop(shop.id) }
                                        Text(
                                            text = foodEmoji,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = shop.shopName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF212121)
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = shop.cuisineType ?: "Local Shop",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF757575)
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isOpen = shop.isCurrentlyOpen ?: true
                                            val statusColor = if (isOpen) Color(0xFF4CAF50) else Color(0xFFE57373)
                                            val statusText = if (isOpen) "Open" else "Closed"

                                            // Tiny colored circle dot
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        color = statusColor,
                                                        shape = CircleShape
                                                    )
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = statusText,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = statusColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )

                                            Text(
                                                text = " • ",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF757575)
                                                )
                                            )

                                            val distanceKm = (shop.distance ?: 0.0) / 1000.0
                                            val distanceStr = if (distanceKm < 0.1) "nearby" else String.format("%.1f km", distanceKm)

                                            Text(
                                                text = distanceStr,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF757575)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Moving cart markers ───────────────────────────────────────
                if (showMovingVendors) {
                    filteredMovingCarts.forEach { cart ->
                        val icon = remember(cart.id) {
                            emojiToBitmapDescriptor(mapIconToEmoji(cart.isMovingCart), sizeDp = 72)
                        }
                        Marker(
                            state = MarkerState(position = LatLng(cart.latitude, cart.longitude)),
                            title = cart.shopName,
                            snippet = "Moving Vendor",
                            icon = icon,
                            onClick = {
                                trackedCartShopId = cart.id.toLongOrNull() ?: -1L
                                trackedCartName = cart.shopName
                                trackedCartLat = cart.latitude
                                trackedCartLng = cart.longitude
                                showCartTracker = true
                                true
                            }
                        )
                    }
                }
            }

            // ── No moving carts message ───────────────────────────────────────
            if (showMovingVendors && filteredMovingCarts.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No moving vendors nearby",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Zoom hint message ─────────────────────────────────────────────
            if (!showMovingVendors && filteredShops.size > visibleShops.size) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    )
                ) {
                    Text(
                        text = "Showing top-rated shops. Zoom in for more",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }

            // ── Floating Search Bar ──────────────────────────────────────────
            MapSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onBackClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // ── Verification prompt card ─────────────────────────────────────
            if (verificationPrompt != null) {
                val prompt = verificationPrompt!!
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Help verify this shop",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            IconButton(
                                onClick = { verificationPrompt = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = mapIconToEmoji(prompt.isMovingCart),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Column {
                                Text(
                                    text = prompt.shopName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = buildString {
                                        append(prompt.shopType ?: "Shop")
                                        prompt.distance?.let {
                                            append(" • ${String.format("%.0f", it)} m away")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Text(
                            text = "Have you seen this shop at the marked location?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("YES" to "Yes", "NO" to "No", "NOT_SURE" to "Not Sure").forEach { (vote, label) ->
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.verifyShop(prompt.id, vote)
                                            verifyPrefs.edit().putBoolean("verified_${prompt.id}", true).apply()
                                            verificationPrompt = null
                                            android.widget.Toast.makeText(context, "Thanks for verifying!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // ── Side Floating Layer Toggles (Bottom Left Corner) ─────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = if (verificationPrompt != null) 180.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MovingVendorsToggle(
                    isSelected = showMovingVendors,
                    onToggle = { showMovingVendors = !showMovingVendors }
                )

                CrowdHeatmapToggle(
                    isSelected = showCrowdHeatmap,
                    onToggle = { showCrowdHeatmap = !showCrowdHeatmap }
                )
            }
        }
    }

    // ── Cart tracking bottom sheet ────────────────────────────────────────────
    if (showCartTracker && trackedCartShopId > 0) {
        MovingCartTracker(
            shopId = trackedCartShopId,
            shopName = trackedCartName,
            initialLat = trackedCartLat,
            initialLng = trackedCartLng,
            onDismiss = { showCartTracker = false },
            onViewDetails = {
                showCartTracker = false
                onShopClick(trackedCartShopId.toString())
            }
        )
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

/** Maps a shop to its display emoji based on whether it is moving or static. */
private fun mapIconToEmoji(isMovingCart: Boolean?): String = 
    if (isMovingCart == true) "🚚" else "📍"

/** Renders an emoji string into a [BitmapDescriptor] for use as a map marker icon. */
private fun emojiToBitmapDescriptor(emoji: String, sizeDp: Int): com.google.android.gms.maps.model.BitmapDescriptor {
    val size = sizeDp.coerceAtLeast(24)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 0.75f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    val x = size / 2f
    val y = size / 2f - (paint.ascent() + paint.descent()) / 2f
    canvas.drawText(emoji, x, y, paint)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Builds a [HeatmapTileProvider] from the list of points returned by the API.
 * Returns null when the list is empty so the TileOverlay is simply not rendered.
 *
 * We need at least 1 point for the provider not to crash. The server already
 * handles the "owner continuously present" exclusion, so we trust the weights.
 */
private fun buildHeatmapProvider(points: List<HeatmapPoint>): HeatmapTileProvider? {
    if (points.isEmpty()) return null

    val weightedPoints = points.map { point ->
        WeightedLatLng(
            com.google.android.gms.maps.model.LatLng(point.lat, point.lng),
            point.weight
        )
    }

    return HeatmapTileProvider.Builder()
        .weightedData(weightedPoints)
        .gradient(HEATMAP_GRADIENT)
        .radius(50)       // pixel radius per point — matches Snapchat's blob size
        .opacity(0.8)
        .build()
}

/** Gets a random, deterministic food emoji for a shop based on its ID. */
private fun getFoodEmojiForShop(shopId: String): String {
    val emojis = listOf("🍔", "🍕", "🌮", "🍜", "🍩", "🍣", "🍦", "🥗", "🥪", "🍰", "🍛", "🥞", "🌯", "🌭", "🍟")
    val index = Math.abs(shopId.hashCode()) % emojis.size
    return emojis[index]
}
