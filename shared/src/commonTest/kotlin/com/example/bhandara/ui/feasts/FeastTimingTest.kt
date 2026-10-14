package com.example.bhandara.ui.feasts

import com.example.bhandara.data.models.api.FeastResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun theCountdownSaysHowLongUntilItEndsOrStarts() {
        assertEquals(160, minutesUntilEnd(feast(), at("2026-10-04", "19:20")))
        assertNull(minutesUntilStart(feast(), at("2026-10-04", "19:20")), "it has started")
        assertEquals(80, minutesUntilStart(feast(), at("2026-10-04", "16:40")))
        assertNull(minutesUntilEnd(feast(), at("2026-10-04", "22:01")), "it's over")
    }

    @Test
    fun aFeastPastMidnightCountsDownToTheNextDay() {
        val late = feast(start = "20:00:00", end = "01:00:00")
        assertEquals(90, minutesUntilEnd(late, at("2026-10-04", "23:30")))
        assertEquals(0.7f, feastProgress(late, at("2026-10-04", "23:30")))
    }

    @Test
    fun progressShowsHowMuchOfAFeastOnNowHasGoneBy() {
        assertEquals(0f, feastProgress(feast(), at("2026-10-04", "18:00")))
        assertEquals(0.5f, feastProgress(feast(), at("2026-10-04", "20:00")))
        assertEquals(1f, feastProgress(feast(), at("2026-10-04", "22:00")))
        assertNull(feastProgress(feast(), at("2026-10-04", "17:00")))
        assertNull(feastProgress(feast(start = "later"), at("2026-10-04", "19:00")))
    }

    @Test
    fun theListIsSplitIntoNowLaterTodayTomorrowAndComingUp() {
        fun at(id: String, date: String, start: String, end: String, distance: Double) =
            feast(date = date, start = start, end = end).copy(id = id, distance = distance)
        val state = FeastsMapState(
            feasts = listOf(
                at("far-now", "2026-10-04", "18:00:00", "22:00:00", 900.0),
                at("near-now", "2026-10-04", "17:00:00", "21:00:00", 100.0),
                at("eight", "2026-10-04", "20:00:00", "23:00:00", 50.0),
                at("seven", "2026-10-04", "19:30:00", "23:00:00", 500.0),
                at("tomorrow", "2026-10-05", "12:00:00", "14:00:00", 10.0),
                at("next-week", "2026-10-11", "12:00:00", "14:00:00", 10.0),
                at("over", "2026-10-04", "12:00:00", "14:00:00", 10.0),
            ),
            checkedAt = this.at("2026-10-04", "19:00"),
        )

        val sections = state.sections()

        assertEquals(listOf(FeastGroup.NOW, FeastGroup.LATER_TODAY, FeastGroup.TOMORROW, FeastGroup.COMING_UP), sections.map { it.group })
        assertEquals(listOf("near-now", "far-now"), sections[0].feasts.map { it.id }, "on now: nearest first")
        assertEquals(listOf("seven", "eight"), sections[1].feasts.map { it.id }, "later: soonest first")
        assertEquals(listOf("tomorrow"), sections[2].feasts.map { it.id })
        assertEquals(listOf("next-week"), sections[3].feasts.map { it.id })
    }
}
