package com.healthmate.app.data.model

/**
 * One row of the "health_records" table.
 *
 * @param id       Primary key (0 means "not saved yet")
 * @param date     Date of the entry in yyyy-MM-dd format (stored as TEXT)
 * @param steps    Steps walked
 * @param calories Calories burned (kcal)
 * @param water    Water intake (ml)
 */
data class HealthRecord(
    val id: Long = 0,
    val date: String,
    val steps: Int,
    val calories: Int,
    val water: Int
)

/** Totals for a period (one day, a week or a month), shown on the dashboard. */
data class DailySummary(
    val totalSteps: Int = 0,
    val totalCalories: Int = 0,
    val totalWater: Int = 0,
    val entryCount: Int = 0
)

/** Totals for a single date, used by the daily bar chart. */
data class DailyTotal(
    val date: String,
    val steps: Int = 0,
    val calories: Int = 0,
    val water: Int = 0
)

/** The user's daily goals, saved on the Settings page. */
data class DailyGoals(
    val steps: Int = DEFAULT_STEPS,
    val calories: Int = DEFAULT_CALORIES,
    val water: Int = DEFAULT_WATER
) {
    companion object {
        const val DEFAULT_STEPS = 10_000
        const val DEFAULT_CALORIES = 500
        const val DEFAULT_WATER = 2_000
    }
}
