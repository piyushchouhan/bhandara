package com.example.bhandara.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.bhandara.data.location.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
actual fun PlatformMap(
    camera: CameraTarget,
    pins: List<MapPin>,
    showMyLocation: Boolean,
    darkMap: Boolean,
    onPinClick: (MapPin) -> Unit,
    modifier: Modifier,
    onZoomChanged: (Float) -> Unit,
) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(camera.center.toLatLng(), camera.zoom)
    }
    LaunchedEffect(camera.id) {
        cameraState.animate(CameraUpdateFactory.newLatLngZoom(camera.center.toLatLng(), camera.zoom))
    }
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)
    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.position.zoom }.distinctUntilChanged().collect { currentOnZoomChanged(it) }
    }

    val style = remember(darkMap) { MapStyleOptions(if (darkMap) MapStyles.DARK else MapStyles.LIGHT) }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        // The "you are here" dot needs location access; callers only turn it on once it's allowed
        properties = MapProperties(isMyLocationEnabled = showMyLocation, mapStyleOptions = style),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
        ),
    ) {
        pins.forEach { pin ->
            key(pin.id) {
                val image = rememberPinImage(pin, darkMap)
                val icon = remember(image) { BitmapDescriptorFactory.fromBitmap(image.bitmap.asAndroidBitmap()) }
                val markerState = rememberMarkerState(key = pin.id, position = pin.position.toLatLng())
                SideEffect { markerState.position = pin.position.toLatLng() } // live carts move
                Marker(
                    state = markerState,
                    icon = icon,
                    anchor = Offset(image.anchorX, image.anchorY),
                    onClick = {
                        onPinClick(pin)
                        true
                    },
                )
            }
        }
    }
}

private fun GeoLocation.toLatLng() = LatLng(latitude, longitude)
