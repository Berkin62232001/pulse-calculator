package com.pulse.calculator

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pulse.calculator.ui.TestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RapidTypingTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * The clock is frozen, so no press animation or background drift ever progresses between taps:
     * input must register on the tap itself and never wait for an animation.
     */
    @Test
    fun thirtyRapidDigitsAreAllRegisteredInOrder() {
        composeRule.mainClock.autoAdvance = false
        val digits = "1234567890".repeat(3)

        digits.forEach { digit ->
            composeRule.onNodeWithTag(TestTags.key(digit.toString())).performClick()
        }
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals(digits, shownExpression().replace(",", ""))
    }

    private fun shownExpression(): String =
        composeRule.onNodeWithTag(TestTags.Expression)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .joinToString("") { it.text }
}
