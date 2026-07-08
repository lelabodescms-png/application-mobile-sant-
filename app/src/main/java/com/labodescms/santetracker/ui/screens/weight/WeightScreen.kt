package com.labodescms.santetracker.ui.screens.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labodescms.santetracker.domain.WeightPoint
import com.labodescms.santetracker.domain.AppUiState
import com.labodescms.santetracker.ui.components.LineAreaChart
import com.labodescms.santetracker.ui.components.PillButton
import com.labodescms.santetracker.ui.components.ScreenHeader
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppShapes
import com.labodescms.santetracker.ui.theme.AppType
import com.labodescms.santetracker.ui.util.formatGoal
import com.labodescms.santetracker.ui.util.formatWeight
import com.labodescms.santetracker.ui.util.shortDateLabel

private data class HistoryRow(
    val dateLabel: String,
    val weightLabel: String,
    val deltaLabel: String,
    val deltaColor: Color,
)

private fun buildHistoryRows(history: List<WeightPoint>): List<HistoryRow> =
    history.reversed().take(8).mapIndexed { i, point ->
        val originalIndex = history.size - 1 - i
        val prev = history.getOrNull(originalIndex - 1)
        val delta = if (prev != null) point.weight - prev.weight else 0.0
        HistoryRow(
            dateLabel = shortDateLabel(point.date),
            weightLabel = "${formatWeight(point.weight)} kg",
            deltaLabel = if (prev != null) {
                (if (delta > 0) "+" else "") + formatWeight(delta)
            } else "—",
            deltaColor = if (delta <= 0) AppColors.Positive else AppColors.Negative,
        )
    }

@Composable
fun WeightScreen(state: AppUiState, onAddWeightClick: () -> Unit) {
    val history = state.weightHistory
    val start = history.firstOrNull()?.weight ?: 0.0
    val current = history.lastOrNull()?.weight ?: 0.0
    val goal = state.weightGoal
    val historyRows = remember(history) { buildHistoryRows(history) }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            ScreenHeader(title = "Suivi du poids", modifier = Modifier.padding(bottom = 18.dp))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(label = "Départ", value = formatWeight(start), modifier = Modifier.weight(1f))
                StatCard(label = "Actuel", value = formatWeight(current), valueColor = AppColors.Gold, modifier = Modifier.weight(1f))
                StatCard(label = "Objectif", value = formatGoal(goal), modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.CardLarge)
                    .background(AppColors.Card)
                    .border(1.dp, AppColors.Border, AppShapes.CardLarge)
                    .padding(18.dp),
            ) {
                LineAreaChart(
                    values = history.map { it.weight },
                    lineColor = AppColors.Gold,
                    areaColor = AppColors.GoldSoft,
                    height = 160.dp,
                    paddingTop = 10.dp,
                    paddingBottom = 10.dp,
                    goalValue = goal,
                    goalLineColor = AppColors.GoalLineColor,
                )
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Jan", "Mar", "Mai", "Jul").forEach {
                        BasicText(it, style = AppType.BodySmall.copy(color = AppColors.TextDim))
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        item {
            PillButton(text = "+ Ajouter une pesée", onClick = onAddWeightClick)
            Spacer(Modifier.height(24.dp))
        }
        item {
            BasicText("Historique", style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
            Spacer(Modifier.height(8.dp))
        }
        items(historyRows) { row ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(row.dateLabel, style = AppType.Body.copy(color = AppColors.TextDim, fontSize = 14.5.sp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText(row.weightLabel, style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp))
                        Spacer(Modifier.width(14.dp))
                        BasicText(
                            row.deltaLabel,
                            style = AppType.BodySmall.copy(color = row.deltaColor, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, textAlign = TextAlign.End),
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = AppColors.TextPrimary) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(AppColors.Card)
            .border(1.dp, AppColors.Border, AppShapes.CardSmall)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(label, style = AppType.BodySmall.copy(color = AppColors.TextDim, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        BasicText(value, style = AppType.CardLabel.copy(color = valueColor, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp))
    }
}
