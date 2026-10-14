package com.example.bhandara.navigation

/**
 * Where tapping a notification takes the user, from the data the backend sends with it ("type" and ids):
 * - new_localfeast: the feast
 * - report_update: My reports
 * - shop_review: the shop's page, with its reviews
 * - claim_status: the shop to manage when approved, else its page
 * Null for anything else (the app just opens).
 */
fun routeForNotification(data: Map<String, String?>): Route? {
    fun id(key: String) = data[key]?.trim()?.takeIf { it.isNotEmpty() }
    return when (data["type"]) {
        "new_localfeast" -> id("feastId")?.let { Route.FeastDetails(it) }
        "report_update" -> Route.MyReports
        "shop_review" -> id("shopId")?.let { Route.ShopDetails(it) }
        "claim_status" -> id("shopId")?.let { if (data["approved"] == "true") Route.ManageShop(it) else Route.ShopDetails(it) }
        else -> null
    }
}

/** The data keys a notification can carry, for platforms that pass them on one by one (e.g. Android intent extras) */
val NOTIFICATION_DATA_KEYS = listOf("type", "feastId", "shopId", "kind", "approved")
