package com.example.bhandara.ui.reportfeast

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The date sheet's calendar grid, Sunday first */
class FeastPickersTest {

    @Test
    fun theFirstOfTheMonthLandsUnderItsWeekday() {
        // 1 Oct 2026 is a Thursday: four blanks (Sun–Wed) before it
        val october = calendarDays(LocalDate(2026, 10, 6))
        assertEquals(4, october.takeWhile { it == null }.size)
        assertEquals(LocalDate(2026, 10, 1), october[4])
        assertEquals(LocalDate(2026, 10, 31), october.last())
        assertEquals(4 + 31, october.size)
    }

    @Test
    fun aMonthStartingOnSundayHasNoBlanks() {
        // 1 Nov 2026 is a Sunday
        val november = calendarDays(LocalDate(2026, 11, 20))
        assertEquals(LocalDate(2026, 11, 1), november.first())
        assertEquals(30, november.size)
    }

    @Test
    fun februaryInALeapYearHas29Days() {
        val february = calendarDays(LocalDate(2028, 2, 1))
        assertEquals(LocalDate(2028, 2, 29), february.last())
        assertNull(february.first(), "1 Feb 2028 is a Tuesday")
    }
}
