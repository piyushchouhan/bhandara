package com.example.bhandara.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bhandara.R
import com.example.bhandara.data.api.NetworkModule
import com.example.bhandara.data.models.api.CrowdPingRequest
import com.example.bhandara.data.models.api.HeatmapPoint
import com.example.bhandara.data.models.api.LocalShopResponse
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
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
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

    // ── Crowd heatmap state ───────────────────────────────────────────────────
    // Null means "no data yet" — the TileOverlay is not added until we have points.
    var heatmapProvider by remember { mutableStateOf<HeatmapTileProvider?>(null) }


    val apiService = NetworkModule.apiService

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

    // ── Location → fetch shops & kick off crowd loops ─────────────────────────
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@LaunchedEffect

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener

            val latLng = LatLng(location.latitude, location.longitude)
            currentLocation = latLng
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)

            coroutineScope.launch {
                // Fetch nearby shops
                runCatching {
                    val response = apiService.getLocalShopsNearby(
                        lat = location.latitude,
                        lon = location.longitude,
                        radius = 5000.0
                    )
                    if (response.isSuccessful) {
                        nearbyShops = response.body() ?: emptyList()
                    }
                }
            }
        }
    }

    // ── Crowd heatmap: fetch around each shop, refresh every 60 s ─────────────
    // Keys on nearbyShops so it (re)starts once shops are loaded, and again if
    // the shop list ever changes. Each shop gets its own 300 m radius query;
    // all points are merged into a single heatmap layer.
    LaunchedEffect(nearbyShops) {
        if (nearbyShops.isEmpty()) return@LaunchedEffect

        while (true) {
            val allPoints = mutableListOf<HeatmapPoint>()

            nearbyShops.forEach { shop ->
                runCatching {
                    val response = apiService.getCrowdHeatmap(
                        lat = shop.latitude,
                        lng = shop.longitude,
                        radius = 300   // crowd within 300 m of this specific shop
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

    // ── Crowd ping: send our own location every 45 s ──────────────────────────
    LaunchedEffect(currentLocation) {
        val loc = currentLocation ?: return@LaunchedEffect

        while (true) {
            runCatching {
                apiService.crowdPing(
                    CrowdPingRequest(
                        latitude = loc.latitude,
                        longitude = loc.longitude
                    )
                )
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
        topBar = {
            TopAppBar(
                title = { Text("Find Local Shops") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasLocationPermission) {
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
    ) { paddingValues ->
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
                // Only rendered when we have actual data from the server.
                heatmapProvider?.let { provider ->
                    TileOverlay(
                        tileProvider = provider,
                        transparency = 0.2f  // 80% opaque — visible but not blocking the map
                    )
                }

                // ── Shop markers ──────────────────────────────────────────────
                nearbyShops.forEach { shop ->
                    Marker(
                        state = MarkerState(position = LatLng(shop.latitude, shop.longitude)),
                        title = shop.shopName,
                        snippet = buildString {
                            append(shop.fullAddress ?: shop.area ?: "Local Shop")
                            shop.distance?.let {
                                val distanceKm = it / 1000.0
                                append(" • ${String.format("%.1f", distanceKm)} km away")
                            }
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        onClick = {
                            onShopClick(shop.id)
                            true
                        }
                    )
                }
            }
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

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
