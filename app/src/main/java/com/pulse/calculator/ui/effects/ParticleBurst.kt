package com.pulse.calculator.ui.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulse.calculator.ui.theme.ClearContainer
import com.pulse.calculator.ui.theme.EqualsContainer
import com.pulse.calculator.ui.theme.FunctionContainer
import com.pulse.calculator.ui.theme.OperatorContainer
import com.pulse.calculator.ui.theme.ParticleCount
import com.pulse.calculator.ui.theme.ParticleGravity
import com.pulse.calculator.ui.theme.ParticleLifeMillis
import com.pulse.calculator.ui.theme.ParticleMaxRadius
import com.pulse.calculator.ui.theme.ParticleMaxSpeed
import com.pulse.calculator.ui.theme.ParticleMinRadius
import com.pulse.calculator.ui.theme.ParticleMinSpeed
import com.pulse.calculator.ui.theme.ParticleSpreadDegrees
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** One particle, in dp so it scales with density. Angle 0 points right, -90 degrees points up. */
data class Particle(
    val angleRadians: Float,
    val speedDp: Float,
    val radiusDp: Float,
    val color: Color,
)

private val palette = listOf(OperatorContainer, FunctionContainer, EqualsContainer, ClearContainer)

/** Deterministic for a given [seed]; always [ParticleCount] particles, all flying upwards. */
fun createParticles(seed: Int): List<Particle> {
    val random = Random(seed)
    val spread = ParticleSpreadDegrees * PI.toFloat() / 180f
    val up = -PI.toFloat() / 2f
    return List(ParticleCount) {
        Particle(
            angleRadians = up + (random.nextFloat() * 2f - 1f) * spread,
            speedDp = ParticleMinSpeed.value + random.nextFloat() * (ParticleMaxSpeed.value - ParticleMinSpeed.value),
            radiusDp = ParticleMinRadius.value + random.nextFloat() * (ParticleMaxRadius.value - ParticleMinRadius.value),
            color = palette[random.nextInt(palette.size)],
        )
    }
}

/**
 * A short burst of particles from [origin]. It draws nothing while idle and a new [burstId]
 * restarts it. It has no pointer input, so it can never intercept a tap.
 */
@Composable
fun ParticleBurst(burstId: Int, origin: Offset, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    val particles = remember(burstId) { createParticles(burstId) }

    LaunchedEffect(burstId) {
        if (burstId > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(ParticleLifeMillis, easing = LinearEasing))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = progress.value
        if (p >= 1f) return@Canvas
        val seconds = p * ParticleLifeMillis / 1000f
        val gravity = ParticleGravity.toPx()
        val fade = 1f - p
        for (particle in particles) {
            val speed = particle.speedDp.dp.toPx()
            val x = origin.x + cos(particle.angleRadians) * speed * seconds
            val y = origin.y + sin(particle.angleRadians) * speed * seconds + 0.5f * gravity * seconds * seconds
            drawCircle(
                color = particle.color.copy(alpha = fade),
                radius = particle.radiusDp.dp.toPx() * (0.4f + 0.6f * fade),
                center = Offset(x, y),
            )
        }
    }
}
