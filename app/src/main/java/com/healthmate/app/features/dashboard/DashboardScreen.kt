package com.healthmate.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthmate.app.di.AppViewModelProvider
import com.healthmate.app.ui.components.DailyBarChart
import com.healthmate.app.ui.components.GoalBar
import com.healthmate.app.ui.components.GoalProgressChart
import com.healthmate.app.ui.components.StatCard
import com.healthmate.app.ui.theme.CaloriesRed
import com.healthmate.app.ui.theme.StepsGreen
import com.healthmate.app.ui.theme.WaterBlue
import com.healthmate.app.utils.DateUtils
import com.healthmate.app.utils.SummaryPeriod
import com.healthmate.app.utils.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    bottomBar: @Composable () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary
    val periodGoals = uiState.periodGoals
    val days = uiState.range.days

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HealthMate", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = bottomBar
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---- Day / Week / Month selector ----
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = uiState.period == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period.label) }
                        )
                    }
                }
            }

            // ---- Title with the date range ----
            item {
                Column {
                    Text(
                        text = when (uiState.period) {
                            SummaryPeriod.DAY -> "Today"
                            SummaryPeriod.WEEK -> "Last 7 days"
                            SummaryPeriod.MONTH -> "This month"
                        } + " · ${summary.entryCount} entr" +
                                (if (summary.entryCount == 1) "y" else "ies") + " recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = DateUtils.rangeText(uiState.range),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ---- Totals and daily averages ----
            item {
                StatCard(
                    emoji = "👣",
                    label = "Steps",
                    value = formatNumber(summary.totalSteps),
                    unit = "steps",
                    color = StepsGreen,
                    supportingText = averageText(summary.totalSteps, days, "steps")
                )
            }
            item {
                StatCard(
                    emoji = "🔥",
                    label = "Calories Burned",
                    value = formatNumber(summary.totalCalories),
                    unit = "kcal",
                    color = CaloriesRed,
                    supportingText = averageText(summary.totalCalories, days, "kcal")
                )
            }
            item {
                StatCard(
                    emoji = "💧",
                    label = "Water Intake",
                    value = formatNumber(summary.totalWater),
                    unit = "ml",
                    color = WaterBlue,
                    supportingText = averageText(summary.totalWater, days, "ml")
                )
            }

            // ---- Summary bar chart (progress towards goals) ----
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = "Summary vs goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (days == 1) "Daily goals can be changed in Settings"
                            else "Goal for $days days = daily goal × $days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GoalProgressChart(
                            bars = listOf(
                                GoalBar("Steps", "👣", summary.totalSteps, periodGoals.steps, "steps", StepsGreen),
                                GoalBar("Calories", "🔥", summary.totalCalories, periodGoals.calories, "kcal", CaloriesRed),
                                GoalBar("Water", "💧", summary.totalWater, periodGoals.water, "ml", WaterBlue)
                            )
                        )
                    }
                }
            }

            // ---- Daily bar chart (week and month only) ----
            if (uiState.period != SummaryPeriod.DAY && uiState.dailyTotals.isNotEmpty()) {
                item {
                    DailyChartCard(uiState = uiState, onMetricSelected = viewModel::selectChartMetric)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyChartCard(
    uiState: DashboardUiState,
    onMetricSelected: (ChartMetric) -> Unit
) {
    val (color, goal) = when (uiState.chartMetric) {
        ChartMetric.STEPS -> StepsGreen to uiState.goals.steps
        ChartMetric.CALORIES -> CaloriesRed to uiState.goals.calories
        ChartMetric.WATER -> WaterBlue to uiState.goals.water
    }
    val values = uiState.dailyTotals.map {
        when (uiState.chartMetric) {
            ChartMetric.STEPS -> it.steps
            ChartMetric.CALORIES -> it.calories
            ChartMetric.WATER -> it.water
        }
    }
    val dates = uiState.range.dates()
    val labels = if (uiState.period == SummaryPeriod.WEEK) {
        dates.map { DateUtils.weekday(it) }
    } else {
        // Month: label the 1st and every 5th day so the labels do not overlap
        dates.map { if (it.dayOfMonth == 1 || it.dayOfMonth % 5 == 0) it.dayOfMonth.toString() else "" }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Daily ${uiState.chartMetric.label.lowercase()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Line = daily goal (${formatNumber(goal)})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChartMetric.entries.forEach { metric ->
                    val metricColor = when (metric) {
                        ChartMetric.STEPS -> StepsGreen
                        ChartMetric.CALORIES -> CaloriesRed
                        ChartMetric.WATER -> WaterBlue
                    }
                    FilterChip(
                        selected = uiState.chartMetric == metric,
                        onClick = { onMetricSelected(metric) },
                        label = { Text(metric.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = metricColor.copy(alpha = 0.2f),
                            selectedLabelColor = metricColor
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            DailyBarChart(values = values, labels = labels, goal = goal, color = color, chartHeight = 120.dp)
        }
    }
}

/** "Average 7,850 steps / day" for week and month; null for a single day. */
private fun averageText(total: Int, days: Int, unit: String): String? =
    if (days <= 1) null else "Average ${formatNumber(total / days)} $unit / day"

