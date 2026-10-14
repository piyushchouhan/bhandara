package com.example.bhandara.ui.shopdetails

import com.example.bhandara.ui.addshop.OPTION_PLAYS_TUNE
import com.example.bhandara.ui.addshop.OPTION_JAIN
import com.example.bhandara.ui.addshop.OPTION_PURE_VEG
import com.example.bhandara.ui.addshop.OPTION_MONTHLY_BILL
import com.example.bhandara.ui.addshop.OPTION_STANDING_ORDERS
import com.example.bhandara.ui.addshop.OPTION_DELIVERS_HOMES
import com.example.bhandara.ui.addshop.OPTION_HOME_DELIVERY
import com.example.bhandara.ui.addshop.clockTime
import kotlinx.datetime.LocalTime
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.MenuItemResponse
import kotlin.math.roundToInt
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
fun formatPrice(price: Double, unit: String? = null): String {
    val paise = (price * 100).roundToLong()
    val rest = paise % 100
    val amount = if (rest == 0L) "₹${paise / 100}" else "₹${paise / 100}.${rest.toString().padStart(2, '0')}"
    // A rate with what it's for: "₹40 / kg"
    return unit?.trim()?.takeIf { it.isNotEmpty() }?.let { "$amount / $it" } ?: amount
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

/** "4.3": a rating with one decimal */
fun formatRating(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

/** What kind of food place it is, for the line under its name: the cuisine if known, else the shop type */
fun shopKind(shop: LocalShopResponse): String? =
    listOfNotNull(shop.cuisineType, shop.shopType).map { it.trim() }.firstOrNull { it.isNotEmpty() }

/** How many of the 4 "how busy" bars are lit: quiet 1, moderately busy 2, busy all 4; 0 when unknown */
fun crowdBars(level: String?): Int = when (level) {
    "QUIET" -> 1
    "MODERATE" -> 2
    "BUSY" -> 4
    else -> 0
}

/** Veg, vegan and Jain dishes count as veg */
fun isVeg(foodType: String): Boolean = foodType in setOf("VEG", "VEGAN", "JAIN")

/** The menu as the user wants it (Settings → Show egg items): egg dishes left out when they're turned off */
fun menuWithPreferences(menu: List<MenuItemResponse>, showEggItems: Boolean): List<MenuItemResponse> =
    if (showEggItems) menu else menu.filterNot { it.foodType == "EGG" }

/** The "Veg" filter only helps when the menu has both veg and other dishes */
fun showsVegFilter(menu: List<MenuItemResponse>): Boolean =
    menu.any { isVeg(it.foodType) } && menu.any { !isVeg(it.foodType) }

/**
 * When a cart is at a stop, from the backend's "HH:mm:ss": "7:00 – 9:30 AM" (AM / PM once when both are in the same
 * half), "4:00 PM" with only a start, null when the times vary or can't be read
 */
fun stopTimes(from: String?, to: String?, timesVary: Boolean): String? {
    if (timesVary) return null
    val start = from?.let(::parseTime) ?: return null
    val end = to?.let(::parseTime) ?: return clockTime(start, withHalf = true)
    return "${clockTime(start, withHalf = (start.hour < 12) != (end.hour < 12))} – ${clockTime(end, withHalf = true)}"
}

private fun parseTime(text: String): LocalTime? = runCatching { LocalTime.parse(text) }.getOrNull()

/** Extra settings of what a cart sells that matter to customers, in the order shown; the rest are for the vendor */
val CUSTOMER_CART_OPTIONS = listOf(
    OPTION_HOME_DELIVERY, OPTION_DELIVERS_HOMES, OPTION_STANDING_ORDERS, OPTION_MONTHLY_BILL, OPTION_PURE_VEG, OPTION_JAIN, OPTION_PLAYS_TUNE,
)

/** The customer-facing settings a cart has turned on, each once (e.g. two categories both deliver to homes) */
fun cartOptionsOn(options: Map<String, Boolean>): List<String> {
    val on = options.filterValues { it }.keys.map { it.substringAfter('/') }.toSet()
    return CUSTOMER_CART_OPTIONS.filter { it in on }
}

/** A count as it fits a small stat: "640", "1.2k", "12k", "1.5M" */
fun compactCount(count: Long): String = when {
    count < 1_000 -> count.toString()
    count < 10_000 -> oneDecimal(count / 100) + "k"
    count < 1_000_000 -> "${count / 1_000}k"
    else -> oneDecimal(count / 100_000) + "M"
}

/** 12 -> "1.2", 10 -> "1" (tenths, no trailing ".0") */
private fun oneDecimal(tenths: Long): String = if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"

