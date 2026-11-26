package com.healthmate.app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmate.app.data.model.DailyGoals
import com.healthmate.app.data.model.DailySummary
import com.healthmate.app.data.model.DailyTotal
import com.healthmate.app.data.repository.GoalsRepository
import com.healthmate.app.data.repository.HealthRepository
import com.healthmate.app.utils.DateRange
import com.healthmate.app.utils.PeriodUtils
import com.healthmate.app.utils.SummaryPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Which value the daily bar chart shows. */
enum class ChartMetric(val label: String) {
    STEPS("Steps"),
    CALORIES("Calories"),
    WATER("Water")
}

data class DashboardUiState(
    val period: SummaryPeriod = SummaryPeriod.DAY,
    val range: DateRange = PeriodUtils.rangeFor(SummaryPeriod.DAY),
    val summary: DailySummary = DailySummary(),
    val goals: DailyGoals = DailyGoals(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val chartMetric: ChartMetric = ChartMetric.STEPS
) {
    /** Goal for the whole period = daily goal × number of days. */
    val periodGoals: DailyGoals
        get() = DailyGoals(
            steps = goals.steps * range.days,
            calories = goals.calories * range.days,
            water = goals.water * range.days
        )
}

class DashboardViewModel(
    private val repository: HealthRepository,
    goalsRepository: GoalsRepository
) : ViewModel() {

    private val period = MutableStateFlow(SummaryPeriod.DAY)
    private val chartMetric = MutableStateFlow(ChartMetric.STEPS)

    /**
     * Re-calculated automatically when records change, goals change,
     * or the user picks another period / chart metric.
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.records, goalsRepository.goals, period, chartMetric
    ) { _, goals, selectedPeriod, metric ->
        val range = PeriodUtils.rangeFor(selectedPeriod)
        DashboardUiState(
            period = selectedPeriod,
            range = range,
            summary = repository.getSummary(range),
            goals = goals,
            dailyTotals = if (selectedPeriod == SummaryPeriod.DAY) emptyList()
            else repository.getDailyTotals(range),
            chartMetric = metric
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun selectPeriod(newPeriod: SummaryPeriod) {
        period.value = newPeriod
    }

    fun selectChartMetric(metric: ChartMetric) {
        chartMetric.value = metric
    }
}
