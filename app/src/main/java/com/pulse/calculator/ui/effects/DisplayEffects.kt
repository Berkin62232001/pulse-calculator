package com.pulse.calculator.ui.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import com.pulse.calculator.ui.theme.DisplayError
import com.pulse.calculator.ui.theme.ErrorTintCorner
import com.pulse.calculator.ui.theme.ErrorTintMillis
import com.pulse.calculator.ui.theme.ErrorTintPeakAlpha
import com.pulse.calculator.ui.theme.ParticleLifeMillis
import com.pulse.calculator.ui.theme.PulsePurple
import com.pulse.calculator.ui.theme.ResultGlowPeakAlpha
import com.pulse.calculator.ui.theme.ResultMillis
import com.pulse.calculator.ui.theme.ResultPeakAt
import com.pulse.calculator.ui.theme.ResultPeakScale
import com.pulse.calculator.ui.theme.ResultRiseDistance
import com.pulse.calculator.ui.theme.ResultRollEnd
import com.pulse.calculator.ui.theme.ResultStartScale
import com.pulse.calculator.ui.theme.ShakeMillis
import com.pulse.calculator.ui.theme.ShakeOscillations
import com.pulse.calculator.ui.theme.ShakeStrongAmplitude
import com.pulse.calculator.ui.theme.ShakeSubtleAmplitude
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/** Decaying sine in -1..1 for a shake at [progress] 0..1. Zero at the start and at rest. */
fun shakeFactor(progress: Float): Float = sin(2f * PI.toFloat() * ShakeOscillations * progress) * (1f - progress)

/** 0 at both ends, 1 in the middle: the shape of the red tint and of the result glow. */
fun pulseFactor(progress: Float): Float = sin(PI.toFloat() * progress)

/** Scale of the result as it rolls up: grows past 1, settles back. 1 once finished. */
fun resultScale(progress: Float): Float {
    val p = (progress / ResultRollEnd).coerceIn(0f, 1f)
    return if (p < ResultPeakAt) {
        ResultStartScale + (ResultPeakScale - ResultStartScale) * (p / ResultPeakAt)
    } else {
        ResultPeakScale + (1f - ResultPeakScale) * ((p - ResultPeakAt) / (1f - ResultPeakAt))
    }
}

/** 1 at the start (fully below its place), 0 once it has risen into it. */
fun resultRiseRemaining(progress: Float): Float {
    val p = (progress / ResultRollEnd).coerceIn(0f, 1f)
    return (1f - p) * (1f - p)
}

/**
 * Animation state for the display. It is driven only by [UiEvent][com.pulse.calculator.state.UiEvent]s
 * and never feeds anything back into the calculator state. Every value rests at its finished
 * position (progress 1), so a fresh instance draws nothing special.
 */
@Stable
class DisplayEffects {
    internal val shakeProgress = Animatable(1f)
    internal val errorProgress = Animatable(1f)
    internal val resultProgress = Animatable(1f)
    internal var shakeStrong = false

    /** Increments once per result so [ParticleBurst] knows a new burst started. */
    var burstId by mutableIntStateOf(0)
        private set

    /** The particle layer is only composed while a burst is running, so it costs nothing otherwise. */
    var burstActive by mutableStateOf(false)
        private set

    /** Centre of the = button in root coordinates, where particles start. */
    var equalsCenter by mutableStateOf(Offset.Zero)

    suspend fun shake(strong: Boolean) {
        shakeStrong = strong
        shakeProgress.snapTo(0f)
        shakeProgress.animateTo(1f, tween(ShakeMillis, easing = LinearEasing))
    }

    suspend fun pulseError() {
        errorProgress.snapTo(0f)
        errorProgress.animateTo(1f, tween(ErrorTintMillis, easing = LinearEasing))
    }

    suspend fun rollResult() {
        resultProgress.snapTo(0f)
        resultProgress.animateTo(1f, tween(ResultMillis, easing = LinearEasing))
    }

    suspend fun burst() {
        val id = ++burstId
        burstActive = true
        delay(ParticleLifeMillis.toLong())
        // A newer burst keeps the layer alive; only the latest one switches it off.
        if (id == burstId) burstActive = false
    }
}

/** Applies shake, result roll-up, glow and error tint. All values are read in the layer and draw phases. */
fun Modifier.displayEffects(effects: DisplayEffects): Modifier = this
    .graphicsLayer {
        val amplitude = (if (effects.shakeStrong) ShakeStrongAmplitude else ShakeSubtleAmplitude).toPx()
        val result = effects.resultProgress.value
        val scale = resultScale(result)
        translationX = amplitude * shakeFactor(effects.shakeProgress.value)
        translationY = ResultRiseDistance.toPx() * resultRiseRemaining(result)
        scaleX = scale
        scaleY = scale
        transformOrigin = TransformOrigin(1f, 0.5f)
    }
    .drawBehind {
        val tint = ErrorTintPeakAlpha * pulseFactor(effects.errorProgress.value)
        if (tint > 0f) {
            drawRoundRect(
                color = DisplayError.copy(alpha = tint),
                cornerRadius = CornerRadius(ErrorTintCorner.toPx()),
            )
        }
        val glow = ResultGlowPeakAlpha * pulseFactor(effects.resultProgress.value)
        if (glow > 0f) {
            // A circle that fades out before its own edge, so the glow has no hard sides
            // and may spill past the display's bounds.
            val center = Offset(size.width * 0.8f, size.height * 0.5f)
            val radius = size.width * 0.5f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PulsePurple.copy(alpha = glow), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }
    }
