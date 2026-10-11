package com.pulse.calculator.ui.effects

import com.pulse.calculator.ui.theme.ParticleCount
import com.pulse.calculator.ui.theme.ResultPeakScale
import com.pulse.calculator.ui.theme.ResultStartScale
import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectMathTest {

    @Test
    fun shakeStartsAndEndsAtRest() {
        assertEquals(0f, shakeFactor(0f), 1e-6f)
        assertEquals(0f, shakeFactor(1f), 1e-6f)
    }

    @Test
    fun shakeSwingsBothWaysAndDecays() {
        val samples = (1..99).map { shakeFactor(it / 100f) }
        assertTrue(samples.any { it > 0.3f })
        assertTrue(samples.any { it < -0.3f })
        assertTrue(samples.all { it in -1f..1f })
        assertTrue(samples.take(30).maxOf { kotlin.math.abs(it) } > samples.takeLast(30).maxOf { kotlin.math.abs(it) })
    }

    @Test
    fun pulseIsZeroAtBothEndsAndPeaksInTheMiddle() {
        assertEquals(0f, pulseFactor(0f), 1e-6f)
        assertEquals(0f, pulseFactor(1f), 1e-6f)
        assertEquals(1f, pulseFactor(0.5f), 1e-6f)
    }

    @Test
    fun resultScaleGrowsPastOneThenSettlesAtExactlyOne() {
        assertEquals(ResultStartScale, resultScale(0f), 1e-6f)
        assertTrue(resultScale(0.33f) > 1f)
        assertEquals(ResultPeakScale, (0..100).maxOf { resultScale(it / 100f) }, 0.01f)
        assertEquals(1f, resultScale(1f), 1e-6f)
    }

    @Test
    fun resultRisesIntoPlaceAndStaysThere() {
        assertEquals(1f, resultRiseRemaining(0f), 1e-6f)
        assertEquals(0f, resultRiseRemaining(1f), 1e-6f)
        assertTrue(resultRiseRemaining(0.2f) > resultRiseRemaining(0.4f))
    }

    @Test
    fun aFinishedEffectDrawsNothing() {
        assertEquals(0f, pulseFactor(1f), 1e-6f)
        assertEquals(0f, shakeFactor(1f), 1e-6f)
        assertEquals(1f, resultScale(1f), 1e-6f)
        assertEquals(0f, resultRiseRemaining(1f), 1e-6f)
    }

    @Test
    fun aBurstNeverHasMoreThanTwentyFourParticles() {
        assertTrue(ParticleCount <= 24)
        assertEquals(ParticleCount, createParticles(seed = 1).size)
        assertEquals(ParticleCount, createParticles(seed = 99).size)
    }

    @Test
    fun particlesAllFlyUpwardsAndAreDeterministic() {
        val particles = createParticles(seed = 7)
        assertEquals(particles, createParticles(seed = 7))
        assertTrue(particles.all { it.angleRadians < 0f && it.angleRadians > -PI.toFloat() })
        assertTrue(particles.all { it.speedDp > 0f && it.radiusDp > 0f })
    }
}
