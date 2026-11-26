package com.healthmate.app.ui.components

import android.app.DatePickerDialog
import android.content.Context
import com.healthmate.app.utils.DateUtils
import java.time.LocalDate
import java.time.ZoneId

/**
 * Opens the standard Android calendar dialog and returns the chosen date as "yyyy-MM-dd".
 * Future dates are always disabled. [minDate] / [maxDate] (yyyy-MM-dd) can limit the
 * range further, e.g. the "To" date of a range cannot be before the "From" date.
 */
fun showDatePicker(
    context: Context,
    initialDate: String?,
    minDate: String? = null,
    maxDate: String? = null,
    onDateSelected: (String) -> Unit
) {
    val start = initialDate?.let { DateUtils.parse(it) } ?: LocalDate.now()
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // DatePicker months are 0-based, LocalDate months are 1-based
            onDateSelected(DateUtils.format(LocalDate.of(year, month + 1, dayOfMonth)))
        },
        start.year,
        start.monthValue - 1,
        start.dayOfMonth
    )
    val todayMillis = System.currentTimeMillis()
    val maxMillis = maxDate?.let { DateUtils.parse(it) }?.let { toMillis(it) } ?: todayMillis
    dialog.datePicker.maxDate = minOf(maxMillis, todayMillis)
    minDate?.let { DateUtils.parse(it) }?.let { dialog.datePicker.minDate = toMillis(it) }
    dialog.show()
}

private fun toMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
