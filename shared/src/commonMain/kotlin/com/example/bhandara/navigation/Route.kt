package com.example.bhandara.navigation

import kotlinx.serialization.Serializable

/** A screen in the app, with what it needs to open */
@Serializable
sealed interface Route {

    @Serializable
    data object Home : Route

    /** Community feasts near the user */
    @Serializable
    data object Hungry : Route

    /** Post a community feast */
    @Serializable
    data object ReportFeast : Route

    @Serializable
    data class FeastDetails(val feastId: String) : Route

    @Serializable
    data object AddShop : Route

    /** Shops and moving carts on the map */
    @Serializable
    data object ShopsMap : Route

    @Serializable
    data class ShopDetails(val shopId: String) : Route

    /** The owner claims a shop listed for them */
    @Serializable
    data class ClaimShop(val shopId: String, val shopName: String) : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Support : Route

    @Serializable
    data object Terms : Route
}
