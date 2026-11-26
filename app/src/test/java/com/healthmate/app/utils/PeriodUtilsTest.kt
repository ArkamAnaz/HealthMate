package com.healthmate.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Unit tests for the Day / Week / Month date ranges used by the dashboard. */
class PeriodUtilsTest {

    private val today = LocalDate.of(2026, 11, 19)

    @Test
    fun day_isOnlyToday() {
        val range = PeriodUtils.rangeFor(SummaryPeriod.DAY, today)
        assertEquals(today, range.start)
        assertEquals(today, range.end)
        assertEquals(1, range.days)
    }

    @Test
    fun week_isLastSevenDaysIncludingToday() {
        val range = PeriodUtils.rangeFor(SummaryPeriod.WEEK, today)
        assertEquals(LocalDate.of(2026, 11, 13), range.start)
        assertEquals(7, range.days)
        assertEquals(7, range.dates().size)
    }

    @Test
    fun month_startsOnTheFirst() {
        val range = PeriodUtils.rangeFor(SummaryPeriod.MONTH, today)
        assertEquals(LocalDate.of(2026, 11, 1), range.start)
        assertEquals(19, range.days)
    }

    @Test
    fun rangeText_formatsWeek() {
        val range = PeriodUtils.rangeFor(SummaryPeriod.WEEK, today)
        assertEquals("13 Nov – 19 Nov 2026", DateUtils.rangeText(range))
    }
}
