package com.example.bhandara.data.location

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Distances between places, and how they're shown to users */
class DistanceTest {

    @Test
    fun theDistanceBetweenTwoPlacesIsMeasuredOnTheEarth() {
        // 0.001 degrees of latitude is about 111 m; Pune to Mumbai is about 120 km as the crow flies
        val vishrantwadi = GeoLocation(18.5823, 73.8845)
        assertTrue(abs(distanceMeters(vishrantwadi, GeoLocation(18.5833, 73.8845)) - 111.2) < 1)
        assertTrue(abs(distanceMeters(GeoLocation(18.5204, 73.8567), GeoLocation(19.0760, 72.8777)) - 119_800) < 1_500)
        assertEquals(0.0, distanceMeters(vishrantwadi, vishrantwadi))
    }

    @Test
    fun shortDistancesAreShownInMetres() {
        assertEquals("Here", formatDistance(4.0))
        assertEquals("10 m", formatDistance(12.0))
        assertEquals("450 m", formatDistance(447.0))
        assertEquals("990 m", formatDistance(994.0))
    }

    @Test
    fun longerDistancesAreShownInKilometres() {
        assertEquals("1 km", formatDistance(996.0))
        assertEquals("1.2 km", formatDistance(1_240.0))
        assertEquals("9.9 km", formatDistance(9_940.0))
        assertEquals("12 km", formatDistance(12_400.0))
        assertEquals("120 km", formatDistance(119_800.0))
    }
}
