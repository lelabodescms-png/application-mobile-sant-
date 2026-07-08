package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppShapes
import com.labodescms.santetracker.ui.theme.AppType

/** Full-width, fully-rounded call-to-action button — the "+ Peser aujourd'hui" style button. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.Gold,
    contentColor: Color = AppColors.GoldOnGold,
) {
    BasicText(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Pill)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
            .wrapContentHeight(),
        style = AppType.PillButton.copy(color = contentColor, textAlign = TextAlign.Center),
    )
}

/** Small rounded pill, e.g. the gold "Objectif 70 kg" badge or meal-type chips. */
@Composable
fun PillTag(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    var mod = modifier
        .clip(RoundedCornerShape(999.dp))
        .background(containerColor)
    if (borderColor != null) {
        mod = mod.border(1.dp, borderColor, RoundedCornerShape(999.dp))
    }
    if (onClick != null) {
        mod = mod.clickable(onClick = onClick)
    }
    BasicText(
        text = text,
        modifier = mod.padding(horizontal = 14.dp, vertical = 7.dp),
        style = AppType.BodySmall.copy(color = contentColor, fontWeight = FontWeight.Bold),
    )
}
