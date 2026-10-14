package com.example.bhandara.ui.shopdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.data.models.api.CartDetails
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.cart_about
import com.example.bhandara.shared.resources.cart_look_for
import com.example.bhandara.shared.resources.cart_no_route_note
import com.example.bhandara.shared.resources.cart_takes_orders
import com.example.bhandara.shared.resources.cart_usual_stops
import com.example.bhandara.shared.resources.daily_delivery
import com.example.bhandara.shared.resources.day_letters
import com.example.bhandara.shared.resources.days_out
import com.example.bhandara.shared.resources.delivers_homes
import com.example.bhandara.shared.resources.jain_option
import com.example.bhandara.shared.resources.monthly_bill
import com.example.bhandara.shared.resources.plays_tune
import com.example.bhandara.shared.resources.pure_veg
import com.example.bhandara.shared.resources.seasonal
import com.example.bhandara.shared.resources.standing_orders
import com.example.bhandara.shared.resources.times_vary_short
import com.example.bhandara.ui.addshop.OPTION_DELIVERS_HOMES
import com.example.bhandara.ui.addshop.OPTION_HOME_DELIVERY
import com.example.bhandara.ui.addshop.OPTION_JAIN
import com.example.bhandara.ui.addshop.OPTION_MONTHLY_BILL
import com.example.bhandara.ui.addshop.OPTION_PLAYS_TUNE
import com.example.bhandara.ui.addshop.OPTION_PURE_VEG
import com.example.bhandara.ui.addshop.OPTION_STANDING_ORDERS
import com.example.bhandara.ui.addshop.WEEKDAYS
import com.example.bhandara.ui.addshop.cartSellsLooks
import com.example.bhandara.ui.addshop.cartTypeLooks
import com.example.bhandara.ui.addshop.paymentLabels
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// A moving cart's own details on its page: what it's on and sells, its usual stops and days, how to pay and how to
// spot it. In the page's card and chip style (the design has no cart variant of the shop page).

private val optionLooks: Map<String, Pair<ImageVector, StringResource>> = mapOf(
    OPTION_HOME_DELIVERY to (Icons.Outlined.Home to Res.string.daily_delivery),
    OPTION_DELIVERS_HOMES to (Icons.Outlined.Home to Res.string.delivers_homes),
    OPTION_STANDING_ORDERS to (Icons.Outlined.Repeat to Res.string.standing_orders),
    OPTION_MONTHLY_BILL to (Icons.Outlined.ReceiptLong to Res.string.monthly_bill),
    OPTION_PURE_VEG to (Icons.Outlined.Eco to Res.string.pure_veg),
    OPTION_JAIN to (Icons.Outlined.Restaurant to Res.string.jain_option),
    OPTION_PLAYS_TUNE to (Icons.Outlined.MusicNote to Res.string.plays_tune),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CartDetailsCard(cart: CartDetails) {
    val colors = MaterialTheme.feastColors
    PageCard {
        CardTitle(stringResource(Res.string.cart_about))
        val chips = buildList {
            cart.cartType?.let(cartTypeLooks::get)?.let { add(it) }
            cart.sells.forEach { sold -> cartSellsLooks[sold]?.let { add(it) } }
        }
        if (chips.isNotEmpty() || cart.seasonal) {
            FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach { (icon, label) -> FacilityChip(icon, stringResource(label)) }
                if (cart.seasonal) FacilityChip(Icons.Rounded.WbSunny, stringResource(Res.string.seasonal))
            }
        }

        if (cart.noFixedRoute) {
            Note(Icons.Rounded.Place, stringResource(Res.string.cart_no_route_note))
        } else if (cart.stops.isNotEmpty()) {
            SubTitle(stringResource(Res.string.cart_usual_stops))
            Column(Modifier.padding(top = 6.dp)) {
                cart.stops.forEachIndexed { index, stop ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(24.dp).background(colors.indigo, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = colors.onIndigo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(stop.name, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            stopTimes(stop.from, stop.to, stop.timesVary) ?: stringResource(Res.string.times_vary_short),
                            color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        if (cart.daysOut.isNotEmpty()) {
            SubTitle(stringResource(Res.string.days_out))
            val letters = stringResource(Res.string.day_letters).split(',')
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WEEKDAYS.forEachIndexed { index, day ->
                    val out = day in cart.daysOut
                    Box(
                        Modifier.size(32.dp).background(if (out) colors.indigo else colors.surface, CircleShape)
                            .then(if (out) Modifier else Modifier.border(1.5.dp, colors.border, CircleShape)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            letters.getOrElse(index) { day.take(1) }, color = if (out) colors.onIndigo else colors.muted,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }

        val payments = cart.payments.mapNotNull(paymentLabels::get)
        val options = cartOptionsOn(cart.options).mapNotNull(optionLooks::get)
        if (payments.isNotEmpty() || cart.takesOrders || options.isNotEmpty()) {
            FlowRow(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (payments.isNotEmpty()) FacilityChip(Icons.Rounded.Payments, payments.map { stringResource(it) }.joinToString(" · "))
                if (cart.takesOrders) FacilityChip(Icons.Rounded.Call, stringResource(Res.string.cart_takes_orders))
                options.forEach { (icon, label) -> FacilityChip(icon, stringResource(label)) }
            }
        }

        cart.spotHint?.takeIf { it.isNotBlank() }?.let { Note(Icons.Rounded.Visibility, stringResource(Res.string.cart_look_for, it.trim())) }
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text, color = MaterialTheme.feastColors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 14.dp),
    )
}

@Composable
private fun Note(icon: ImageVector, text: String) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp).background(colors.lavenderSoft, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
        Text(text, color = colors.ink, fontSize = 14.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodyMedium)
    }
}
