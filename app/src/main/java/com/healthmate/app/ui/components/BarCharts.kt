package com.healthmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthmate.app.ui.theme.CaloriesRed
import com.healthmate.app.ui.theme.HealthMateTheme
import com.healthmate.app.ui.theme.StepsGreen
import com.healthmate.app.ui.theme.WaterBlue
import com.healthmate.app.utils.formatNumber
import kotlin.math.roundToInt

/** One bar of the goal-progress chart. */
data class GoalBar(
    val label: String,
    val emoji: String,
    val value: Int,
    val goal: Int,
    val unit: String,
    val color: Color
)

/**
 * Summary bar chart: one vertical bar each for steps, calories and water.
 * The light background of each bar is the goal (100 %) and the coloured part
 * is how much of the goal has been reached. Built with plain Compose layouts,
 * so no chart library is needed.
 */
@Composable
fun GoalProgressChart(
    bars: List<GoalBar>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 110.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val fraction = if (bar.goal > 0) bar.value.toFloat() / bar.goal else 0f
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(fraction * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = bar.color
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Track = 100 % of the goal
                Box(
                    modifier = Modifier
                        .height(chartHeight)
                        .width(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bar.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Filled part = progress (capped at the top of the track)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(10.dp))
                            .background(bar.color)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${bar.emoji} ${bar.label}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${formatNumber(bar.value)} / ${formatNumber(bar.goal)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = bar.color,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = bar.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Daily bar chart for the week / month view: one bar per day.
 * A grey line marks the daily goal; bars that reach the goal are full colour,
 * bars below the goal are lighter.
 */
@Composable
fun DailyBarChart(
    values: List<Int>,
    labels: List<String>,
    goal: Int,
    color: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 110.dp
) {
    val maxValue = maxOf(values.maxOrNull() ?: 0, goal).let { if (it <= 0) 1f else it * 1.1f }
    val goalFraction = goal / maxValue
    val gap = if (values.size > 10) 2.dp else 8.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEach { value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight((value / maxValue).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (value >= goal) color else color.copy(alpha = 0.45f))
                    )
                }
            }
            // Goal line
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = chartHeight * goalFraction)
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalProgressChartPreview() {
    HealthMateTheme(darkTheme = false) {
        GoalProgressChart(
            bars = listOf(
                GoalBar("Steps", "👣", 7850, 10000, "steps", StepsGreen),
                GoalBar("Calories", "🔥", 420, 500, "kcal", CaloriesRed),
                GoalBar("Water", "💧", 2000, 2000, "ml", WaterBlue)
            )
        )
    }
}
