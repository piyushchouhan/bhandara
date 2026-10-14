package com.example.bhandara.ui.feasts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.duration_hours_minutes
import com.example.bhandara.shared.resources.duration_minutes
import com.example.bhandara.shared.resources.feast_ended
import com.example.bhandara.shared.resources.feast_ends_in
import com.example.bhandara.shared.resources.feast_happening_now
import com.example.bhandara.shared.resources.feast_starts_in
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.sqrt

// Pieces shared by the redesigned feast screens (use inside FeastTheme)

/** The phone's time, refreshed every half minute so countdowns and "happening now" stay current */
@Composable
internal fun rememberNow(): State<LocalDateTime> = produceState(localNow()) {
    while (true) {
        delay(30_000)
        value = localNow()
    }
}

/** Diagonal stripes in two colours: the tile shown where a feast has no photo */
internal fun Modifier.stripes(a: Color, b: Color, stripe: Dp): Modifier = clipToBounds().drawBehind {
    drawRect(a)
    val w = stripe.toPx()
    val diagonal = size.width + size.height
    var x = -size.height
    while (x < diagonal) {
        // A band 45° to the right, as the design's 135° gradient
        drawLine(b, Offset(x + w * 1.5f, 0f), Offset(x + w * 1.5f - size.height, size.height), strokeWidth = w * sqrt(2f))
        x += w * 2
    }
}

private val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/** The ticket's date stub: month, day and weekday on indigo (greyed for a taken-down feast) */
@Composable
internal fun DateStub(feast: FeastResponse, width: Dp = 66.dp, faded: Boolean = false, showWeekday: Boolean = true) {
    val colors = MaterialTheme.feastColors
    val date = runCatching { LocalDate.parse(feast.feastDate) }.getOrNull() ?: return
    val small = width < 60.dp
    Column(
        Modifier
            .width(width)
            .background(if (faded) (if (colors.isDark) colors.border else colors.muted) else colors.indigo, RoundedCornerShape(if (small) 14.dp else 16.dp))
            .padding(vertical = if (small) 6.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            MONTHS[date.monthNumber - 1], color = if (faded) Color.White else colors.saffron, fontSize = if (small) 10.sp else 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, style = MaterialTheme.typography.labelSmall,
        )
        Text(
            "${date.dayOfMonth}", color = Color.White, fontSize = if (small) 22.sp else 28.sp, lineHeight = if (small) 22.sp else 29.sp,
            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall,
        )
        if (showWeekday) {
            Text(
                DAYS[date.dayOfWeek.ordinal], color = if (faded) Color.White else colors.headerSubtext, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/** "2 h 40 min" or "40 min" */
@Composable
internal fun durationText(minutes: Long): String {
    val hours = minutes / 60
    val rest = (minutes % 60).toInt()
    return if (hours > 0) stringResource(Res.string.duration_hours_minutes, hours.toInt(), rest) else stringResource(Res.string.duration_minutes, rest)
}

/** "Ends in 2 h 40 min" while it's on, "Starts in 1 h 20 min" before; null when neither applies */
@Composable
internal fun countdownText(feast: FeastResponse, now: LocalDateTime): String? {
    val timing = feastTiming(feast, now)
    return when {
        timing is FeastTiming.Now -> minutesUntilEnd(feast, now)?.let { stringResource(Res.string.feast_ends_in, durationText(it)) }
        timing is FeastTiming.Today -> minutesUntilStart(feast, now)?.let { stringResource(Res.string.feast_starts_in, durationText(it)) }
        else -> null
    }
}

/** A rounded status chip: green "Happening now", saffron when it starts, muted "Ended" */
@Composable
internal fun FeastStatusChip(timing: FeastTiming) {
    val colors = MaterialTheme.feastColors
    val (text, fill, ink) = when (timing) {
        is FeastTiming.Now -> Triple(stringResource(Res.string.feast_happening_now), colors.successSoft, colors.success)
        FeastTiming.Ended -> Triple(stringResource(Res.string.feast_ended), colors.lavender, colors.muted)
        else -> Triple(timing.label().first, colors.saffronSoft, colors.saffronText)
    }
    Row(
        Modifier.height(26.dp).background(fill, CircleShape).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).background(ink, CircleShape))
        Text(text, color = ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.labelMedium)
    }
}
