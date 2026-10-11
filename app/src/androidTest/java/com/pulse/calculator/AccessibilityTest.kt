package com.pulse.calculator

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulse.calculator.ui.TestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** PRODUCT_SPEC section 10: results and errors are announced, and every gesture has an accessible equivalent. */
class AccessibilityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun tap(vararg labels: String) =
        labels.forEach { composeRule.onNodeWithTag(TestTags.key(it)).performClick() }

    private fun frame() = composeRule.mainClock.advanceTimeByFrame()

    private fun liveRegionOfExpression() =
        composeRule.onNodeWithTag(TestTags.Expression)
            .fetchSemanticsNode().config.getOrNull(SemanticsProperties.LiveRegion)

    private fun shownExpression(): String =
        composeRule.onNodeWithTag(TestTags.Expression)
            .fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)
            .orEmpty().joinToString("") { it.text }

    @Test
    fun theConfirmedResultIsAnnouncedPolitelyButTypingIsNot() {
        composeRule.mainClock.autoAdvance = false

        tap("2", "+", "3")
        frame()
        assertNull("typing must not be announced", liveRegionOfExpression())

        tap("=")
        frame()
        assertEquals(LiveRegionMode.Polite, liveRegionOfExpression())
        assertEquals("5", shownExpression())
    }

    @Test
    fun theResultStopsBeingALiveRegionOnceItIsEdited() {
        composeRule.mainClock.autoAdvance = false

        tap("2", "+", "3", "=")
        frame()
        tap("+")
        frame()

        assertNull(liveRegionOfExpression())
    }

    @Test
    fun anErrorIsAnnouncedAssertively() {
        composeRule.mainClock.autoAdvance = false

        tap("8", "÷", "0", "=")
        repeat(30) { frame() }

        val node = composeRule.onNodeWithText("Can't divide by zero").fetchSemanticsNode()
        assertEquals(LiveRegionMode.Assertive, node.config.getOrNull(SemanticsProperties.LiveRegion))
    }

    @Test
    fun backspaceOffersClearAllAsAnAccessibilityAction() {
        composeRule.mainClock.autoAdvance = false
        tap("1", "2", "+", "5")
        frame()

        val node = composeRule.onNodeWithContentDescription("backspace").fetchSemanticsNode()
        val action = node.config.getOrNull(SemanticsActions.CustomActions)
            .orEmpty().firstOrNull { it.label == "Clear all" }
        assertNotNull("backspace has no \"Clear all\" action", action)

        composeRule.runOnUiThread { action!!.action() }
        frame()

        assertEquals("0", shownExpression())
    }
}
