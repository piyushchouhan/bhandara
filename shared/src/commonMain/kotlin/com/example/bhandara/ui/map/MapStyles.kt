package com.example.bhandara.ui.map

/** Google map styles (the same JSON works on Android and iOS): fewer distractions so the app's pins stand out */
object MapStyles {

    val DARK = """
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

    val LIGHT = """
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
