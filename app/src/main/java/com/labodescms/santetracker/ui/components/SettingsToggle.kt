package com.labodescms.santetracker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.labodescms.santetracker.ui.theme.AppColors

/** 48x28 pill switch with a 22dp sliding white thumb, colored on-state per metric (README spec). */
@Composable
fun SettingsToggle(
    checked: Boolean,
    onColor: Color,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackColor = if (checked) onColor else AppColors.Track
    val thumbOffset by animateDpAsState(targetValue = if (checked) 23.dp else 3.dp, animationSpec = tween(150), label = "thumb")
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
            .clickable(onClick = onCheckedChange),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
