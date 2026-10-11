package com.pulse.calculator

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pulse.calculator.ui.TestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The clock is driven by hand: the background animates forever, so letting it run on its own
 * would never go idle. Time is advanced past every sheet animation explicitly.
 */
class HistorySheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun tap(vararg labels: String) =
        labels.forEach { composeRule.onNodeWithTag(TestTags.key(it)).performClick() }

    /**
     * The sheet is drawn in its own window, which needs a little real time to appear, so the
     * clock is stepped frame by frame with a short real pause instead of jumped in one go.
     */
    private fun settle() = repeat(SettleFrames) {
        composeRule.mainClock.advanceTimeByFrame()
        Thread.sleep(FrameMillis)
    }

    private companion object {
        const val SettleFrames = 90
        const val FrameMillis = 16L
    }

    private fun textOf(tag: String): String =
        composeRule.onNodeWithTag(tag)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .joinToString("|") { it.text }

    @Test
    fun openTapLoadsTheExpressionAndClosesTheSheet() {
        composeRule.mainClock.autoAdvance = false

        tap("1", "+", "1", "=")
        settle()
        composeRule.onNodeWithTag(TestTags.HistoryButton).performClick()
        settle()

        composeRule.onNodeWithTag(TestTags.HistorySheet).assertIsDisplayed()
        assertEquals("1 + 1|2", textOf(TestTags.historyRow(0)))

        composeRule.onNodeWithTag(TestTags.historyRow(0)).performClick()
        settle()

        composeRule.onNodeWithTag(TestTags.HistorySheet).assertDoesNotExist()
        assertEquals("1 + 1", textOf(TestTags.Expression))
    }

    @Test
    fun emptyHistoryShowsTheFriendlyEmptyState() {
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag(TestTags.HistoryButton).performClick()
        settle()

        composeRule.onNodeWithTag(TestTags.HistoryEmpty).assertIsDisplayed()
    }

    @Test
    fun clearHistoryEmptiesTheListAndKeepsTheSheetOpen() {
        composeRule.mainClock.autoAdvance = false

        tap("2", "+", "2", "=")
        settle()
        composeRule.onNodeWithTag(TestTags.HistoryButton).performClick()
        settle()
        composeRule.onNodeWithTag(TestTags.HistoryClear).performClick()
        settle()

        composeRule.onNodeWithTag(TestTags.HistoryEmpty).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.HistorySheet).assertIsDisplayed()
    }

    @Test
    fun newestEntryIsFirstInTheSheet() {
        composeRule.mainClock.autoAdvance = false

        tap("1", "+", "1", "=")
        tap("2", "+", "2", "=")
        settle()
        composeRule.onNodeWithTag(TestTags.HistoryButton).performClick()
        settle()

        assertEquals("2 + 2|4", textOf(TestTags.historyRow(0)))
        assertEquals("1 + 1|2", textOf(TestTags.historyRow(1)))
    }
}
