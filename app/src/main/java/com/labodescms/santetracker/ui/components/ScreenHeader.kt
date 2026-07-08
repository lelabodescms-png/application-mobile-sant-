package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.labodescms.santetracker.R
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppType

/** Rounded gold-bordered badge with the dumbbell logo — used at the left of every screen title. */
@Composable
fun LogoBadge(modifier: Modifier = Modifier, size: Dp = 38.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1D1E23), Color(0xFF111217)),
                    start = Offset(0f, 0f),
                    end = Offset(size.value, size.value),
                ),
            )
            .border(1.5.dp, AppColors.Gold.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_dumbbell),
            contentDescription = null,
            tint = AppColors.Gold,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}

/** Screen title row: logo badge + title text, as used on Poids / Journal / Réglages. */
@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LogoBadge()
        Spacer(Modifier.width(12.dp))
        BasicText(text = title, style = AppType.ScreenTitle.copy(color = AppColors.TextPrimary))
    }
}
