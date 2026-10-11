package com.pulse.calculator

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulse.calculator.ui.TestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The clock is frozen in every test, so no animation ever progresses: the displayed values must
 * already be correct, because animations are presentation only and never gate the state.
 */
class DisplayAnimationsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun tap(vararg labels: String) =
        labels.forEach { composeRule.onNodeWithTag(TestTags.key(it)).performClick() }

    private fun shownExpression(): String =
        composeRule.onNodeWithTag(TestTags.Expression)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .joinToString("") { it.text }

    @Test
    fun resultIsShownImmediatelyWhileAnimationsHaveNotStarted() {
        composeRule.mainClock.autoAdvance = false

        tap("2", "+", "3", "×", "4", "=")
        composeRule.mainClock.advanceTimeByFrame()

        assertEquals("14", shownExpression())
    }

    @Test
    fun errorMessageIsShownImmediately() {
        composeRule.mainClock.autoAdvance = false

        tap("8", "÷", "0", "=")
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onNodeWithText("Can't divide by zero").assertIsDisplayed()
        assertEquals("8 ÷ 0", shownExpression())
    }

    @Test
    fun allClearEmptiesTheDisplayImmediately() {
        composeRule.mainClock.autoAdvance = false

        tap("1", "2", "+", "5", "AC")
        composeRule.mainClock.advanceTimeByFrame()

        assertEquals("0", shownExpression())
    }

    @Test
    fun hammeringTheKeypadLosesNothing() {
        composeRule.mainClock.autoAdvance = false

        repeat(4) { tap("9", "+", "9", "−") }
        tap("9")
        composeRule.mainClock.advanceTimeByFrame()

        assertEquals("9 + 9 − ".repeat(4) + "9", shownExpression())
    }
}
