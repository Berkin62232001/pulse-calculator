package com.pulse.calculator.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReducedMotionTest {

    @Test
    fun animationsOffMeansReducedMotion() {
        assertTrue(isReducedMotion(0f))
    }

    @Test
    fun anyNonZeroDurationScaleKeepsAnimations() {
        assertFalse(isReducedMotion(1f))
        assertFalse(isReducedMotion(0.5f))
        assertFalse(isReducedMotion(10f))
    }
}
