package com.healthmate.app.features.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmate.app.data.model.DailyGoals
import com.healthmate.app.data.repository.GoalsRepository
import com.healthmate.app.utils.RecordValidator
import kotlinx.coroutines.launch

/** Text in the three goal fields plus their error messages. */
data class GoalsFormState(
    val steps: String = "",
    val calories: String = "",
    val water: String = "",
    val stepsError: String? = null,
    val caloriesError: String? = null,
    val waterError: String? = null
)

class SettingsViewModel(private val goalsRepository: GoalsRepository) : ViewModel() {

    var formState by mutableStateOf(goalsRepository.goals.value.toFormState())
        private set

    fun onStepsChange(value: String) {
        formState = formState.copy(steps = value, stepsError = null)
    }

    fun onCaloriesChange(value: String) {
        formState = formState.copy(calories = value, caloriesError = null)
    }

    fun onWaterChange(value: String) {
        formState = formState.copy(water = value, waterError = null)
    }

    /** Fills the form with the default goals (the user still has to tap Save). */
    fun resetToDefaults() {
        formState = DailyGoals().toFormState()
    }

    /** Validates the goals and, if valid, saves them and calls [onSaved]. */
    fun saveGoals(onSaved: () -> Unit) {
        val stepsError = RecordValidator.validateGoal(formState.steps, "Steps", RecordValidator.MAX_STEPS)
        val caloriesError = RecordValidator.validateGoal(formState.calories, "Calories", RecordValidator.MAX_CALORIES)
        val waterError = RecordValidator.validateGoal(formState.water, "Water", RecordValidator.MAX_WATER)

        if (stepsError != null || caloriesError != null || waterError != null) {
            formState = formState.copy(
                stepsError = stepsError,
                caloriesError = caloriesError,
                waterError = waterError
            )
            return
        }

        val goals = DailyGoals(
            steps = formState.steps.trim().toInt(),
            calories = formState.calories.trim().toInt(),
            water = formState.water.trim().toInt()
        )
        viewModelScope.launch {
            goalsRepository.saveGoals(goals)
            onSaved()
        }
    }
}

private fun DailyGoals.toFormState() = GoalsFormState(
    steps = steps.toString(),
    calories = calories.toString(),
    water = water.toString()
)
