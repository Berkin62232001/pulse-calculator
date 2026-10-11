package com.pulse.calculator.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color

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

// Background glow drift: one full back-and-forth cycle.
const val BackgroundDriftMillis = 18_000
val BackgroundDriftEasing = LinearEasing
