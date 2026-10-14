package com.example.bhandara.ui.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.bhandara.ui.theme.FeastColors

/** How the map and its pins are drawn: the original look, or the redesign's warm map with indigo/saffron pins */
enum class MapLook { CLASSIC, FEAST }

/** Google map styles (the same JSON works on Android and iOS): fewer distractions so the app's pins stand out */
object MapStyles {

    fun forLook(look: MapLook, dark: Boolean): String = when (look) {
        MapLook.CLASSIC -> if (dark) DARK else LIGHT
        MapLook.FEAST -> if (dark) FEAST_DARK else FEAST_LIGHT
    }

    private val FEAST_LIGHT = feast(FeastColors.Light)
    private val FEAST_DARK = feast(FeastColors.Dark)

    /**
     * The redesign's map: its land, water, park and road colours; shops, transit, other places' icons and lane names
     * hidden (main roads and areas keep their names, so people can still tell where a place is)
     */
    private fun feast(c: FeastColors): String {
        val labelStroke = if (c.isDark) c.mapLand else Color.White
        return """
            [
                { "elementType": "geometry", "stylers": [{"color": "${c.mapLand.hex()}"}] },
                { "elementType": "labels.icon", "stylers": [{"visibility": "off"}] },
                { "elementType": "labels.text.fill", "stylers": [{"color": "${c.muted.hex()}"}] },
                { "elementType": "labels.text.stroke", "stylers": [{"color": "${labelStroke.hex()}"}] },
                { "featureType": "poi", "stylers": [{"visibility": "off"}] },
                { "featureType": "poi.park", "stylers": [{"visibility": "on"}] },
                { "featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "${c.mapPark.hex()}"}] },
                { "featureType": "poi.park", "elementType": "labels", "stylers": [{"visibility": "off"}] },
                { "featureType": "road", "elementType": "geometry", "stylers": [{"color": "${c.mapRoad.hex()}"}] },
                { "featureType": "transit", "stylers": [{"visibility": "off"}] },
                { "featureType": "road.local", "elementType": "labels", "stylers": [{"visibility": "off"}] },
                { "featureType": "water", "elementType": "geometry", "stylers": [{"color": "${c.mapWater.hex()}"}] },
                { "featureType": "water", "elementType": "labels", "stylers": [{"visibility": "off"}] }
            ]
        """.trimIndent()
    }

    private fun Color.hex(): String = "#" + (toArgb() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

    private val DARK = """
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
    """.trimIndent()

    private val LIGHT = """
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
    """.trimIndent()
}
