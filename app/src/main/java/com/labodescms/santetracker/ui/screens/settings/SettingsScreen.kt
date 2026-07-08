package com.labodescms.santetracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labodescms.santetracker.domain.AppUiState
import com.labodescms.santetracker.ui.components.ScreenHeader
import com.labodescms.santetracker.ui.components.SettingsToggle
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppShapes
import com.labodescms.santetracker.ui.theme.AppType
import com.labodescms.santetracker.ui.util.formatGoal

@Composable
fun SettingsScreen(
    state: AppUiState,
    onToggleWeighIn: () -> Unit,
    onToggleHydration: () -> Unit,
    onToggleActivity: () -> Unit,
) {
    val start = state.weightHistory.firstOrNull()?.weight ?: 0.0

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            ScreenHeader(title = "Réglages", modifier = Modifier.padding(bottom = 20.dp))
        }
        item {
            SectionLabel("OBJECTIF")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(AppShapes.CardSmall)
                    .background(AppColors.Card)
                    .border(1.dp, AppColors.Border, AppShapes.CardSmall),
            ) {
                GoalColumn(label = "Départ", value = "${formatGoal(start)} kg", modifier = Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AppColors.Border))
                GoalColumn(label = "Objectif", value = "${formatGoal(state.weightGoal)} kg", valueColor = AppColors.Gold, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(26.dp))
        }
        item {
            SectionLabel("NOTIFICATIONS")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.CardSmall)
                    .background(AppColors.Card)
                    .border(1.dp, AppColors.Border, AppShapes.CardSmall),
            ) {
                NotificationRow(
                    title = "Rappel de pesée",
                    subtitle = "Tous les jours à 8h00",
                    checked = state.notifications.weighIn,
                    onColor = AppColors.Gold,
                    onToggle = onToggleWeighIn,
                    showDivider = true,
                )
                NotificationRow(
                    title = "Rappel de boire de l'eau",
                    subtitle = "Toutes les 2 heures",
                    checked = state.notifications.hydration,
                    onColor = AppColors.Water,
                    onToggle = onToggleHydration,
                    showDivider = true,
                )
                NotificationRow(
                    title = "Rappel d'activité physique",
                    subtitle = "Tous les jours à 18h00",
                    checked = state.notifications.activity,
                    onColor = AppColors.Activity,
                    onToggle = onToggleActivity,
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    BasicText(
        text,
        style = AppType.SectionLabel.copy(color = AppColors.TextDim),
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun GoalColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(label, style = AppType.BodySmall.copy(color = AppColors.TextDim, fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        BasicText(value, style = AppType.CardLabel.copy(color = valueColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
    }
}

@Composable
private fun NotificationRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onColor: androidx.compose.ui.graphics.Color,
    onToggle: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(title, style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontSize = 15.sp))
                Spacer(Modifier.height(3.dp))
                BasicText(subtitle, style = AppType.BodySmall.copy(color = AppColors.TextDim, fontSize = 13.sp))
            }
            SettingsToggle(checked = checked, onColor = onColor, onCheckedChange = onToggle)
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
        }
    }
}
