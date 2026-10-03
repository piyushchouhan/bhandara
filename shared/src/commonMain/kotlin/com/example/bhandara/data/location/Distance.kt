package com.example.bhandara.data.location

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

/** Straight-line distance between two places on the Earth (haversine), in metres */
fun distanceMeters(from: GeoLocation, to: GeoLocation): Double {
    fun Double.radians() = this * kotlin.math.PI / 180
    val dLat = (to.latitude - from.latitude).radians()
    val dLon = (to.longitude - from.longitude).radians()
    val a = sin(dLat / 2).pow(2) +
        cos(from.latitude.radians()) * cos(to.latitude.radians()) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}

/** A distance as people read it: "Here", "450 m" (to 10 m), "1.2 km", "12 km" */
fun formatDistance(meters: Double): String {
    val roundedMeters = (meters / 10).roundToInt() * 10
    return when {
        meters < 5 -> "Here"
        roundedMeters < 1_000 -> "$roundedMeters m"
        meters < 9_950 -> {
            val tenths = (meters / 100).roundToLong() // kilometres with one decimal, e.g. 12 -> 1.2
            if (tenths % 10 == 0L) "${tenths / 10} km" else "${tenths / 10}.${tenths % 10} km"
        }
        else -> "${(meters / 1_000).roundToLong()} km"
    }
}
