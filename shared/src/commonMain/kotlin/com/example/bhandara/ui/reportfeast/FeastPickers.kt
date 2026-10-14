package com.example.bhandara.ui.reportfeast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.ok
import com.example.bhandara.shared.resources.select_date
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.feasts.localNow
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

// The post-a-feast pickers (designs 1g / 1h), as bottom sheets

private val MONTHS = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

/** The days to show for [month]'s grid, Sunday first: nulls before the 1st */
internal fun calendarDays(month: LocalDate): List<LocalDate?> {
    val first = LocalDate(month.year, month.monthNumber, 1)
    val next = first.plus(DatePeriod(months = 1))
    val lead = first.dayOfWeek.ordinal.let { (it + 1) % 7 } // Monday = 0 in kotlinx, Sunday first here
    val days = generateSequence(first) { it.plus(DatePeriod(days = 1)) }.takeWhile { it < next }.toList()
    return List(lead) { null } + days
}

/** A white sheet with a handle, Cancel and OK at the bottom */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerSheet(onDismiss: () -> Unit, onOk: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.feastColors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val close: (() -> Unit) -> Unit = { then -> scope.launch { state.hide() }.invokeOnCompletion { onDismiss(); then() } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        containerColor = colors.surface,
        contentColor = colors.ink,
        scrimColor = colors.sheetScrim,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 38.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp))) },
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
            Row(Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeastButton(stringResource(Res.string.cancel), onClick = { close {} }, style = FeastButtonStyle.SOFT, modifier = Modifier.weight(1f))
                FeastButton(stringResource(Res.string.ok), onClick = { close(onOk) }, style = FeastButtonStyle.SAFFRON, modifier = Modifier.weight(1.3f))
            }
        }
    }
}

/** Date as a calendar sheet: past days fade, today is ringed, the chosen day is filled indigo */
@Composable
internal fun FeastDateSheet(current: LocalDate?, onPicked: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val today = remember { localNow().date }
    var chosen by remember { mutableStateOf(current ?: today) }
    var month by remember { mutableStateOf(LocalDate(chosen.year, chosen.monthNumber, 1)) }
    val firstMonth = LocalDate(today.year, today.monthNumber, 1)

    PickerSheet(onDismiss = onDismiss, onOk = { onPicked(chosen) }) {
        Text(stringResource(Res.string.select_date), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
        Text(
            "${MONTHS[chosen.monthNumber - 1].take(3)} ${chosen.dayOfMonth}, ${chosen.year}",
            color = colors.ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall,
        )
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${MONTHS[month.monthNumber - 1]} ${month.year}", color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            val canGoBack = month > firstMonth
            MonthButton(Icons.Rounded.ChevronLeft, enabled = canGoBack) { month = month.minus(DatePeriod(months = 1)) }
            Spacer(Modifier.width(4.dp))
            MonthButton(Icons.Rounded.ChevronRight, enabled = true) { month = month.plus(DatePeriod(months = 1)) }
        }
        Row {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).height(32.dp).padding(top = 6.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelLarge)
            }
        }
        calendarDays(month).chunked(7).forEach { week ->
            Row {
                week.forEach { day -> DayCell(day, chosen, today, Modifier.weight(1f)) { chosen = it } }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MonthButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.ink.copy(alpha = if (enabled) 1f else 0.35f), modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun DayCell(day: LocalDate?, chosen: LocalDate, today: LocalDate, modifier: Modifier, onClick: (LocalDate) -> Unit) {
    val colors = MaterialTheme.feastColors
    Box(modifier.height(44.dp), contentAlignment = Alignment.Center) {
        if (day == null) return@Box
        val past = day < today
        val selected = day == chosen
        val shape = RoundedCornerShape(14.dp)
        Box(
            Modifier
                .size(40.dp)
                .clip(shape)
                .then(if (selected) Modifier.background(colors.indigo) else Modifier)
                .then(if (day == today && !selected) Modifier.border(1.5.dp, colors.selectedOutline, shape) else Modifier)
                .clickable(enabled = !past) { onClick(day) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${day.dayOfMonth}",
                color = when {
                    selected -> colors.onIndigo
                    past -> colors.ink.copy(alpha = 0.25f)
                    else -> colors.ink
                },
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/** Time as one-tap choices: an hour grid with AM / PM, and the minutes as four chips */
@Composable
internal fun FeastTimeSheet(initial: LocalTime, onPicked: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    var hour12 by remember { mutableStateOf(if (initial.hour % 12 == 0) 12 else initial.hour % 12) }
    var pm by remember { mutableStateOf(initial.hour >= 12) }
    var minute by remember { mutableStateOf(initial.minute) }
    val time = { LocalTime((hour12 % 12) + if (pm) 12 else 0, minute) }

    PickerSheet(onDismiss = onDismiss, onOk = { onPicked(time()) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigNumber("$hour12", colors.indigo, colors.onIndigo, Modifier.weight(1f))
            Text(":", color = colors.ink, fontSize = 36.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.displaySmall)
            BigNumber(minute.toString().padStart(2, '0'), colors.lavender, colors.ink, Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HalfChip("AM", selected = !pm) { pm = false }
                HalfChip("PM", selected = pm) { pm = true }
            }
        }
        (1..12).chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { h ->
                    val on = h == hour12
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp)).background(if (on) colors.indigo else colors.lavenderSoft).clickable { hour12 = h },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$h", color = if (on) colors.onIndigo else colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 15, 30, 45).forEach { m ->
                val on = m == minute
                val shape = RoundedCornerShape(12.dp)
                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(shape)
                        .background(if (on) colors.saffronSoft else colors.lavenderSoft)
                        .then(if (on) Modifier.border(1.5.dp, colors.saffron, shape) else Modifier)
                        .clickable { minute = m },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(":${m.toString().padStart(2, '0')}", color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun BigNumber(text: String, fill: androidx.compose.ui.graphics.Color, ink: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Box(modifier.height(76.dp).background(fill, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
        Text(text, color = ink, fontSize = 46.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.displayMedium)
    }
}

@Composable
private fun HalfChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Box(
        Modifier.size(width = 56.dp, height = 36.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) colors.saffron else colors.lavender).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) colors.onSaffron else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}
