package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.models.api.LocalShopResponse
import kotlin.math.roundToLong

// How the shop details page writes a shop's times, address, prices and facilities

/** "9:00 AM" for the backend's "09:00:00" (or "09:00"); anything unreadable is shown as it came */
fun formatShopTime(time: String): String {
    val parts = time.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()
    val minute = parts.getOrNull(1)?.toIntOrNull()
    if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) return time
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    val half = if (hour < 12) "AM" else "PM"
    return "$hour12:${minute.toString().padStart(2, '0')} $half"
}

/** "9:00 AM – 10:00 PM", or null unless both times are known */
fun openingHours(shop: LocalShopResponse): String? {
    val opens = shop.openingTime?.takeIf { it.isNotBlank() } ?: return null
    val closes = shop.closingTime?.takeIf { it.isNotBlank() } ?: return null
    return "${formatShopTime(opens)} – ${formatShopTime(closes)}"
}

/** The full address if there is one, else "area, city"; null when the shop has neither */
fun shopAddress(shop: LocalShopResponse): String? =
    shop.fullAddress?.trim()?.takeIf { it.isNotEmpty() }
        ?: listOfNotNull(shop.area, shop.city)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(", ")
            .takeIf { it.isNotEmpty() }

/**
 * The landmark without a leading "Near" (people often type "Near Datta Mandir"), since the screen adds it in the
 * user's language; null when blank
 */
fun landmarkName(landmark: String?): String? =
    landmark?.trim()
        ?.replace(Regex("^near\\b[\\s:,-]*", RegexOption.IGNORE_CASE), "")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

/** "Restaurant • South Indian", or null when neither is known */
fun shopTypeLine(shop: LocalShopResponse): String? =
    listOfNotNull(shop.shopType, shop.cuisineType)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" • ")
        .takeIf { it.isNotEmpty() }

/** ₹20 for whole rupees, ₹20.50 otherwise */
fun formatPrice(price: Double): String {
    val paise = (price * 100).roundToLong()
    val rest = paise % 100
    return if (rest == 0L) "₹${paise / 100}" else "₹${paise / 100}.${rest.toString().padStart(2, '0')}"
}

/** What a shop offers besides food; the screen shows each in the user's language */
enum class Facility { SEATING, HOME_DELIVERY, TAKEAWAY, WIFI, ONLINE_PAYMENT }

/** The facilities the shop says it has, in a fixed order; unknown counts as "no" */
fun facilities(shop: LocalShopResponse): List<Facility> = buildList {
    if (shop.hasSeating == true) add(Facility.SEATING)
    if (shop.homeDelivery == true) add(Facility.HOME_DELIVERY)
    if (shop.takeaway == true) add(Facility.TAKEAWAY)
    if (shop.wifiAvailable == true) add(Facility.WIFI)
    if (shop.onlinePayment == true) add(Facility.ONLINE_PAYMENT)
}
