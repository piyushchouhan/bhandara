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
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.ui.components.MovingCartTracker
import com.example.bhandara.ui.components.MapPreviewCard
import com.example.bhandara.ui.components.crowdLabelRes
import com.example.bhandara.ui.components.formatMapDistance
import com.example.bhandara.ui.components.labeledPin
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
import com.google.maps.android.compose.rememberCameraPositionState
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

// ─────────────────────────────────────────────────────────────────────────────
// this screen is for local shops map to show shops and moving carts
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalShopsMapScreen(
    onBackClick: () -> Unit = {},
    onShopClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val locationHelper = remember { com.example.bhandara.utils.LocationHelper(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val isDarkTheme = isSystemInDarkTheme()
    val coroutineScope = rememberCoroutineScope()

    // ── Location & shop state ─────────────────────────────────────────────────
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var nearbyShops by remember { mutableStateOf<List<LocalShopResponse>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }


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
                // ── Shop markers (hidden when moving vendor mode is on) ───────
                if (!showMovingVendors) {
                    visibleShops.forEach { shop ->
                        val pin = remember(shop.id, shop.shopName, isDarkTheme) {
                            labeledPin(mapIconToEmoji(shop.isMovingCart), shop.shopName, isLiveCart = false, darkMap = isDarkTheme)
                        }
                        // Tap the pin or its name -> small card; tap the card -> shop details
                        MarkerInfoWindow(
                            state = MarkerState(position = LatLng(shop.latitude, shop.longitude)),
                            icon = pin.icon,
                            anchor = pin.anchor,
                            onInfoWindowClick = {
                                onShopClick(shop.id)
                            }
                        ) {
                            val isOpen = shop.isCurrentlyOpen ?: true
                            MapPreviewCard(
                                emoji = remember(shop.id) { getFoodEmojiForShop(shop.id) },
                                title = shop.shopName,
                                subtitle = shop.cuisineType ?: "Local Shop",
                                statusText = if (isOpen) "Open" else "Closed",
                                statusColor = if (isOpen) Color(0xFF4CAF50) else Color(0xFFE57373),
                                trailingText = listOfNotNull(
                                    crowdLabelRes(shop.crowdLevel)?.takeIf { isOpen }?.let { stringResource(it) },
                                    formatMapDistance(shop.distance)
                                ).joinToString(" • ")
                            )
                        }
                    }
                }

                // ── Moving cart markers ───────────────────────────────────────
                if (showMovingVendors) {
                    filteredMovingCarts.forEach { cart ->
                        val pin = remember(cart.id, cart.shopName, isDarkTheme) {
                            labeledPin(mapIconToEmoji(cart.isMovingCart), cart.shopName, isLiveCart = true, darkMap = isDarkTheme)
                        }
                        Marker(
                            state = MarkerState(position = LatLng(cart.latitude, cart.longitude)),
                            title = cart.shopName,
                            snippet = "Moving Vendor",
                            icon = pin.icon,
                            anchor = pin.anchor,
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
                            text = if (prompt.isMovingCart == true) {
                                "Is this cart here near you right now?"
                            } else {
                                "Have you seen this shop at the marked location?"
                            },
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
                                            // The backend only counts answers from people who are actually there
                                            val here = locationHelper.getCurrentLocation()
                                            val result = repository.verifyShop(prompt.id, vote, here?.latitude, here?.longitude)
                                            if (result.success || result.alreadyVoted) {
                                                verifyPrefs.edit().putBoolean("verified_${prompt.id}", true).apply()
                                            }
                                            // On other refusals (e.g. too far away) the prompt can come back when the user is closer
                                            verificationPrompt = null
                                            android.widget.Toast.makeText(
                                                context,
                                                if (result.success) "Thanks for verifying!" else result.message,
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
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

/** Gets a random, deterministic food emoji for a shop based on its ID. */
private fun getFoodEmojiForShop(shopId: String): String {
    val emojis = listOf("🍔", "🍕", "🌮", "🍜", "🍩", "🍣", "🍦", "🥗", "🥪", "🍰", "🍛", "🥞", "🌯", "🌭", "🍟")
    val index = Math.abs(shopId.hashCode()) % emojis.size
    return emojis[index]
}
