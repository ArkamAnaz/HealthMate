package com.healthmate.app.utils

import java.time.LocalDate

/** Holds one error message per form field (null = field is valid). */
data class ValidationResult(
    val dateError: String? = null,
    val stepsError: String? = null,
    val caloriesError: String? = null,
    val waterError: String? = null
) {
    val isValid: Boolean
        get() = dateError == null && stepsError == null &&
                caloriesError == null && waterError == null
}

/** Form validation rules for the Add / Edit Record screen. */
object RecordValidator {

    const val MAX_STEPS = 100_000
    const val MAX_CALORIES = 10_000
    const val MAX_WATER = 10_000

    fun validate(date: String, steps: String, calories: String, water: String) = ValidationResult(
        dateError = validateDate(date),
        stepsError = validateNumber(steps, "Steps", MAX_STEPS),
        caloriesError = validateNumber(calories, "Calories", MAX_CALORIES),
        waterError = validateNumber(water, "Water intake", MAX_WATER)
    )

    fun validateDate(date: String): String? {
        if (date.isBlank()) return "Please select a date"
        val parsed = DateUtils.parse(date) ?: return "Invalid date (use yyyy-MM-dd)"
        if (parsed.isAfter(LocalDate.now())) return "Date cannot be in the future"
        return null
    }

    fun validateNumber(value: String, fieldName: String, max: Int): String? {
        val text = value.trim()
        if (text.isEmpty()) return "$fieldName is required"
        val number = text.toIntOrNull() ?: return "$fieldName must be a whole number"
        if (number < 0) return "$fieldName cannot be negative"
        if (number > max) return "$fieldName cannot be more than ${formatNumber(max)}"
        return null
    }

    /** Goals must be whole numbers greater than 0 and not above the maximum. */
    fun validateGoal(value: String, fieldName: String, max: Int): String? {
        validateNumber(value, fieldName, max)?.let { return it }
        if (value.trim().toInt() == 0) return "$fieldName goal must be greater than 0"
        return null
    }
}
