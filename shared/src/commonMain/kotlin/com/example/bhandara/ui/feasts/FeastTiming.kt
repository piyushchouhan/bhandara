package com.example.bhandara.ui.feasts

import com.example.bhandara.data.models.api.FeastResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** When a feast is, compared with now */
sealed interface FeastTiming {
    /** On right now, until [endsAt] */
    data class Now(val endsAt: LocalTime) : FeastTiming

    data class Today(val startsAt: LocalTime) : FeastTiming

    data class Tomorrow(val startsAt: LocalTime) : FeastTiming

    data class Later(val date: LocalDate, val startsAt: LocalTime) : FeastTiming

    data object Ended : FeastTiming

    /** The backend sent a date or time the app can't read */
    data object Unknown : FeastTiming
}

/** The phone's current local date and time */
fun localNow(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/** A feast that ends before it starts runs past midnight, e.g. 8 PM to 1 AM ends the next day */
fun feastTiming(feast: FeastResponse, now: LocalDateTime): FeastTiming {
    val date = runCatching { LocalDate.parse(feast.feastDate) }.getOrNull() ?: return FeastTiming.Unknown
    val startTime = parseTime(feast.startTime) ?: return FeastTiming.Unknown
    val endTime = parseTime(feast.endTime) ?: return FeastTiming.Unknown
    val start = date.atTime(startTime)
    val end = (if (endTime < startTime) date.plus(DatePeriod(days = 1)) else date).atTime(endTime)
    return when {
        now > end -> FeastTiming.Ended
        now >= start -> FeastTiming.Now(endTime)
        date == now.date -> FeastTiming.Today(startTime)
        date == now.date.plus(DatePeriod(days = 1)) -> FeastTiming.Tomorrow(startTime)
        else -> FeastTiming.Later(date, startTime)
    }
}

/** "09:30:00" or "09:30" */
private fun parseTime(text: String): LocalTime? {
    val parts = text.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.takeIf { it in 0..23 } ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it in 0..59 } ?: return null
    val second = parts.getOrNull(2)?.substringBefore('.')?.toIntOrNull()?.takeIf { it in 0..59 } ?: 0
    return LocalTime(hour, minute, second)
}

/** "6 PM", or "6:30 PM" when it isn't on the hour */
fun formatFeastTime(time: LocalTime): String {
    val hour12 = if (time.hour % 12 == 0) 12 else time.hour % 12
    val half = if (time.hour < 12) "AM" else "PM"
    return if (time.minute == 0) "$hour12 $half" else "$hour12:${time.minute.toString().padStart(2, '0')} $half"
}

private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "Sat 5 Oct" */
fun formatFeastDay(date: LocalDate): String =
    "${DAYS[date.dayOfWeek.ordinal]} ${date.dayOfMonth} ${MONTHS[date.monthNumber - 1]}"

/** "Sat 5 Oct 2026" for the feast's date, or the date as it came if unreadable */
fun formatFeastDate(feast: FeastResponse): String =
    runCatching { LocalDate.parse(feast.feastDate) }.getOrNull()?.let { "${formatFeastDay(it)} ${it.year}" } ?: feast.feastDate

/** "6 PM – 10 PM", using the times as they came if unreadable */
fun formatFeastHours(feast: FeastResponse): String {
    val start = parseTime(feast.startTime)?.let(::formatFeastTime) ?: feast.startTime
    val end = parseTime(feast.endTime)?.let(::formatFeastTime) ?: feast.endTime
    return "$start – $end"
}
