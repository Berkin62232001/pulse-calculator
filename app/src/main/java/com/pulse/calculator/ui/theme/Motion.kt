package com.pulse.calculator.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Single source for every duration, spring and easing in the app (TECHNICAL_DESIGN section 4).

// Button press feedback.
const val PressScale = 0.90f
const val FlashAmount = 0.28f
const val FlashInMillis = 40
const val FlashOutMillis = 220

val ButtonPressSpring = spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessHigh)

/** Low damping so the button overshoots past 1.0 a little when released. */
val ButtonReleaseSpring = spring<Float>(dampingRatio = 0.3f, stiffness = Spring.StiffnessMedium)

val FlashInSpec = tween<Color>(durationMillis = FlashInMillis, easing = LinearOutSlowInEasing)
val FlashOutSpec = tween<Color>(durationMillis = FlashOutMillis, easing = FastOutSlowInEasing)

// A newly typed glyph pops in.
const val GlyphPopStartScale = 0.6f
const val GlyphFadeInMillis = 90
val GlyphPopSpring = spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)

// A replaced operator flips over: half the time out, half the time in.
const val OperatorFlipMillis = 220
const val FlipCameraDistance = 12f

// Live preview value slides up and crossfades.
const val PreviewSlideMillis = 220
val PreviewSlideDistance = 14.dp

// Confirmed result rolls up with scale and a glow behind it. One progress value drives both.
const val ResultMillis = 700
const val ResultRollEnd = 0.55f
const val ResultStartScale = 0.85f
const val ResultPeakScale = 1.05f
const val ResultPeakAt = 0.6f
const val ResultGlowPeakAlpha = 0.55f
val ResultRiseDistance = 24.dp

// Shake: a decaying sine. Subtle for ignored input, strong for a math error (which also tints red).
const val ShakeMillis = 360
const val ShakeOscillations = 3
val ShakeSubtleAmplitude = 6.dp
val ShakeStrongAmplitude = 14.dp
const val ErrorTintMillis = 520
const val ErrorTintPeakAlpha = 0.28f
val ErrorTintCorner = 16.dp

// AC sweeps the old content away.
const val ClearSweepMillis = 260
const val ClearSweepMinLength = 2
val ClearSweepDistance = 48.dp

// Particle burst from the = button. Never more than 24.
const val ParticleCount = 20
const val ParticleLifeMillis = 700
const val ParticleSpreadDegrees = 70f
val ParticleMinSpeed = 120.dp
val ParticleMaxSpeed = 320.dp
val ParticleGravity = 360.dp
val ParticleMinRadius = 3.dp
val ParticleMaxRadius = 6.dp

// Background glow drift: one full back-and-forth cycle.
const val BackgroundDriftMillis = 18_000
val BackgroundDriftEasing = LinearEasing
