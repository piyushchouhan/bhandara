package com.example.bhandara.ui.addshop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_button
import com.example.bhandara.shared.resources.add_category
import com.example.bhandara.shared.resources.add_category_text
import com.example.bhandara.shared.resources.added_label
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.category_name_hint
import com.example.bhandara.shared.resources.daily_delivery
import com.example.bhandara.shared.resources.daily_delivery_text
import com.example.bhandara.shared.resources.delivers_homes
import com.example.bhandara.shared.resources.delivers_homes_text
import com.example.bhandara.shared.resources.done
import com.example.bhandara.shared.resources.edit
import com.example.bhandara.shared.resources.edit_in_route
import com.example.bhandara.shared.resources.ex_bread
import com.example.bhandara.shared.resources.ex_chaat
import com.example.bhandara.shared.resources.ex_chai
import com.example.bhandara.shared.resources.ex_eggs
import com.example.bhandara.shared.resources.ex_fruits
import com.example.bhandara.shared.resources.ex_icecream
import com.example.bhandara.shared.resources.ex_milk
import com.example.bhandara.shared.resources.ex_other
import com.example.bhandara.shared.resources.ex_sabzi
import com.example.bhandara.shared.resources.from_route
import com.example.bhandara.shared.resources.item_name
import com.example.bhandara.shared.resources.jain_option
import com.example.bhandara.shared.resources.jain_option_text
import com.example.bhandara.shared.resources.monthly_bill
import com.example.bhandara.shared.resources.monthly_bill_text
import com.example.bhandara.shared.resources.month_letters
import com.example.bhandara.shared.resources.more_label
import com.example.bhandara.shared.resources.morning_round
import com.example.bhandara.shared.resources.morning_round_plain
import com.example.bhandara.shared.resources.no_route_times
import com.example.bhandara.shared.resources.on_cart_title
import com.example.bhandara.shared.resources.out_in
import com.example.bhandara.shared.resources.out_in_text
import com.example.bhandara.shared.resources.plays_tune
import com.example.bhandara.shared.resources.plays_tune_text
import com.example.bhandara.shared.resources.price_hint
import com.example.bhandara.shared.resources.pure_veg
import com.example.bhandara.shared.resources.pure_veg_text
import com.example.bhandara.shared.resources.quick_add
import com.example.bhandara.shared.resources.rates_daily_text
import com.example.bhandara.shared.resources.rates_daily_title
import com.example.bhandara.shared.resources.remind_rates
import com.example.bhandara.shared.resources.remind_rates_text
import com.example.bhandara.shared.resources.remove
import com.example.bhandara.shared.resources.remove_item
import com.example.bhandara.shared.resources.remove_items_text
import com.example.bhandara.shared.resources.remove_items_title
import com.example.bhandara.shared.resources.season_items_label
import com.example.bhandara.shared.resources.season_title
import com.example.bhandara.shared.resources.seasonal_items
import com.example.bhandara.shared.resources.seasonal_none
import com.example.bhandara.shared.resources.sells_other
import com.example.bhandara.shared.resources.size_options
import com.example.bhandara.shared.resources.size_options_text
import com.example.bhandara.shared.resources.something_else
import com.example.bhandara.shared.resources.standing_orders
import com.example.bhandara.shared.resources.standing_orders_text
import com.example.bhandara.shared.resources.time_from
import com.example.bhandara.shared.resources.time_to
import com.example.bhandara.shared.resources.what_sell_text
import com.example.bhandara.shared.resources.what_sell_title
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastFormField
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// A moving cart's Items step (design "Add Shop Flow" 3m, 3i, 3r, 3k–3q light; 6m–6q dark): pick what it sells,
// then what's on the cart, a tab for each, with suggestions, units, prices and each kind's own settings

private val categoryExamples: Map<String, StringResource> = mapOf(
    "Sabzi" to Res.string.ex_sabzi,
    "Fruits" to Res.string.ex_fruits,
    "Milk & dairy" to Res.string.ex_milk,
    "Bread & pav" to Res.string.ex_bread,
    "Eggs" to Res.string.ex_eggs,
    "Chaat & snacks" to Res.string.ex_chaat,
    "Chai & drinks" to Res.string.ex_chai,
    "Ice cream" to Res.string.ex_icecream,
)

/** The categories with suggestions, in the design's order ("Other" and the owner's own aren't in the grid) */
private val PRESET_CATEGORIES = CART_SELLS.filter { it in CART_PRESETS }

/** A category's name in the user's language; one the owner named stays as typed */
@Composable
private fun categoryName(category: String): String =
    cartSellsLooks[category]?.second?.let { stringResource(it) } ?: category

@Composable
private fun categoryExample(category: String): String =
    categoryExamples[category]?.let { stringResource(it) } ?: stringResource(Res.string.ex_other)

private fun categoryIcon(category: String): ImageVector = cartSellsLooks[category]?.first ?: Icons.Outlined.Edit

/** A dashed outline drawn around a box */
private fun Modifier.dashed(color: Color, radius: Dp, width: Dp = 1.5.dp) = drawBehind {
    drawRoundRect(
        color, cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
    )
}

/** "Something else" and the like: a dashed row with an accent label */
@Composable
private fun DashedAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(16.dp)).dashed(colors.starOff, 16.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Text(label, color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

/** 3m: what the cart sells, as big cards; each picked one becomes a tab of items */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryPickStep(form: ShopForm, viewModel: AddShopViewModel) {
    val colors = MaterialTheme.feastColors
    var naming by remember { mutableStateOf(false) }
    Column {
        Text(stringResource(Res.string.what_sell_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(Res.string.what_sell_text), color = colors.muted, fontSize = 14.sp, lineHeight = 19.6.sp,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp),
        )
    }
    // Two columns; the owner's own categories (and "Other") join the grid once added
    val shown = PRESET_CATEGORIES + form.cartSells.filter { it !in PRESET_CATEGORIES }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shown.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { category ->
                    CategoryCard(category, picked = category in form.cartSells, Modifier.weight(1f)) { viewModel.toggleCartSells(category) }
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
    DashedAction(Icons.Rounded.Add, stringResource(Res.string.something_else)) { naming = true }
    if (naming) NameCategorySheet(onAdd = viewModel::addCustomCategory, onDismiss = { naming = false })
}

@Composable
private fun CategoryCard(category: String, picked: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(if (picked) colors.indigo else colors.surface)
            .then(if (picked) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp).align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(categoryIcon(category), contentDescription = null, tint = if (picked) colors.saffron else colors.saffronText, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    categoryName(category), color = if (picked) colors.onIndigo else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    categoryExample(category), color = if (picked) colors.headerSubtext else colors.muted, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (picked) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.saffron, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp))
        }
    }
}

/**
 * 3i (and 3k–3q, the same screen for each kind of cart): a tab per category with its items, quick-add suggestions
 * and the category's own settings
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CartItemsStep(form: ShopForm, viewModel: AddShopViewModel, onEditRoute: () -> Unit) {
    val colors = MaterialTheme.feastColors
    var tab by rememberSaveable { mutableStateOf(form.cartSells.firstOrNull().orEmpty()) }
    val current = tab.takeIf { it in form.cartSells } ?: form.cartSells.firstOrNull()
    var addingCategory by remember { mutableStateOf(false) }
    var editingSeason by remember { mutableStateOf<String?>(null) }

    Text(stringResource(Res.string.on_cart_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        form.cartSells.forEach { category ->
            CategoryTab(categoryName(category), form.cartItems.count { it.category == category }, selected = category == current) { tab = category }
        }
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.surface).border(1.5.dp, colors.border, RoundedCornerShape(14.dp)).clickable { addingCategory = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(Res.string.add_category), tint = colors.accent, modifier = Modifier.size(20.dp))
        }
    }

    if (current != null) {
        val items = form.cartItems.filter { it.category == current }
        val preset = presetFor(current)
        if (items.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface)) {
                items.forEachIndexed { i, item ->
                    ItemRow(item, units = (preset.units + item.unit).distinct(), viewModel)
                    if (i < items.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
                }
            }
        }
        val added = items.map { it.name.lowercase() }.toSet()
        Column {
            Text(
                stringResource(Res.string.quick_add), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                preset.quickAdd.filter { it.name.lowercase() !in added }.forEach { suggestion ->
                    QuickAddChip(suggestion.name) { viewModel.addCartItem(current, suggestion) }
                }
                QuickAddChip(stringResource(Res.string.sells_other)) { viewModel.addCartItem(current) }
            }
        }
        CategoryExtras(current, form, viewModel, onEditRoute = onEditRoute, onEditSeason = { editingSeason = current })
    }

    if (addingCategory) {
        AddCategorySheet(
            form = form,
            onAdd = { category -> viewModel.toggleCartSells(category); tab = category },
            onAddCustom = { name -> viewModel.addCustomCategory(name); tab = name.trim() },
            onRemove = viewModel::toggleCartSells,
            onDismiss = { addingCategory = false },
        )
    }
    editingSeason?.let { category ->
        val season = form.seasons[category] ?: (2 to 5)
        val items = if (category == "Chai & drinks") form.cartItems.filter { it.category == category } else emptyList()
        SeasonSheet(
            from = season.first,
            to = season.second,
            items = items,
            seasonal = form.seasonalItems,
            onDone = { from, to, picked -> viewModel.setSeason(category, from, to, picked.takeIf { items.isNotEmpty() }) },
            onDismiss = { editingSeason = null },
        )
    }
}

/** A category tab with how many items it has: indigo with a saffron count when open */
@Composable
private fun CategoryTab(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            label, color = if (selected) colors.onIndigo else colors.ink, fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.labelLarge,
        )
        Box(
            Modifier.height(20.dp).widthIn(min = 20.dp).background(if (selected) colors.saffron else colors.lavender, CircleShape).padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("$count", color = if (selected) colors.onSaffron else colors.ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** An item: its name (typed for the owner's own), its unit to pick, its price, and × to drop it */
@Composable
private fun ItemRow(item: CartItem, units: List<String>, viewModel: AddShopViewModel) {
    val colors = MaterialTheme.feastColors
    var picking by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().height(50.dp).padding(start = 14.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val nameStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.ink)
        if (item.custom) {
            BasicTextField(
                value = item.name,
                onValueChange = { viewModel.onCartItemName(item.id, it) },
                singleLine = true,
                textStyle = nameStyle,
                cursorBrush = SolidColor(colors.ink),
                modifier = Modifier.weight(1f),
                decorationBox = { field ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (item.name.isEmpty()) Text(stringResource(Res.string.item_name), style = nameStyle.copy(color = colors.placeholder, fontWeight = FontWeight.Normal), maxLines = 1)
                        field()
                    }
                },
            )
        } else {
            Text(item.name, style = nameStyle, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
        Box {
            Row(
                Modifier.height(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.lavender).clickable { picking = true }.padding(start = 8.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(item.unit, color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = picking, onDismissRequest = { picking = false }, shape = RoundedCornerShape(16.dp), containerColor = colors.surface) {
                units.forEach { unit ->
                    DropdownMenuItem(text = { Text(unit, color = colors.ink) }, onClick = { picking = false; viewModel.onCartItemUnit(item.id, unit) })
                }
            }
        }
        val priceStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.ink, textAlign = TextAlign.Center)
        BasicTextField(
            value = item.price,
            onValueChange = { viewModel.onCartItemPrice(item.id, it) },
            singleLine = true,
            textStyle = priceStyle,
            cursorBrush = SolidColor(colors.ink),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(68.dp).height(36.dp).background(colors.page, RoundedCornerShape(10.dp)).border(1.5.dp, colors.border, RoundedCornerShape(10.dp)),
            decorationBox = { field ->
                Row(Modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(stringResource(Res.string.price_hint), style = priceStyle.copy(color = if (item.price.isEmpty()) colors.placeholder else colors.ink))
                    Box(Modifier.widthIn(min = 8.dp)) { field() }
                }
            },
        )
        Box(
            Modifier.size(28.dp).clip(CircleShape).clickable { viewModel.removeCartItem(item.id) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Close, stringResource(Res.string.remove_item), tint = colors.disabledText, modifier = Modifier.size(20.dp))
        }
    }
}

/** A suggestion one tap away: dashed, with a plus */
@Composable
private fun QuickAddChip(label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.height(36.dp).clip(RoundedCornerShape(12.dp)).background(colors.surface).dashed(colors.starOff, 12.dp).clickable(onClick = onClick).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
        Text(label, color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
    }
}

/** An indigo on/off switch, as in the design's settings rows */
@Composable
private fun ItemSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.feastColors
    val thumb = if (colors.isDark) colors.surface else Color.White
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.indigo, checkedThumbColor = thumb, checkedBorderColor = Color.Transparent,
            uncheckedTrackColor = colors.border, uncheckedThumbColor = thumb, uncheckedBorderColor = Color.Transparent,
        ),
    )
}

/** One of a category's settings: an icon, what it is, and a switch or a link at the end */
@Composable
private fun ExtraRow(icon: ImageVector, title: String, text: String, divider: Boolean, onClick: (() -> Unit)? = null, end: @Composable () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Text(text, color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
            }
            end()
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
    }
}

@Composable
private fun LinkText(text: String) {
    Text(text, color = MaterialTheme.feastColors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun monthName(month: Int): String = stringResource(Res.string.month_letters).split(',').getOrElse(month) { "" }.trim()

/** "Mar–Jun" */
@Composable
private fun seasonText(season: Pair<Int, Int>): String = "${monthName(season.first)}–${monthName(season.second)}"

/** "6:30–9 AM": the first stop's start to the last one's end; null without times */
private fun roundTimes(stops: List<CartStop>): String? {
    val from = stops.firstNotNullOfOrNull { it.from } ?: return null
    val to = stops.lastOrNull { it.to != null }?.to ?: return null
    fun short(t: kotlinx.datetime.LocalTime) = clockTime(t, withHalf = false).removeSuffix(":00")
    val half = { t: kotlinx.datetime.LocalTime -> if (t.hour < 12) "AM" else "PM" }
    return if (half(from) == half(to)) "${short(from)}–${short(to)} ${half(to)}" else "${short(from)} ${half(from)}–${short(to)} ${half(to)}"
}

/** The settings that come with a kind of cart (3i, 3k, 3l, 3n, 3o, 3p, 3q) */
@Composable
private fun CategoryExtras(category: String, form: ShopForm, viewModel: AddShopViewModel, onEditRoute: () -> Unit, onEditSeason: () -> Unit) {
    val colors = MaterialTheme.feastColors
    @Composable
    fun Toggle(option: String) {
        val on = form.cartOption(category, option)
        ItemSwitch(on) { viewModel.setCartOption(category, option, it) }
    }
    fun flip(option: String): () -> Unit = { viewModel.setCartOption(category, option, !form.cartOption(category, option)) }

    // Sabzi and fruit: rates change daily, on saffron as a reminder rather than a setting
    if (category == "Sabzi" || category == "Fruits") {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.saffronSoft).clickable(onClick = flip(OPTION_DAILY_RATES)).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.EventRepeat, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.rates_daily_title), color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(stringResource(Res.string.rates_daily_text), color = colors.saffronBody, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall)
            }
            Toggle(OPTION_DAILY_RATES)
        }
        return
    }
    val rows: List<@Composable (divider: Boolean) -> Unit> = when (category) {
        "Milk & dairy" -> listOf(
            { d -> ExtraRow(Icons.Outlined.Home, stringResource(Res.string.daily_delivery), stringResource(Res.string.daily_delivery_text), d, flip(OPTION_HOME_DELIVERY)) { Toggle(OPTION_HOME_DELIVERY) } },
            { d -> ExtraRow(Icons.Outlined.ReceiptLong, stringResource(Res.string.monthly_bill), stringResource(Res.string.monthly_bill_text), d, flip(OPTION_MONTHLY_BILL)) { Toggle(OPTION_MONTHLY_BILL) } },
        )
        "Bread & pav" -> listOf(
            { d ->
                val times = roundTimes(form.stops)
                ExtraRow(
                    Icons.Outlined.WbTwilight,
                    if (times != null) stringResource(Res.string.morning_round, times) else stringResource(Res.string.morning_round_plain),
                    stringResource(if (times != null) Res.string.from_route else Res.string.no_route_times),
                    d, onEditRoute,
                ) { LinkText(stringResource(Res.string.edit_in_route)) }
            },
            { d -> ExtraRow(Icons.Outlined.Repeat, stringResource(Res.string.standing_orders), stringResource(Res.string.standing_orders_text), d, flip(OPTION_STANDING_ORDERS)) { Toggle(OPTION_STANDING_ORDERS) } },
        )
        "Eggs" -> listOf(
            { d -> ExtraRow(Icons.Outlined.EventRepeat, stringResource(Res.string.remind_rates), stringResource(Res.string.remind_rates_text), d, flip(OPTION_WEEKLY_REMINDER)) { Toggle(OPTION_WEEKLY_REMINDER) } },
            { d -> ExtraRow(Icons.Outlined.Home, stringResource(Res.string.delivers_homes), stringResource(Res.string.delivers_homes_text), d, flip(OPTION_DELIVERS_HOMES)) { Toggle(OPTION_DELIVERS_HOMES) } },
        )
        "Chaat & snacks" -> listOf(
            { d -> ExtraRow(Icons.Outlined.Eco, stringResource(Res.string.pure_veg), stringResource(Res.string.pure_veg_text), d, flip(OPTION_PURE_VEG)) { Toggle(OPTION_PURE_VEG) } },
            { d -> ExtraRow(Icons.Outlined.Restaurant, stringResource(Res.string.jain_option), stringResource(Res.string.jain_option_text), d, flip(OPTION_JAIN)) { Toggle(OPTION_JAIN) } },
        )
        "Chai & drinks" -> listOf(
            { d ->
                val seasonal = form.cartItems.filter { it.category == category && it.id in form.seasonalItems }
                val season = form.seasons[category]
                val text = if (seasonal.isEmpty() || season == null) stringResource(Res.string.seasonal_none)
                else "${seasonal.joinToString(", ") { it.name }} · ${seasonText(season)}"
                ExtraRow(Icons.Outlined.WbSunny, stringResource(Res.string.seasonal_items), text, d, onEditSeason) { LinkText(stringResource(Res.string.edit)) }
            },
            { d -> ExtraRow(Icons.Outlined.LocalCafe, stringResource(Res.string.size_options), stringResource(Res.string.size_options_text), d, flip(OPTION_SIZE_OPTIONS)) { Toggle(OPTION_SIZE_OPTIONS) } },
        )
        "Ice cream" -> listOf(
            { d ->
                ExtraRow(Icons.Outlined.WbSunny, stringResource(Res.string.out_in), stringResource(Res.string.out_in_text), d, onEditSeason) {
                    LinkText(seasonText(form.seasons[category] ?: (2 to 5)))
                }
            },
            { d -> ExtraRow(Icons.Outlined.MusicNote, stringResource(Res.string.plays_tune), stringResource(Res.string.plays_tune_text), d, flip(OPTION_PLAYS_TUNE)) { Toggle(OPTION_PLAYS_TUNE) } },
        )
        else -> emptyList()
    }
    if (rows.isEmpty()) return
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface)) {
        rows.forEachIndexed { i, row -> row(i < rows.lastIndex) }
    }
}

/** 3r: the categories not added yet, the added ones to remove (asking first if they have items), and "Something else" */
@Composable
private fun AddCategorySheet(
    form: ShopForm,
    onAdd: (String) -> Unit,
    onAddCustom: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    var removing by remember { mutableStateOf<String?>(null) }
    var naming by remember { mutableStateOf(false) }
    FeastSheet(onDismiss) { close ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.add_category), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.add_category_text), color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
            }
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.page).clickable { close {} }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.ink, modifier = Modifier.size(22.dp))
            }
        }
        if (form.cartSells.isNotEmpty()) {
            SheetLabel(stringResource(Res.string.added_label), Modifier.padding(top = 12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                form.cartSells.forEach { category ->
                    Row(
                        Modifier.height(36.dp).clip(RoundedCornerShape(12.dp)).background(colors.indigo)
                            .clickable { if (form.cartItems.any { it.category == category }) removing = category else onRemove(category) }
                            .padding(start = 12.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(categoryName(category), color = colors.onIndigo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(Res.string.remove), tint = colors.headerSubtext, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        val more = PRESET_CATEGORIES.filter { it !in form.cartSells }
        if (more.isNotEmpty()) {
            SheetLabel(stringResource(Res.string.more_label), Modifier.padding(top = 14.dp))
            more.forEachIndexed { i, category ->
                Row(
                    Modifier.fillMaxWidth().height(56.dp).clickable { close { onAdd(category) } }.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(40.dp).background(colors.saffronSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(categoryIcon(category), contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(categoryName(category), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Text(categoryExample(category), color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                    }
                    Box(Modifier.size(44.dp).background(colors.lavender, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                    }
                }
                if (i < more.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
            }
        }
        Box(Modifier.padding(top = 12.dp)) {
            if (naming) {
                CategoryNameField(onAdd = { name -> close { onAddCustom(name) } })
            } else {
                DashedAction(Icons.Outlined.Edit, stringResource(Res.string.something_else)) { naming = true }
            }
        }
    }
    removing?.let { category ->
        val count = form.cartItems.count { it.category == category }
        AlertDialog(
            onDismissRequest = { removing = null },
            title = { Text(stringResource(Res.string.remove_items_title, count)) },
            text = { Text(stringResource(Res.string.remove_items_text, categoryName(category))) },
            confirmButton = {
                TextButton(onClick = { removing = null; onRemove(category) }, colors = ButtonDefaults.textButtonColors(contentColor = colors.danger)) {
                    Text(stringResource(Res.string.remove))
                }
            },
            dismissButton = { TextButton(onClick = { removing = null }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun SheetLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(), color = MaterialTheme.feastColors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp,
        style = MaterialTheme.typography.labelMedium, modifier = modifier.padding(bottom = 6.dp),
    )
}

/** Naming a category of the owner's own, with Add beside it */
@Composable
private fun CategoryNameField(onAdd: (String) -> Unit) {
    val colors = MaterialTheme.feastColors
    var name by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FeastFormField(name, stringResource(Res.string.category_name_hint), { name = it.take(40) }, height = 48.dp, textSize = 15, modifier = Modifier.weight(1f))
        FeastButton(stringResource(Res.string.add_button), onClick = { onAdd(name) }, enabled = name.trim().isNotEmpty(), height = 48.dp, modifier = Modifier.width(88.dp))
    }
}

/** "Something else" on 3m: a small sheet to name the category */
@Composable
private fun NameCategorySheet(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Text(stringResource(Res.string.something_else), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.add_category_text), color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))
        CategoryNameField(onAdd = { name -> close { onAdd(name) } })
    }
}

/** When a seasonal cart is out (and, for drinks, which items only then): months to pick from and to */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeasonSheet(
    from: Int,
    to: Int,
    items: List<CartItem>,
    seasonal: Set<Long>,
    onDone: (from: Int, to: Int, seasonalItems: Set<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    var start by remember { mutableStateOf(from) }
    var end by remember { mutableStateOf(to) }
    var picked by remember { mutableStateOf(items.filter { it.id in seasonal }.map { it.id }.toSet()) }
    FeastSheet(onDismiss) { close ->
        Text(stringResource(Res.string.season_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        listOf(Res.string.time_from to true, Res.string.time_to to false).forEach { (label, isStart) ->
            SheetLabel(stringResource(label), Modifier.padding(top = 14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp), maxItemsInEachRow = 6) {
                (0..11).forEach { month ->
                    val selected = if (isStart) start == month else end == month
                    PickChip(monthName(month), selected, { if (isStart) start = month else end = month }, pickedIcon = null, height = 36, fontSize = 13, modifier = Modifier.weight(1f))
                }
            }
        }
        if (items.isNotEmpty()) {
            SheetLabel(stringResource(Res.string.season_items_label), Modifier.padding(top = 14.dp))
            items.forEach { item ->
                CheckRow(item.name.ifBlank { "–" }, item.id in picked, { picked = if (item.id in picked) picked - item.id else picked + item.id })
            }
        }
        FeastButton(stringResource(Res.string.done), onClick = { close { onDone(start, end, picked) } }, modifier = Modifier.padding(top = 16.dp).fillMaxWidth())
    }
}
