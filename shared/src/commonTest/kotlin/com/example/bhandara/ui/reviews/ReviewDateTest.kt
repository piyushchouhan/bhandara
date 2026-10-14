package com.example.bhandara.ui.reviews

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** When a review was written, as shown under it */
class ReviewDateTest {

    @Test
    fun theBackendsTimestampIsShownAsADay() {
        assertEquals("3 Oct 2026", formatReviewDate("2026-10-03T16:42:24"))
        assertEquals("15 Jan 2025", formatReviewDate("2025-01-15T09:00:00.123456"))
        assertEquals("31 Dec 2024", formatReviewDate("2024-12-31"))
    }

    @Test
    fun nothingIsShownForAMissingOrUnreadableDate() {
        assertNull(formatReviewDate(null))
        assertNull(formatReviewDate(""))
        assertNull(formatReviewDate("yesterday"))
        assertNull(formatReviewDate("2026-13-01T00:00:00"))
    }
}
