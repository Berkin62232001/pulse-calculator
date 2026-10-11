package com.pulse.calculator.ui.effects

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import com.pulse.calculator.ui.theme.BackgroundDriftEasing
import com.pulse.calculator.ui.theme.BackgroundDriftMillis
import com.pulse.calculator.ui.theme.GlowBlue
import com.pulse.calculator.ui.theme.GlowViolet
import com.pulse.calculator.ui.theme.GradientBottom
import com.pulse.calculator.ui.theme.GradientMid
import com.pulse.calculator.ui.theme.GradientTop
import com.pulse.calculator.ui.theme.LocalReducedMotion
import com.pulse.calculator.ui.theme.PulseTheme

private const val RestingProgress = 0.5f
private const val GlowRadiusRatio = 0.7f

/** Deep navy-violet gradient with two slowly drifting glows. Holds still under reduced motion. */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val progress: State<Float> = if (LocalReducedMotion.current) {
        remember { mutableStateOf(RestingProgress) }
    } else {
        rememberInfiniteTransition(label = "background").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = BackgroundDriftMillis, easing = BackgroundDriftEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "drift",
        )
    }

    // The progress is read only while drawing, so the drift never triggers recomposition.
    Box(
        modifier = modifier.drawBehind {
            drawRect(
                Brush.linearGradient(
                    colors = listOf(GradientTop, GradientMid, GradientBottom),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )
            val p = progress.value
            drawGlow(
                color = GlowViolet,
                center = Offset(size.width * (0.2f + 0.6f * p), size.height * (0.25f + 0.15f * p)),
            )
            drawGlow(
                color = GlowBlue,
                center = Offset(size.width * (0.85f - 0.6f * p), size.height * (0.7f - 0.2f * p)),
            )
        },
    )
}

private fun DrawScope.drawGlow(color: Color, center: Offset) {
    val radius = size.maxDimension * GlowRadiusRatio
    drawCircle(
        brush = Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius),
        radius = radius,
        center = center,
    )
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun AnimatedBackgroundPreview() {
    PulseTheme { AnimatedBackground(Modifier.fillMaxSize()) }
}
