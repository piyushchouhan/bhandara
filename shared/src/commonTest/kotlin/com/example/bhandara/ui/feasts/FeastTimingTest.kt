package com.example.bhandara.ui.feasts

import com.example.bhandara.data.models.api.FeastResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/** When a feast is, as the map card and the feast page say it */
class FeastTimingTest {

    private fun feast(date: String = "2026-10-04", start: String = "18:00:00", end: String = "22:00:00") = FeastResponse(
        id = "1", menuItems = listOf("Puri"), feastDate = date, startTime = start, endTime = end,
        latitude = 18.58, longitude = 73.88, estimatedCapacity = 100,
    )

    private fun at(date: String, time: String) = LocalDateTime(LocalDate.parse(date), LocalTime.parse(time))

    @Test
    fun aFeastOnRightNowSaysUntilWhen() {
        assertEquals(FeastTiming.Now(LocalTime(22, 0)), feastTiming(feast(), at("2026-10-04", "19:30")))
        assertEquals(FeastTiming.Now(LocalTime(22, 0)), feastTiming(feast(), at("2026-10-04", "18:00")))
        assertEquals(FeastTiming.Now(LocalTime(22, 0)), feastTiming(feast(), at("2026-10-04", "22:00")))
    }

    @Test
    fun aFeastLaterTodayTomorrowOrAfterSaysWhenItStarts() {
        assertEquals(FeastTiming.Today(LocalTime(18, 0)), feastTiming(feast(), at("2026-10-04", "09:00")))
        assertEquals(FeastTiming.Tomorrow(LocalTime(18, 0)), feastTiming(feast(), at("2026-10-03", "21:00")))
        assertEquals(
            FeastTiming.Later(LocalDate(2026, 10, 4), LocalTime(18, 0)),
            feastTiming(feast(), at("2026-10-01", "12:00")),
        )
    }

    @Test
    fun aFeastThatAlreadyEndedSaysSo() {
        // The list can be older than the feast's end (the app stayed open), so the app checks too
        assertEquals(FeastTiming.Ended, feastTiming(feast(), at("2026-10-04", "22:01")))
        assertEquals(FeastTiming.Ended, feastTiming(feast(), at("2026-10-05", "08:00")))
    }

    @Test
    fun aFeastPastMidnightEndsTheNextDay() {
        val overnight = feast(start = "20:00:00", end = "01:00:00")

        assertEquals(FeastTiming.Now(LocalTime(1, 0)), feastTiming(overnight, at("2026-10-04", "23:30")))
        assertEquals(FeastTiming.Now(LocalTime(1, 0)), feastTiming(overnight, at("2026-10-05", "00:30")))
        assertEquals(FeastTiming.Ended, feastTiming(overnight, at("2026-10-05", "01:30")))
    }

    @Test
    fun unreadableDatesOrTimesAreUnknown() {
        assertEquals(FeastTiming.Unknown, feastTiming(feast(date = "someday"), at("2026-10-04", "12:00")))
        assertEquals(FeastTiming.Unknown, feastTiming(feast(start = "evening"), at("2026-10-04", "12:00")))
    }

    @Test
    fun timesDropMinutesOnTheHour() {
        assertEquals("6 PM", formatFeastTime(LocalTime(18, 0)))
        assertEquals("6:30 PM", formatFeastTime(LocalTime(18, 30)))
        assertEquals("12 PM", formatFeastTime(LocalTime(12, 0)))
        assertEquals("12:05 AM", formatFeastTime(LocalTime(0, 5)))
    }

    @Test
    fun theFeastPageShowsItsDayAndHours() {
        assertEquals("Sun 4 Oct 2026", formatFeastDate(feast()))
        assertEquals("6 PM – 10 PM", formatFeastHours(feast()))
        assertEquals("8:30 PM – 1 AM", formatFeastHours(feast(start = "20:30", end = "01:00:00")))
        assertEquals("Sat 3 Oct", formatFeastDay(LocalDate(2026, 10, 3)))
    }
}
