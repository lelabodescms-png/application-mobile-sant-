package com.labodescms.santetracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labodescms.santetracker.R
import com.labodescms.santetracker.domain.AppUiState
import com.labodescms.santetracker.ui.components.LineAreaChart
import com.labodescms.santetracker.ui.components.LogoBadge
import com.labodescms.santetracker.ui.components.PillButton
import com.labodescms.santetracker.ui.components.PillTag
import com.labodescms.santetracker.ui.components.ProgressRing
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppShapes
import com.labodescms.santetracker.ui.theme.AppType
import com.labodescms.santetracker.ui.util.formatGoal
import com.labodescms.santetracker.ui.util.formatWater
import com.labodescms.santetracker.ui.util.formatWeight
import com.labodescms.santetracker.ui.util.longDateLabel
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    state: AppUiState,
    onWeighInClick: () -> Unit,
    onIncWater: () -> Unit,
    onDecWater: () -> Unit,
    onIncFasting: () -> Unit,
    onDecFasting: () -> Unit,
    onAddActivityClick: () -> Unit,
    onGoToJournal: () -> Unit,
) {
    val today = LocalDate.now()
    val history = state.weightHistory
    val current = history.lastOrNull()?.weight ?: 0.0
    val start = history.firstOrNull()?.weight ?: current
    val goal = state.weightGoal
    val kgToGo = current - goal
    val pct = if (start - goal != 0.0) ((start - current) / (start - goal)).coerceIn(0.0, 1.0) else 0.0
    val sparkValues = history.takeLast(7).map { it.weight }
    val mealsToday = state.meals[today]?.size ?: 0

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoBadge(size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        BasicText("Bonjour", style = AppType.GreetingTitle.copy(color = AppColors.TextPrimary))
                        Spacer(Modifier.height(4.dp))
                        BasicText(longDateLabel(today), style = AppType.Body.copy(color = AppColors.TextDim))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AppColors.Card)
                        .border(1.dp, AppColors.Border, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bell),
                        contentDescription = "Notifications",
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 9.dp, end = 1.dp)
                            .clip(CircleShape)
                            .background(AppColors.Gold),
                    )
                }
            }
        }

        item {
            WeightCard(
                current = current,
                goal = goal,
                kgToGo = kgToGo,
                pct = pct,
                sparkValues = sparkValues,
                onWeighInClick = onWeighInClick,
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                WaterCard(
                    water = state.water,
                    waterGoal = state.waterGoal,
                    onInc = onIncWater,
                    onDec = onDecWater,
                    modifier = Modifier.weight(1f),
                )
                ActivityCard(
                    current = state.activityMin,
                    goal = state.activityGoal,
                    onAddClick = onAddActivityClick,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                FastingCard(
                    current = state.fastingHr,
                    goal = state.fastingGoal,
                    onInc = onIncFasting,
                    onDec = onDecFasting,
                    modifier = Modifier.weight(1f),
                )
                MealsCard(
                    count = mealsToday,
                    onClick = onGoToJournal,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WeightCard(
    current: Double,
    goal: Double,
    kgToGo: Double,
    pct: Double,
    sparkValues: List<Double>,
    onWeighInClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardLarge)
            .background(AppColors.Card)
            .border(1.dp, AppColors.Border, AppShapes.CardLarge)
            .padding(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BasicText("Poids", style = AppType.CardLabel.copy(color = AppColors.TextDim))
            PillTag(
                text = "Objectif ${formatGoal(goal)} kg",
                containerColor = AppColors.GoldSoft,
                contentColor = AppColors.Gold,
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(formatWeight(current), style = AppType.WeightValue.copy(color = AppColors.TextPrimary))
                Spacer(Modifier.width(6.dp))
                BasicText("kg", style = AppType.Body.copy(color = AppColors.TextDim, fontWeight = FontWeight.SemiBold, fontSize = 17.sp))
            }
            val toGoLabel = if (kgToGo <= 0) "Objectif atteint !" else "+${formatWeight(kgToGo)} kg à faire"
            BasicText(toGoLabel, style = AppType.Body.copy(color = AppColors.Positive, fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.height(14.dp))
        LineAreaChart(
            values = sparkValues,
            lineColor = AppColors.Gold,
            areaColor = AppColors.GoldSoft,
            height = 90.dp,
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppColors.Track),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct.toFloat())
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppColors.Gold),
            )
        }
        Spacer(Modifier.height(10.dp))
        BasicText(
            "${(pct * 100).roundToInt()}% de l'objectif atteint",
            style = AppType.Body.copy(color = AppColors.TextDim),
        )
        Spacer(Modifier.height(16.dp))
        PillButton(text = "+ Peser aujourd'hui", onClick = onWeighInClick)
    }
}

@Composable
private fun MetricCardShell(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = Modifier
        .clip(AppShapes.CardLarge)
        .background(AppColors.Card)
        .border(1.dp, AppColors.Border, AppShapes.CardLarge)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(18.dp)
    Column(
        modifier = modifier.then(base),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun RingValueLabel(value: String, unitLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(value, style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp))
        BasicText(unitLabel, style = AppType.BodySmall.copy(color = AppColors.TextDim, fontSize = 11.5.sp))
    }
}

@Composable
private fun WaterCard(water: Double, waterGoal: Double, onInc: () -> Unit, onDec: () -> Unit, modifier: Modifier = Modifier) {
    MetricCardShell(modifier = modifier) {
        BasicText("Eau", style = AppType.CardLabel.copy(color = AppColors.TextDim), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ProgressRing(current = water, goal = waterGoal, trackColor = AppColors.Track, progressColor = AppColors.Water) {
            RingValueLabel(value = formatWater(water), unitLabel = "L / ${formatGoal(waterGoal)}")
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundActionButton(label = "−", background = AppColors.TrackSoft, contentColor = AppColors.TextPrimary, onClick = onDec, modifier = Modifier.weight(1f))
            RoundActionButton(label = "+", background = AppColors.WaterSoft, contentColor = AppColors.Water, onClick = onInc, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActivityCard(current: Int, goal: Int, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    MetricCardShell(modifier = modifier) {
        BasicText("Activité", style = AppType.CardLabel.copy(color = AppColors.TextDim), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ProgressRing(current = current.toDouble(), goal = goal.toDouble(), trackColor = AppColors.Track, progressColor = AppColors.Activity) {
            RingValueLabel(value = "$current", unitLabel = "min / $goal")
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppColors.ActivitySoft)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center,
        ) {
            BasicText("+ Ajouter", style = AppType.Body.copy(color = AppColors.Activity, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun FastingCard(current: Int, goal: Int, onInc: () -> Unit, onDec: () -> Unit, modifier: Modifier = Modifier) {
    MetricCardShell(modifier = modifier) {
        BasicText("Jeûne", style = AppType.CardLabel.copy(color = AppColors.TextDim), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ProgressRing(current = current.toDouble(), goal = goal.toDouble(), trackColor = AppColors.Track, progressColor = AppColors.Fasting) {
            RingValueLabel(value = "${current}h", unitLabel = "/ ${goal}h")
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundActionButton(label = "−", background = AppColors.TrackSoft, contentColor = AppColors.TextPrimary, onClick = onDec, modifier = Modifier.weight(1f))
            RoundActionButton(label = "+", background = AppColors.FastingSoft, contentColor = AppColors.Fasting, onClick = onInc, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MealsCard(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MetricCardShell(modifier = modifier, onClick = onClick) {
        BasicText("Repas du jour", style = AppType.CardLabel.copy(color = AppColors.TextDim), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(AppColors.MealsSoft),
            contentAlignment = Alignment.Center,
        ) {
            BasicText("$count", style = AppType.WeightValue.copy(fontSize = 30.sp, color = AppColors.Meals))
        }
        Spacer(Modifier.height(14.dp))
        BasicText("Voir le journal →", style = AppType.BodySmall.copy(color = AppColors.TextDim))
    }
}

@Composable
private fun RoundActionButton(label: String, background: Color, contentColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(label, style = AppType.CardLabel.copy(color = contentColor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp))
    }
}
