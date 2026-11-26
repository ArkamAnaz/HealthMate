package com.healthmate.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/** Date helpers. Dates are stored in the database as "yyyy-MM-dd" text. */
object DateUtils {

    private val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
    private val shortDisplayFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH)
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)
    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

    fun today(): String = LocalDate.now().format(dbFormatter)

    fun format(date: LocalDate): String = date.format(dbFormatter)

    fun parse(text: String): LocalDate? = try {
        LocalDate.parse(text, dbFormatter)
    } catch (e: DateTimeParseException) {
        null
    }

    /** "2026-08-18" -> "18 August 2026" */
    fun toDisplay(text: String): String = parse(text)?.format(displayFormatter) ?: text

    /** "2026-08-18" -> "Tue, 18 Aug 2026" */
    fun toShortDisplay(text: String): String = parse(text)?.format(shortDisplayFormatter) ?: text

    /** "2026-11-18" -> "18 Nov 2026" */
    fun toMediumDisplay(text: String): String = parse(text)?.format(dayMonthYearFormatter) ?: text

    /** LocalDate -> "Mon" (used under the weekly bars) */
    fun weekday(date: LocalDate): String = date.format(weekdayFormatter)

    /** "13 Nov – 19 Nov 2026" (or just "19 November 2026" for a single day) */
    fun rangeText(range: DateRange): String =
        if (range.start == range.end) range.start.format(displayFormatter)
        else "${range.start.format(dayMonthFormatter)} – ${range.end.format(dayMonthYearFormatter)}"
}
