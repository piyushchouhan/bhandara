package com.example.bhandara.ui.menu

import com.example.bhandara.data.models.api.MenuItemRequest
import kotlin.math.roundToLong

// The backend's limits for menu items (MenuItemRequestDTO, MenuItemBulkRequestDTO)
const val MIN_MENU_NAME = 2
const val MAX_MENU_NAME = 255
const val MAX_MENU_ITEMS_AT_ONCE = 300
const val MAX_MENU_PHOTOS = 10

/** The food types people pick from; the backend also knows VEGAN, JAIN and UNKNOWN (e.g. from AI) */
val PICKABLE_FOOD_TYPES = listOf("VEG", "NON_VEG", "EGG")

/** One menu item being typed in: the price as typed, so "12." can become "12.5" */
data class MenuRow(val id: Long, val name: String = "", val price: String = "", val foodType: String = "VEG") {
    val isBlank: Boolean get() = name.isBlank() && price.isBlank()
}

/** "20" for 20.0, "12.5" for 12.5: how a saved price is shown for editing */
fun priceText(price: Double?): String = when {
    price == null -> ""
    price % 1.0 == 0.0 -> price.toLong().toString()
    else -> ((price * 100).roundToLong() / 100.0).toString()
}

/** Keeps what can be part of a price: digits and one decimal point, at most 2 decimals */
fun cleanPrice(text: String): String {
    val kept = StringBuilder()
    var seenDot = false
    var decimals = 0
    for (c in text) {
        when {
            c.isDigit() && (!seenDot || decimals < 2) -> {
                kept.append(c)
                if (seenDot) decimals++
            }
            c == '.' && !seenDot -> {
                seenDot = true
                kept.append(c)
            }
        }
    }
    return kept.toString().take(9)
}

/**
 * The rows as items to save, or the first problem: blank rows are skipped; names must be 2-255 characters and not
 * repeated (in the list or on the menu already, ignoring case); a price, if given, must be more than 0.
 */
fun menuItemsFrom(rows: List<MenuRow>, existingNames: Collection<String>): Result<List<MenuItemRequest>> {
    val filled = rows.filterNot { it.isBlank }
    if (filled.isEmpty()) return Result.failure(MenuProblem("Add at least one item"))
    if (filled.size > MAX_MENU_ITEMS_AT_ONCE) return Result.failure(MenuProblem("You can save up to $MAX_MENU_ITEMS_AT_ONCE items at once"))
    val onMenu = existingNames.map { it.trim().lowercase() }.toSet()
    val seen = mutableSetOf<String>()
    val items = filled.map { row ->
        val name = row.name.trim()
        if (name.length < MIN_MENU_NAME) {
            return Result.failure(MenuProblem(if (name.isEmpty()) "Every item needs a name" else "\"$name\" is too short for an item name"))
        }
        if (name.length > MAX_MENU_NAME) return Result.failure(MenuProblem("\"${name.take(20)}…\" is too long for an item name"))
        val key = name.lowercase()
        if (key in onMenu) return Result.failure(MenuProblem("\"$name\" is already on the menu"))
        if (!seen.add(key)) return Result.failure(MenuProblem("\"$name\" is listed more than once"))
        val price = row.price.takeIf { it.isNotBlank() }?.let {
            it.toDoubleOrNull()?.takeIf { value -> value > 0 } ?: return Result.failure(MenuProblem("Check the price of \"$name\""))
        }
        MenuItemRequest(name = name, foodType = row.foodType, price = price)
    }
    return Result.success(items)
}

class MenuProblem(message: String) : Exception(message)
