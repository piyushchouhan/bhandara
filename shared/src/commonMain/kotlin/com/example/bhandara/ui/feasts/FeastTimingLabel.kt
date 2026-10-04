package com.example.bhandara.ui.feasts

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.feast_ended
import com.example.bhandara.shared.resources.feast_later
import com.example.bhandara.shared.resources.feast_now_till
import com.example.bhandara.shared.resources.feast_today
import com.example.bhandara.shared.resources.feast_tomorrow
import com.example.bhandara.shared.resources.feast_upcoming
import org.jetbrains.compose.resources.stringResource

private val Live = Color(0xFF4CAF50)
private val Upcoming = Color(0xFFFB8C00)
private val Over = Color(0xFF9E9E9E)

/** "Now till 9 PM" (green) while it's on, when it starts (orange) otherwise, or "Ended" (grey) */
@Composable
internal fun FeastTiming.label(): Pair<String, Color> = when (this) {
    is FeastTiming.Now -> stringResource(Res.string.feast_now_till, formatFeastTime(endsAt)) to Live
    is FeastTiming.Today -> stringResource(Res.string.feast_today, formatFeastTime(startsAt)) to Upcoming
    is FeastTiming.Tomorrow -> stringResource(Res.string.feast_tomorrow, formatFeastTime(startsAt)) to Upcoming
    is FeastTiming.Later -> stringResource(Res.string.feast_later, formatFeastDay(date), formatFeastTime(startsAt)) to Upcoming
    FeastTiming.Ended -> stringResource(Res.string.feast_ended) to Over
    FeastTiming.Unknown -> stringResource(Res.string.feast_upcoming) to Upcoming
}
