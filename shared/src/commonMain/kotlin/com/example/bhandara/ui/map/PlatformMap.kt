package com.example.bhandara.ui.map

import androidx.compose.foundation.layout.PaddingValues
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
    /** The place the user tapped: drawn larger in the redesign's look */
    val selected: Boolean = false,
    /** Another place is selected: drawn faded in the redesign's look */
    val dimmed: Boolean = false,
    /** A community feast in the redesign's look: a pill with this text (e.g. "Now", "6 PM") instead of a drop */
    val feastPill: String? = null,
    /** The feast is on now: the pill is saffron */
    val feastLive: Boolean = false,
    /** A shop zone's label: a pill with [emoji], the zone's name ([label], may be blank) and "N shops" */
    val zoneCount: Int? = null,
)

/** A group of nearby shops drawn as one shape; [faint] once the map is close enough to show their own pins */
data class MapZone(
    val id: String,
    val outline: List<GeoLocation>,
    val faint: Boolean = false,
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
 * @param look the original look or the redesign's
 * @param contentPadding the parts covered by the screen's own bars and sheets: the map centres in what's left and
 *                       keeps the Google logo visible
 * @param zones shapes drawn under the pins (groups of nearby shops); [onZoneClick] gets the tapped one's id
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
    look: MapLook = MapLook.CLASSIC,
    contentPadding: PaddingValues = PaddingValues(),
    zones: List<MapZone> = emptyList(),
    onZoneClick: (zoneId: String) -> Unit = {},
)
