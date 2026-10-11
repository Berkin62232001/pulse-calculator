package com.pulse.calculator

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulse.calculator.ui.TestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The cases TEST_PLAN sections 5 and 6 ask for that no other test covered:
 * live preview in the UI, a description on every button, and rotation.
 */
class TestPlanGapsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun tap(vararg labels: String) =
        labels.forEach { composeRule.onNodeWithTag(TestTags.key(it)).performClick() }

    /** Stepped with a short real pause: the history sheet lives in its own window. */
    private fun settle() = repeat(60) {
        composeRule.mainClock.advanceTimeByFrame()
        Thread.sleep(16)
    }

    private fun shownExpression(): String =
        composeRule.onNodeWithTag(TestTags.Expression)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .joinToString("") { it.text }

    @Test
    fun previewShowsTwentyBeforeEquals() {
        composeRule.mainClock.autoAdvance = false

        tap("(", "2", "+", "3", ")", "×", "4")
        settle()

        composeRule.onNodeWithText("20").assertIsDisplayed()
        assertEquals("(2 + 3) × 4", shownExpression())
    }

    @Test
    fun everyButtonHasANonEmptyContentDescription() {
        composeRule.mainClock.autoAdvance = false

        val clickable = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()

        // 20 keys, backspace and the history button.
        assertTrue("expected at least 22 buttons, found ${clickable.size}", clickable.size >= 22)
        clickable.forEach { node ->
            val description = node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            assertTrue("a button has no content description: ${node.config}", description.any { it.isNotBlank() })
        }
    }

    @Test
    fun rotationKeepsTheExpressionAndThePreview() {
        composeRule.mainClock.autoAdvance = false

        tap("1", "2", "+", "5")
        settle()
        composeRule.activityRule.scenario.recreate()
        settle()

        assertEquals("12 + 5", shownExpression())
        composeRule.onNodeWithText("17").assertIsDisplayed()
    }

    @Test
    fun rotationKeepsTheHistory() {
        composeRule.mainClock.autoAdvance = false

        tap("1", "2", "+", "5", "=")
        settle()
        composeRule.activityRule.scenario.recreate()
        settle()
        composeRule.onNodeWithTag(TestTags.HistoryButton).performClick()
        settle()

        assertEquals("17", shownExpression())
        composeRule.onNodeWithTag(TestTags.historyRow(0)).assertIsDisplayed()
        composeRule.onNodeWithText("12 + 5").assertIsDisplayed()
    }
}
