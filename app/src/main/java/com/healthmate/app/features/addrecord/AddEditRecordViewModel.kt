package com.healthmate.app.features.addrecord

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmate.app.data.model.HealthRecord
import com.healthmate.app.data.repository.HealthRepository
import com.healthmate.app.navigation.Routes
import com.healthmate.app.utils.DateUtils
import com.healthmate.app.utils.RecordValidator
import kotlinx.coroutines.launch

/** Everything the Add / Edit form needs to draw itself. */
data class RecordFormState(
    val date: String = DateUtils.today(),
    val steps: String = "",
    val calories: String = "",
    val water: String = "",
    val dateError: String? = null,
    val stepsError: String? = null,
    val caloriesError: String? = null,
    val waterError: String? = null,
    val isSaving: Boolean = false
)

class AddEditRecordViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HealthRepository
) : ViewModel() {

    private val recordId: Long = savedStateHandle.get<Long>(Routes.ARG_RECORD_ID) ?: -1L

    /** true = editing an existing record, false = adding a new one */
    val isEditMode: Boolean = recordId != -1L

    var formState by mutableStateOf(RecordFormState())
        private set

    init {
        if (isEditMode) {
            viewModelScope.launch {
                repository.getRecordById(recordId)?.let { record ->
                    formState = RecordFormState(
                        date = record.date,
                        steps = record.steps.toString(),
                        calories = record.calories.toString(),
                        water = record.water.toString()
                    )
                }
            }
        }
    }

    // Each change clears that field's error so the user sees feedback immediately.
    fun onDateChange(value: String) {
        formState = formState.copy(date = value, dateError = null)
    }

    fun onStepsChange(value: String) {
        formState = formState.copy(steps = value, stepsError = null)
    }

    fun onCaloriesChange(value: String) {
        formState = formState.copy(calories = value, caloriesError = null)
    }

    fun onWaterChange(value: String) {
        formState = formState.copy(water = value, waterError = null)
    }

    /** Validates the form. If valid, inserts or updates the record and calls [onSaved]. */
    fun saveRecord(onSaved: () -> Unit) {
        if (formState.isSaving) return

        val result = RecordValidator.validate(
            formState.date, formState.steps, formState.calories, formState.water
        )
        if (!result.isValid) {
            formState = formState.copy(
                dateError = result.dateError,
                stepsError = result.stepsError,
                caloriesError = result.caloriesError,
                waterError = result.waterError
            )
            return
        }

        formState = formState.copy(isSaving = true)
        val record = HealthRecord(
            id = if (isEditMode) recordId else 0,
            date = formState.date,
            steps = formState.steps.trim().toInt(),
            calories = formState.calories.trim().toInt(),
            water = formState.water.trim().toInt()
        )
        viewModelScope.launch {
            if (isEditMode) repository.updateRecord(record) else repository.addRecord(record)
            onSaved()
        }
    }

    fun deleteRecord(onDeleted: () -> Unit) {
        if (!isEditMode) return
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            onDeleted()
        }
    }
}
