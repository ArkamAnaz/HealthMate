package com.healthmate.app.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Periods the dashboard can show. */
enum class SummaryPeriod(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}

/** An inclusive range of dates, e.g. 13 Nov – 19 Nov. */
data class DateRange(val start: LocalDate, val end: LocalDate) {
    val days: Int
        get() = ChronoUnit.DAYS.between(start, end).toInt() + 1

    fun dates(): List<LocalDate> = (0 until days).map { start.plusDays(it.toLong()) }
}

object PeriodUtils {

    /**
     * DAY   = today only
     * WEEK  = the last 7 days including today
     * MONTH = from the 1st of the current month until today
     */
    fun rangeFor(period: SummaryPeriod, today: LocalDate = LocalDate.now()): DateRange =
        when (period) {
            SummaryPeriod.DAY -> DateRange(today, today)
            SummaryPeriod.WEEK -> DateRange(today.minusDays(6), today)
            SummaryPeriod.MONTH -> DateRange(today.withDayOfMonth(1), today)
        }
}
