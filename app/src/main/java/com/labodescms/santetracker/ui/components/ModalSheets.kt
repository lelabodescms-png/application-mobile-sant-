package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labodescms.santetracker.domain.MealType
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppShapes
import com.labodescms.santetracker.ui.theme.AppType
import com.labodescms.santetracker.ui.theme.Manrope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = AppShapes.Sheet,
        containerColor = AppColors.Sheet,
        contentColor = AppColors.TextPrimary,
        scrimColor = AppColors.OverlayScrim,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(AppShapes.Pill)
                    .background(AppColors.HandleColor),
            )
        },
    ) {
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 30.dp)) {
            content()
        }
    }
}

private val fieldTextStyle = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 26.sp,
    color = AppColors.TextPrimary,
)

@Composable
private fun NumericInputRow(value: String, onValueChange: (String) -> Unit, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(AppColors.Card)
            .border(1.dp, AppColors.Border, AppShapes.CardSmall)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                BasicText("0.0", style = fieldTextStyle.copy(color = AppColors.TextDim))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = fieldTextStyle,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(AppColors.Gold),
            )
        }
        BasicText(unit, style = AppType.Body.copy(color = AppColors.TextDim, fontWeight = FontWeight.Bold, fontSize = 16.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightInputSheet(
    sheetState: SheetState,
    value: String,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
) {
    AppBottomSheet(sheetState = sheetState, onDismissRequest = onDismissRequest) {
        BasicText("Ajouter une pesée", style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp))
        Spacer(Modifier.height(18.dp))
        NumericInputRow(value = value, onValueChange = onValueChange, unit = "kg")
        Spacer(Modifier.height(20.dp))
        PillButton(text = "Enregistrer", onClick = onSubmit)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityInputSheet(
    sheetState: SheetState,
    value: String,
    onValueChange: (String) -> Unit,
    onPreset: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
) {
    AppBottomSheet(sheetState = sheetState, onDismissRequest = onDismissRequest) {
        BasicText("Ajouter de l'activité", style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp))
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(10, 20, 30).forEach { preset ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.CardSmall)
                        .background(AppColors.ActivitySoft)
                        .clickable { onPreset(preset) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("+$preset", style = AppType.CardLabel.copy(color = AppColors.Activity, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        NumericInputRow(value = value, onValueChange = onValueChange, unit = "minutes")
        Spacer(Modifier.height(20.dp))
        PillButton(text = "Ajouter", onClick = onSubmit)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealInputSheet(
    sheetState: SheetState,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    selectedType: MealType,
    onTypeSelected: (MealType) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
) {
    AppBottomSheet(sheetState = sheetState, onDismissRequest = onDismissRequest) {
        BasicText("Ajouter un repas", style = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp))
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { type ->
                val active = type == selectedType
                PillTag(
                    text = type.label,
                    containerColor = if (active) AppColors.MealsSoft else AppColors.Card,
                    contentColor = if (active) AppColors.Meals else AppColors.TextDim,
                    borderColor = if (active) AppColors.Meals else AppColors.Border,
                    onClick = { onTypeSelected(type) },
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.CardSmall)
                .background(AppColors.Card)
                .border(1.dp, AppColors.Border, AppShapes.CardSmall)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            if (mealName.isEmpty()) {
                BasicText("Nom du repas", style = AppType.CardLabel.copy(color = AppColors.TextDim, fontSize = 16.sp))
            }
            BasicTextField(
                value = mealName,
                onValueChange = onMealNameChange,
                textStyle = AppType.CardLabel.copy(color = AppColors.TextPrimary, fontSize = 16.sp),
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Gold),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(20.dp))
        StripedPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(AppShapes.CardSmall),
        )
        Spacer(Modifier.height(20.dp))
        PillButton(text = "Ajouter le repas", onClick = onSubmit)
    }
}
