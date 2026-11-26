package com.healthmate.app.data.repository

import com.healthmate.app.data.local.GoalsPreferences
import com.healthmate.app.data.model.DailyGoals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Single source of truth for the daily goals. The dashboard observes [goals]. */
class GoalsRepository(private val preferences: GoalsPreferences) {

    private val _goals = MutableStateFlow(preferences.load())
    val goals: StateFlow<DailyGoals> = _goals.asStateFlow()

    suspend fun saveGoals(goals: DailyGoals) {
        withContext(Dispatchers.IO) { preferences.save(goals) }
        _goals.value = goals
    }
}
