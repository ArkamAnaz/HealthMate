package com.healthmate.app.data.local

import android.content.Context
import com.healthmate.app.data.model.DailyGoals

/**
 * Saves the daily goals with Android SharedPreferences.
 * Three small numbers do not need a database table; SharedPreferences is the
 * standard Android way to store simple key-value settings.
 */
class GoalsPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): DailyGoals = DailyGoals(
        steps = prefs.getInt(KEY_STEPS, DailyGoals.DEFAULT_STEPS),
        calories = prefs.getInt(KEY_CALORIES, DailyGoals.DEFAULT_CALORIES),
        water = prefs.getInt(KEY_WATER, DailyGoals.DEFAULT_WATER)
    )

    fun save(goals: DailyGoals) {
        prefs.edit()
            .putInt(KEY_STEPS, goals.steps)
            .putInt(KEY_CALORIES, goals.calories)
            .putInt(KEY_WATER, goals.water)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "healthmate_settings"
        private const val KEY_STEPS = "goal_steps"
        private const val KEY_CALORIES = "goal_calories"
        private const val KEY_WATER = "goal_water"
    }
}
