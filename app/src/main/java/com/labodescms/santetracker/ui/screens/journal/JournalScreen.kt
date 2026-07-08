package com.labodescms.santetracker.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labodescms.santetracker.domain.AppUiState
import com.labodescms.santetracker.domain.Meal
import com.labodescms.santetracker.ui.components.ScreenHeader
import com.labodescms.santetracker.ui.components.StripedPlaceholder
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppShapes
import com.labodescms.santetracker.ui.theme.AppType
import com.labodescms.santetracker.ui.util.longDateLabel
import com.labodescms.santetracker.ui.util.weekdayShort
import java.time.LocalDate

@Composable
fun JournalScreen(
    state: AppUiState,
    onSelectDate: (LocalDate) -> Unit,
    onAddMealClick: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val days = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
    val meals = state.meals[state.journalSelectedDate].orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                ScreenHeader(title = "Journal des repas", modifier = Modifier.padding(bottom = 18.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    days.forEach { day ->
                        DateChip(day = day, selected = day == state.journalSelectedDate, onClick = { onSelectDate(day) })
                    }
                }
            }
            item {
                BasicText(
                    longDateLabel(state.journalSelectedDate),
                    style = AppType.Body.copy(color = AppColors.TextDim, fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }
            if (meals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp, horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        BasicText(
                            "Aucun repas enregistré pour ce jour.",
                            style = AppType.Body.copy(color = AppColors.TextDim, textAlign = TextAlign.Center),
                        )
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    MealRow(meal = meal, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
            item { Spacer(Modifier.size(72.dp)) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 88.dp)
                .size(58.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = AppColors.Gold, spotColor = AppColors.Gold)
                .clip(CircleShape)
                .background(AppColors.Gold)
                .clickable(onClick = onAddMealClick),
            contentAlignment = Alignment.Center,
        ) {
            BasicText("+", style = AppType.WeightValue.copy(color = AppColors.GoldOnGold, fontSize = 26.sp))
        }
    }
}

@Composable
private fun DateChip(day: LocalDate, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) AppColors.Gold else AppColors.TextDim
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AppColors.GoldBadgeSoft else Color.Transparent)
            .border(1.dp, if (selected) AppColors.Gold else AppColors.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(weekdayShort(day), style = AppType.BodySmall.copy(color = color, fontWeight = FontWeight.Bold))
        Spacer(Modifier.size(4.dp))
        BasicText("${day.dayOfMonth}", style = AppType.CardLabel.copy(color = color, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp))
    }
}

@Composable
private fun MealRow(meal: Meal, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Card)
            .border(1.dp, AppColors.Border, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StripedPlaceholder(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                meal.name,
                style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontSize = 15.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(3.dp))
            BasicText(meal.time, style = AppType.BodySmall.copy(color = AppColors.TextDim, fontSize = 13.sp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AppColors.MealsSoft)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            BasicText(meal.type.label, style = AppType.BodySmall.copy(color = AppColors.Meals, fontWeight = FontWeight.Bold, fontSize = 12.5.sp))
        }
    }
}
