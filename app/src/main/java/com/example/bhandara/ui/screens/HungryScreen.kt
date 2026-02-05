package com.example.bhandara.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bhandara.R
import com.example.bhandara.data.api.NetworkModule
import com.example.bhandara.data.models.api.FeastResponse
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
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HungryScreen(
    onBackClick: () -> Unit = {},
    onFeastClick: (String) -> Unit = {} // Add callback for feast click
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val isDarkTheme = isSystemInDarkTheme()
    val coroutineScope = rememberCoroutineScope()
    
    // State for location permission
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    
    // State for nearby feasts
    var nearbyFeasts by remember { mutableStateOf<List<FeastResponse>>(emptyList()) }
    var isLoadingFeasts by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // API service
    val apiService = NetworkModule.apiService
    
    // Default location - Delhi, India (fallback)
    val defaultLocation = LatLng(28.6139, 77.2090)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation ?: defaultLocation, 14f)
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    
    // Request location permission on launch
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    // Get current location when permission is granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLocation = latLng
                    // Animate camera to current location
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)
                    
                    // Fetch nearby feasts
                    coroutineScope.launch {
                        isLoadingFeasts = true
                        errorMessage = null
                        try {
                            val response = apiService.getFeastsNearby(
                                lat = location.latitude,
                                lon = location.longitude,
                                radius = 5000.0 // 5km radius
                            )
                            
                            if (response.isSuccessful) {
                                val feasts = response.body() ?: emptyList()
                                nearbyFeasts = feasts
                            } else {
                                val errorBody = response.errorBody()?.string()
                                errorMessage = "Failed to load nearby feasts"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isLoadingFeasts = false
                        }
                    }
                }
            }
        }
    }
    
    // Function to re-center map to current location
    val recenterToCurrentLocation: () -> Unit = {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLocation = latLng
                    // Animate to current location
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
    
    // Dark mode map style
    val darkMapStyle = remember {
        MapStyleOptions("""
            [
                {
                    "elementType": "geometry",
                    "stylers": [{"color": "#212121"}]
                },
                {
                    "elementType": "labels.icon",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#757575"}]
                },
                {
                    "elementType": "labels.text.stroke",
                    "stylers": [{"color": "#212121"}]
                },
                {
                    "featureType": "administrative",
                    "elementType": "geometry",
                    "stylers": [{"color": "#757575"}]
                },
                {
                    "featureType": "poi",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "road",
                    "elementType": "geometry.fill",
                    "stylers": [{"color": "#2c2c2c"}]
                },
                {
                    "featureType": "road",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#8a8a8a"}]
                },
                {
                    "featureType": "road.arterial",
                    "elementType": "geometry",
                    "stylers": [{"color": "#373737"}]
                },
                {
                    "featureType": "road.highway",
                    "elementType": "geometry",
                    "stylers": [{"color": "#3c3c3c"}]
                },
                {
                    "featureType": "road.highway.controlled_access",
                    "elementType": "geometry",
                    "stylers": [{"color": "#4e4e4e"}]
                },
                {
                    "featureType": "road.local",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#616161"}]
                },
                {
                    "featureType": "transit",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "water",
                    "elementType": "geometry",
                    "stylers": [{"color": "#000000"}]
                },
                {
                    "featureType": "water",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#3d3d3d"}]
                }
            ]
        """.trimIndent())
    }
    
    // Light mode clean map style
    val lightMapStyle = remember {
        MapStyleOptions("""
            [
                {
                    "featureType": "poi",
                    "elementType": "labels",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.business",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "transit",
                    "elementType": "labels.icon",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.attraction",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.government",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.medical",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.park",
                    "elementType": "labels",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.place_of_worship",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.school",
                    "stylers": [{"visibility": "off"}]
                },
                {
                    "featureType": "poi.sports_complex",
                    "stylers": [{"visibility": "off"}]
                }
            ]
        """.trimIndent())
    }
    
    // Map properties
    val mapProperties = MapProperties(
        mapType = MapType.NORMAL,
        isMyLocationEnabled = hasLocationPermission,
        mapStyleOptions = if (isDarkTheme) darkMapStyle else lightMapStyle
    )
    
    val uiSettings = MapUiSettings(
        zoomControlsEnabled = false, // Disabled - use pinch to zoom instead
        myLocationButtonEnabled = false, // Disabled - using custom FAB instead
        compassEnabled = true,
        mapToolbarEnabled = false
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Bhandara") },
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
                // Add markers for nearby feasts
                nearbyFeasts.forEach { feast ->
                    Marker(
                        state = MarkerState(position = LatLng(feast.latitude, feast.longitude)),
                        title = feast.organizerName,
                        snippet = buildString {
                            append(feast.address)
                            feast.distance?.let {
                                val distanceKm = it / 1000.0
                                append(" • ${String.format("%.1f", distanceKm)} km away")
                            }
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            // Navigate to feast details
                            onFeastClick(feast.id)
                            true // Return true to indicate the event was consumed
                        }
                    )
                }
            }
        }
    }
}
