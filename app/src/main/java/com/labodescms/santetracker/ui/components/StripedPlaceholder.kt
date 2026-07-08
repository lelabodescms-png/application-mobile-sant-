package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppType
import kotlin.math.hypot

/**
 * Diagonal-stripe "photo" placeholder — matches the CSS
 * `repeating-linear-gradient(135deg, #232428, #232428 6px, #2b2c31 6px, #2b2c31 12px)` pattern
 * used until a real photo picker replaces it.
 */
@Composable
fun StripedPlaceholder(modifier: Modifier = Modifier, stripeWidthDp: Float = 6f, showLabel: Boolean = true) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = AppColors.PlaceholderStripeDark, size = size)
            val stripePx = stripeWidthDp.dp.toPx()
            val period = stripePx * 2
            val diagonal = hypot(size.width.toDouble(), size.height.toDouble()).toFloat()
            rotate(degrees = 45f, pivot = Offset(size.width / 2, size.height / 2)) {
                var x = -diagonal
                while (x < diagonal * 2) {
                    drawRect(
                        color = AppColors.PlaceholderStripeLight,
                        topLeft = Offset(x, -diagonal),
                        size = Size(stripePx, diagonal * 3),
                    )
                    x += period
                }
            }
        }
        if (showLabel) {
            BasicText(
                text = "photo",
                style = AppType.BodySmall.copy(color = AppColors.PlaceholderText, textAlign = TextAlign.Center),
            )
        }
    }
}
