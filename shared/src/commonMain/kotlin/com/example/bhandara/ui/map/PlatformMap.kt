package com.example.bhandara.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bhandara.data.location.GeoLocation

/** A place shown on the map: an emoji with the place's name underneath */
data class MapPin(
    val id: String,
    val position: GeoLocation,
    val label: String,
    val emoji: String,
    /** Live moving carts are drawn green so they stand out from fixed shops */
    val isLiveCart: Boolean = false,
)

/**
 * Where the map should look. The map moves there when given a new target, so to re-centre on the same spot
 * again, pass a new instance (each has its own [id]).
 */
class CameraTarget(val center: GeoLocation, val zoom: Float = DEFAULT_ZOOM) {
    val id: Long = nextId++

    companion object {
        const val DEFAULT_ZOOM = 14f
        private var nextId = 0L
    }
}

/**
 * A Google map with the app's pins, the same on Android (Maps Compose) and iOS (Google Maps SDK).
 *
 * @param showMyLocation the blue "you are here" dot; only pass true once location access is allowed
 * @param onZoomChanged after the user zooms, with the new zoom level (e.g. to show more pins up close)
 */
@Composable
expect fun PlatformMap(
    camera: CameraTarget,
    pins: List<MapPin>,
    showMyLocation: Boolean,
    darkMap: Boolean,
    onPinClick: (MapPin) -> Unit,
    modifier: Modifier = Modifier,
    onZoomChanged: (Float) -> Unit = {},
)
