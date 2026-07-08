package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Simple line + filled-area chart drawn from raw values (no external charting library),
 * matching the prototype's hand-rolled SVG polyline/area builder. An optional dashed goal
 * line is drawn at [goalValue]'s vertical position (used by the annual weight chart).
 */
@Composable
fun LineAreaChart(
    values: List<Double>,
    lineColor: Color,
    areaColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 90.dp,
    paddingTop: Dp = 8.dp,
    paddingBottom: Dp = 8.dp,
    goalValue: Double? = null,
    goalLineColor: Color = Color.Transparent,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val padTopPx = paddingTop.toPx()
        val padBottomPx = paddingBottom.toPx()

        val allValues = if (goalValue != null) values + goalValue else values
        val min = allValues.min()
        val max = allValues.max()
        val range = (max - min).takeIf { it != 0.0 } ?: 1.0

        fun yFor(v: Double): Float =
            (padTopPx + (1 - (v - min) / range) * (h - padTopPx - padBottomPx)).toFloat()

        val n = values.size
        val points = values.mapIndexed { i, v ->
            val x = if (n == 1) 0f else (i / (n - 1).toFloat()) * w
            Offset(x, yFor(v))
        }

        val linePath = Path().apply {
            points.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(areaPath, color = areaColor)
        drawPath(
            linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        if (goalValue != null) {
            val goalY = yFor(goalValue)
            drawLine(
                color = goalLineColor,
                start = Offset(0f, goalY),
                end = Offset(w, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f),
            )
        }
    }
}
