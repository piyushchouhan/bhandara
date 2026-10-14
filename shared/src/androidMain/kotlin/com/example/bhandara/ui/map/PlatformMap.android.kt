package com.example.bhandara.ui.map

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalDensity
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
import com.google.maps.android.compose.Polygon
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
    look: MapLook,
    contentPadding: PaddingValues,
    zones: List<MapZone>,
    onZoneClick: (zoneId: String) -> Unit,
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

    val style = remember(look, darkMap) { MapStyleOptions(MapStyles.forLook(look, darkMap)) }
    val zoneColors = remember(darkMap) { zoneColors(darkMap) }
    val zoneStrokePx = with(LocalDensity.current) { ZONE_STROKE_WIDTH.toPx() }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        contentPadding = contentPadding,
        // The "you are here" dot needs location access; callers only turn it on once it's allowed
        properties = MapProperties(isMyLocationEnabled = showMyLocation, mapStyleOptions = style),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
        ),
    ) {
        zones.forEach { zone ->
            key(zone.id) {
                Polygon(
                    points = zone.outline.map { it.toLatLng() },
                    fillColor = if (zone.faint) zoneColors.faintFill else zoneColors.fill,
                    strokeColor = if (zone.faint) zoneColors.faintStroke else zoneColors.stroke,
                    strokeWidth = zoneStrokePx,
                    clickable = true,
                    onClick = { onZoneClick(zone.id) },
                )
            }
        }
        pins.forEach { pin ->
            key(pin.id) {
                val image = rememberPinImage(pin, darkMap, look)
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
