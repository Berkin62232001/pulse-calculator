package com.pulse.calculator

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.pulse.calculator.ui.TestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** PRODUCT_SPEC section 10: text respects the system font scale only up to a cap, everywhere. */
class FontScaleCapTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private fun shell(command: String) {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    private fun fontScaleOf(tag: String): Float =
        composeRule.onNodeWithTag(tag).fetchSemanticsNode().layoutInfo.density.fontScale

    @Test
    fun theMainScreenAndTheHistorySheetBothStayWithinTheCap() {
        shell("settings put system font_scale 2.0")
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                composeRule.mainClock.autoAdvance = false
                composeRule.onNodeWithTag(TestTags.key("1")).performClick()
                composeRule.onNodeWithTag(TestTags.key("+")).performClick()
                composeRule.onNodeWithTag(TestTags.key("1")).performClick()
                composeRule.onNodeWithTag(TestTags.key("=")).performClick()
                composeRule.onNodeWithTag(TestTags.HistoryButton).performClick()
                // The sheet is drawn in its own window, which needs a little real time to appear.
                repeat(60) {
                    composeRule.mainClock.advanceTimeByFrame()
                    Thread.sleep(16)
                }

                val screen = fontScaleOf(TestTags.Expression)
                val sheet = fontScaleOf(TestTags.HistorySheet)
                assertTrue("main screen font scale $screen is above the cap", screen <= MaxFontScale + Tolerance)
                assertTrue("history sheet font scale $sheet is above the cap", sheet <= MaxFontScale + Tolerance)
            }
        } finally {
            shell("settings put system font_scale 1.0")
        }
    }

    private companion object {
        const val MaxFontScale = 1.3f
        const val Tolerance = 0.001f
    }
}
