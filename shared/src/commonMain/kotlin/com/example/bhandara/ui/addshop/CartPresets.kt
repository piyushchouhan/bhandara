package com.example.bhandara.ui.addshop

// What a moving cart's Items step suggests for each thing it sells (design "Add Shop Flow" 3j, based on the common
// Pune hawker types). Owners can always add their own item.

/** One item a cart sells, as the owner fills it in: its price as typed, so "12." can become "12.5" */
data class CartItem(
    val id: Long,
    val category: String,
    val name: String,
    val unit: String,
    val price: String = "",
    /** Typed in by the owner (its name can be changed), not picked from the suggestions */
    val custom: Boolean = false,
)

/** A suggested item and the unit it's usually sold by */
data class PresetItem(val name: String, val unit: String)

/**
 * A thing a cart sells and what comes with it
 *
 * @param starters added as rows when the category is picked
 * @param quickAdd one tap away under the list
 * @param options extra on/off settings shown under the list, with whether each starts on
 * @param seasonFrom / [seasonTo] the months it's out by default (0 = January), when it has a season
 */
data class CartPreset(
    val units: List<String>,
    val starters: List<PresetItem> = emptyList(),
    val quickAdd: List<PresetItem> = emptyList(),
    val options: Map<String, Boolean> = emptyMap(),
    val seasonFrom: Int? = null,
    val seasonTo: Int? = null,
)

// The extra settings, by key
const val OPTION_DAILY_RATES = "daily_rates"
const val OPTION_HOME_DELIVERY = "home_delivery"
const val OPTION_MONTHLY_BILL = "monthly_bill"
const val OPTION_STANDING_ORDERS = "standing_orders"
const val OPTION_WEEKLY_REMINDER = "weekly_reminder"
const val OPTION_DELIVERS_HOMES = "delivers_homes"
const val OPTION_PURE_VEG = "pure_veg"
const val OPTION_JAIN = "jain"
const val OPTION_SIZE_OPTIONS = "size_options"
const val OPTION_PLAYS_TUNE = "plays_tune"

/** Units offered for something the owner named themselves */
val CUSTOM_UNITS = listOf("piece", "kg", "plate", "packet")

val CART_PRESETS: Map<String, CartPreset> = mapOf(
    "Sabzi" to CartPreset(
        units = listOf("kg", "½ kg", "bunch"),
        starters = listOf(PresetItem("Tomato", "kg"), PresetItem("Onion", "kg"), PresetItem("Methi", "bunch"), PresetItem("Kothimbir", "bunch")),
        quickAdd = listOf(PresetItem("Potato", "kg"), PresetItem("Bhindi", "kg"), PresetItem("Palak", "bunch"), PresetItem("Mirchi", "½ kg")),
        options = mapOf(OPTION_DAILY_RATES to true),
    ),
    "Fruits" to CartPreset(
        units = listOf("dozen", "kg", "piece"),
        starters = listOf(PresetItem("Banana", "dozen"), PresetItem("Papaya", "piece")),
        quickAdd = listOf(PresetItem("Mango", "dozen"), PresetItem("Sitaphal", "kg"), PresetItem("Apple", "kg"), PresetItem("Chikoo", "kg")),
        options = mapOf(OPTION_DAILY_RATES to true),
    ),
    "Milk & dairy" to CartPreset(
        units = listOf("litre", "½ L", "kg"),
        starters = listOf(PresetItem("Cow milk", "litre"), PresetItem("Buffalo milk", "litre"), PresetItem("Dahi", "kg")),
        quickAdd = listOf(PresetItem("Ghee", "kg"), PresetItem("Taak", "litre"), PresetItem("Lassi", "½ L"), PresetItem("Shrikhand", "kg")),
        options = mapOf(OPTION_HOME_DELIVERY to true, OPTION_MONTHLY_BILL to true),
    ),
    "Bread & pav" to CartPreset(
        units = listOf("packet", "piece", "6 pcs", "200 g"),
        starters = listOf(PresetItem("Ladi pav", "6 pcs"), PresetItem("Bread", "packet"), PresetItem("Khari", "200 g")),
        quickAdd = listOf(PresetItem("Brun", "piece"), PresetItem("Nankhatai", "200 g"), PresetItem("Cake rusk", "packet"), PresetItem("Eggs", "piece")),
        options = mapOf(OPTION_STANDING_ORDERS to true),
    ),
    "Eggs" to CartPreset(
        units = listOf("dozen", "tray of 30", "piece"),
        starters = listOf(PresetItem("Eggs", "dozen"), PresetItem("Eggs", "tray of 30"), PresetItem("Brown eggs", "dozen")),
        quickAdd = listOf(PresetItem("Boiled eggs", "piece"), PresetItem("Desi eggs", "dozen")),
        options = mapOf(OPTION_WEEKLY_REMINDER to true, OPTION_DELIVERS_HOMES to false),
    ),
    "Chaat & snacks" to CartPreset(
        units = listOf("plate", "6 pcs", "piece"),
        starters = listOf(PresetItem("Bhel", "plate"), PresetItem("Pani puri", "6 pcs"), PresetItem("Vada pav", "piece")),
        quickAdd = listOf(PresetItem("Sev puri", "plate"), PresetItem("Dahi puri", "plate"), PresetItem("Ragda pattice", "plate"), PresetItem("Bhutta", "piece")),
        options = mapOf(OPTION_PURE_VEG to true, OPTION_JAIN to false),
    ),
    "Chai & drinks" to CartPreset(
        units = listOf("cup", "glass", "piece"),
        starters = listOf(PresetItem("Cutting chai", "cup"), PresetItem("Ganne ras", "glass"), PresetItem("Naral pani", "piece")),
        quickAdd = listOf(PresetItem("Coffee", "cup"), PresetItem("Nimbu pani", "glass"), PresetItem("Kokum sarbat", "glass"), PresetItem("Taak", "glass")),
        options = mapOf(OPTION_SIZE_OPTIONS to true),
        // Ganne ras is a summer drink
        seasonFrom = 2, seasonTo = 5,
    ),
    "Ice cream" to CartPreset(
        units = listOf("piece", "matka"),
        starters = listOf(PresetItem("Matka kulfi", "matka"), PresetItem("Kulfi stick", "piece"), PresetItem("Gola", "piece")),
        quickAdd = listOf(PresetItem("Malai kulfi", "piece"), PresetItem("Kesar pista", "piece"), PresetItem("Falooda", "glass")),
        options = mapOf(OPTION_PLAYS_TUNE to true),
        seasonFrom = 2, seasonTo = 5,
    ),
)

/** What a category brings; one the owner named (or "Other") brings nothing but a few units */
fun presetFor(category: String): CartPreset = CART_PRESETS[category] ?: CartPreset(units = CUSTOM_UNITS)

/** Items a chai cart marks as seasonal by default */
val SEASONAL_BY_DEFAULT = setOf("Ganne ras")
