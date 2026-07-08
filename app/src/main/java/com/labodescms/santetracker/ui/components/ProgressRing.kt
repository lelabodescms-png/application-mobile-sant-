package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Circular progress ring matching the README spec: 112dp box, 47dp ring radius, 10dp stroke,
 * round cap, starts at the top (-90deg) and sweeps clockwise. Progress is capped visually at
 * 100% even if [current] exceeds [goal] (e.g. activity minutes beyond the daily goal).
 */
@Composable
fun ProgressRing(
    current: Double,
    goal: Double,
    trackColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier,
    boxSize: Dp = 112.dp,
    radius: Dp = 47.dp,
    strokeWidth: Dp = 10.dp,
    content: @Composable () -> Unit = {},
) {
    val fraction = if (goal > 0) (current / goal).toFloat().coerceIn(0f, 1f) else 0f
    Box(modifier = modifier.size(boxSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(boxSize)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val radiusPx = radius.toPx()
            val center = boxSize.toPx() / 2
            val topLeft = Offset(center - radiusPx, center - radiusPx)
            val arcSize = Size(radiusPx * 2, radiusPx * 2)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            if (fraction > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
        }
        content()
    }
}
