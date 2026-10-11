package com.pulse.calculator.state

import com.pulse.calculator.engine.ErrorReason
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** TEST_PLAN sections 4 (preview stays hidden in M4) and 5. */
@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    private fun CalculatorViewModel.press(keys: String) = keyActions(keys).forEach(::onAction)

    private fun TestScope.collectEvents(viewModel: CalculatorViewModel): List<UiEvent> {
        val events = mutableListOf<UiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }
        return events
    }

    @Test
    fun equalsReplacesExpressionWithResultAndAddsHistory() = runTest {
        val viewModel = CalculatorViewModel()
        val events = collectEvents(viewModel)

        viewModel.press("12+5=")

        val state = viewModel.state.value
        assertEquals("17", state.expression)
        assertTrue(state.isResult)
        assertNull(state.error)
        assertEquals(listOf(HistoryEntry(0, "12+5", "17")), state.history)
        assertEquals(listOf<UiEvent>(UiEvent.ResultConfirmed), events)
    }

    @Test
    fun precedenceIsAppliedThroughTheKeypad() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("2+3*4=")
        assertEquals("14", viewModel.state.value.expression)
    }

    @Test
    fun percentEvaluates() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("50%=")
        assertEquals("0.5", viewModel.state.value.expression)
    }

    @Test
    fun historyStoresTheGroupedResult() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("1000*1000=")
        assertEquals("1,000,000", viewModel.state.value.history.single().result)
        assertEquals("1000000", viewModel.state.value.expression)
    }

    @Test
    fun incompleteExpressionShakesAndChangesNothing() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("12+")
        val events = collectEvents(viewModel)

        viewModel.press("=")

        val state = viewModel.state.value
        assertEquals("12+", state.expression)
        assertFalse(state.isResult)
        assertNull(state.error)
        assertTrue(state.history.isEmpty())
        assertEquals(listOf<UiEvent>(UiEvent.Shake), events)
    }

    @Test
    fun divideByZeroKeepsExpressionShowsErrorAndSkipsHistory() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("8/0")
        val events = collectEvents(viewModel)

        viewModel.press("=")

        val state = viewModel.state.value
        assertEquals("8/0", state.expression)
        assertEquals(ErrorReason.DivideByZero, state.error)
        assertFalse(state.isResult)
        assertTrue(state.history.isEmpty())
        assertEquals(listOf<UiEvent>(UiEvent.ErrorShown), events)
    }

    @Test
    fun nextInputClearsTheErrorAndAppliesNormally() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("8/0=")

        viewModel.press("5")

        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("8/5", state.expression)
    }

    @Test
    fun clearAlsoClearsTheError() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("8/0=C")
        assertNull(viewModel.state.value.error)
        assertEquals("", viewModel.state.value.expression)
    }

    @Test
    fun overflowShowsAnError() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.onAction(CalculatorAction.LoadHistory("9".repeat(60) + "*" + "9".repeat(60)))

        viewModel.press("=")

        assertEquals(ErrorReason.Overflow, viewModel.state.value.error)
        assertTrue(viewModel.state.value.history.isEmpty())
    }

    @Test
    fun equalsOnASingleNumberOrOnAResultIsANoOp() = runTest {
        val viewModel = CalculatorViewModel()
        val events = collectEvents(viewModel)

        viewModel.press("=")
        viewModel.press("42=")
        viewModel.press("C-42=")
        viewModel.press("C1+1==")

        assertEquals("2", viewModel.state.value.expression)
        assertEquals(1, viewModel.state.value.history.size)
        assertEquals(listOf<UiEvent>(UiEvent.ResultConfirmed), events)
    }

    @Test
    fun operatorContinuesFromTheResult() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("8+9=*2")
        assertEquals("17*2", viewModel.state.value.expression)
        assertFalse(viewModel.state.value.isResult)

        viewModel.press("=")
        assertEquals("34", viewModel.state.value.expression)
    }

    @Test
    fun digitAfterAResultStartsANewExpression() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("8+9=3")
        assertEquals("3", viewModel.state.value.expression)
        assertFalse(viewModel.state.value.isResult)
    }

    @Test
    fun backspaceEditsTheResultAsANormalNumber() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("8+9=<")
        assertEquals("1", viewModel.state.value.expression)
        assertFalse(viewModel.state.value.isResult)
    }

    @Test
    fun scientificResultsContinueAsPlainDecimals() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("0.0000000001*1=")
        assertEquals("0.0000000001", viewModel.state.value.expression)

        viewModel.press("*5=")
        assertEquals("0.0000000005", viewModel.state.value.expression)

        viewModel.press("C999999999999*10=")
        assertEquals("9999999999990", viewModel.state.value.expression)
        assertEquals("9.99999999999E12", viewModel.state.value.history.first().result)
    }

    @Test
    fun resultsUseTheRoundedDisplayedValue() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("1/3=")
        assertEquals("0.333333333333", viewModel.state.value.expression)
    }

    @Test
    fun clearKeepsHistory() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("1+1=C")
        assertEquals("", viewModel.state.value.expression)
        assertEquals(1, viewModel.state.value.history.size)
    }

    @Test
    fun historyIsNewestFirstAndCappedAtFifty() = runTest {
        val viewModel = CalculatorViewModel()

        repeat(51) { viewModel.press("1+1=") }

        val history = viewModel.state.value.history
        assertEquals(50, history.size)
        assertEquals(50L, history.first().id)
        assertEquals(1L, history.last().id)
    }

    @Test
    fun loadHistoryPutsTheExpressionBackInTheEditor() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("12+5=")
        val entry = viewModel.state.value.history.first()

        viewModel.onAction(CalculatorAction.LoadHistory(entry.expression))

        val state = viewModel.state.value
        assertEquals("12+5", state.expression)
        assertFalse(state.isResult)
        assertEquals(1, state.history.size)
    }

    @Test
    fun clearHistoryEmptiesOnlyTheHistory() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("1+1=")

        viewModel.onAction(CalculatorAction.ClearHistory)

        assertTrue(viewModel.state.value.history.isEmpty())
        assertEquals("2", viewModel.state.value.expression)
    }

    @Test
    fun typingPastTheLimitEmitsShake() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("1".repeat(100))
        val events = collectEvents(viewModel)

        viewModel.press("1")

        assertEquals(100, viewModel.state.value.expression.length)
        assertEquals(listOf<UiEvent>(UiEvent.Shake), events)
    }

    @Test
    fun rapidTypingKeepsEveryDigitInOrder() = runTest {
        val viewModel = CalculatorViewModel()
        viewModel.press("1234567890".repeat(3))
        assertEquals("1234567890".repeat(3), viewModel.state.value.expression)
    }

    // TEST_PLAN section 4: live preview.

    private fun previewAfter(keys: String): String? {
        val viewModel = CalculatorViewModel()
        viewModel.press(keys)
        return viewModel.state.value.preview
    }

    @Test
    fun previewShowsTheResultOfACompleteExpression() {
        assertEquals("17", previewAfter("12+5"))
        assertEquals("14", previewAfter("2+3*4"))
        assertEquals("20", previewAfter("(2+3)*4"))
        assertEquals("0.05", previewAfter("5%"))
        assertEquals("3", previewAfter("-5+8"))
    }

    @Test
    fun previewIsGroupedLikeTheDisplay() {
        assertEquals("1,000,000", previewAfter("1000*1000"))
    }

    @Test
    fun incompleteExpressionsHaveNoPreview() {
        assertNull(previewAfter("12+"))
        assertNull(previewAfter("3*"))
        assertNull(previewAfter("(12+5"))
        assertNull(previewAfter("("))
        assertNull(previewAfter("5*-"))
    }

    @Test
    fun aSingleNumberHasNoPreview() {
        assertNull(previewAfter(""))
        assertNull(previewAfter("42"))
        assertNull(previewAfter("-42"))
        assertNull(previewAfter("4.5"))
        assertNull(previewAfter("(42)"))
    }

    @Test
    fun mathErrorsHideThePreviewSilently() {
        val viewModel = CalculatorViewModel()
        viewModel.press("8/0")
        assertNull(viewModel.state.value.preview)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun unclosedParenthesesAreNotAutoClosedForThePreview() {
        val viewModel = CalculatorViewModel()
        viewModel.press("12+5*(3+2")
        assertNull(viewModel.state.value.preview)

        viewModel.press(")")
        assertEquals("37", viewModel.state.value.preview)
    }

    @Test
    fun backspaceUpdatesThePreview() {
        val viewModel = CalculatorViewModel()
        viewModel.press("12+55")
        assertEquals("67", viewModel.state.value.preview)

        viewModel.press("<")
        assertEquals("17", viewModel.state.value.preview)

        viewModel.press("<")
        assertNull(viewModel.state.value.preview)

        viewModel.press("<<")
        assertNull(viewModel.state.value.preview)
    }

    @Test
    fun previewTracksEveryEdit() {
        val viewModel = CalculatorViewModel()
        val seen = "12+5*2".map { key ->
            viewModel.press(key.toString())
            viewModel.state.value.preview
        }
        assertEquals(listOf(null, null, null, "17", null, "22"), seen)
    }

    @Test
    fun previewDisappearsOnceTheResultIsConfirmed() {
        val viewModel = CalculatorViewModel()
        viewModel.press("12+5")
        assertEquals("17", viewModel.state.value.preview)

        viewModel.press("=")

        assertNull(viewModel.state.value.preview)
        assertEquals("17", viewModel.state.value.expression)
    }

    @Test
    fun errorOnEqualsLeavesNoPreview() {
        val viewModel = CalculatorViewModel()
        viewModel.press("8/0=")
        assertNull(viewModel.state.value.preview)
        assertEquals(ErrorReason.DivideByZero, viewModel.state.value.error)
    }

    @Test
    fun clearHidesThePreview() {
        val viewModel = CalculatorViewModel()
        viewModel.press("12+5C")
        assertNull(viewModel.state.value.preview)
    }

    @Test
    fun continuingFromAResultStartsPreviewingAgain() {
        val viewModel = CalculatorViewModel()
        viewModel.press("8+9=*2")
        assertEquals("34", viewModel.state.value.preview)
    }

    @Test
    fun loadingFromHistoryShowsItsPreview() {
        val viewModel = CalculatorViewModel()
        viewModel.onAction(CalculatorAction.LoadHistory("12+5"))
        assertEquals("17", viewModel.state.value.preview)
    }

    @Test
    fun overflowAndOtherFailuresHideThePreview() {
        val viewModel = CalculatorViewModel()
        viewModel.onAction(CalculatorAction.LoadHistory("9".repeat(60) + "*" + "9".repeat(60)))
        assertNull(viewModel.state.value.preview)
    }

    @Test
    fun signsAloneAreNotAnOperation() {
        val viewModel = CalculatorViewModel()
        viewModel.onAction(CalculatorAction.LoadHistory("--5"))
        assertNull(viewModel.state.value.preview)

        viewModel.onAction(CalculatorAction.LoadHistory("-(5)"))
        assertNull(viewModel.state.value.preview)

        viewModel.onAction(CalculatorAction.LoadHistory("5-3"))
        assertEquals("2", viewModel.state.value.preview)

        viewModel.onAction(CalculatorAction.LoadHistory("(2)-3"))
        assertEquals("-1", viewModel.state.value.preview)
    }

    // M7: history is mirrored in the SavedStateHandle so it survives rotation (never written to disk).

    private fun viewModelWith(handle: SavedStateHandle) = CalculatorViewModel(handle)

    @Test
    fun historySurvivesAViewModelRebuiltFromTheSameSavedState() {
        val handle = SavedStateHandle()
        val first = viewModelWith(handle)
        first.press("1+1=C2+2=C3+3=")

        val restored = viewModelWith(handle)

        assertEquals(first.state.value.history, restored.state.value.history)
        assertEquals(listOf("3+3", "2+2", "1+1"), restored.state.value.history.map { it.expression })
        assertEquals(listOf("6", "4", "2"), restored.state.value.history.map { it.result })
    }

    @Test
    fun restoredViewModelKeepsNumberingNewestFirst() {
        val handle = SavedStateHandle()
        viewModelWith(handle).press("1+1=C2+2=")

        val restored = viewModelWith(handle)
        restored.press("C3+3=")

        assertEquals(listOf(2L, 1L, 0L), restored.state.value.history.map { it.id })
        assertEquals("3+3", restored.state.value.history.first().expression)
    }

    @Test
    fun onlyTheHistoryIsRestoredNotTheExpression() {
        val handle = SavedStateHandle()
        viewModelWith(handle).press("1+1=C5+")

        val restored = viewModelWith(handle)

        assertEquals(1, restored.state.value.history.size)
        assertEquals("", restored.state.value.expression)
    }

    @Test
    fun clearingHistoryAlsoClearsTheSavedState() {
        val handle = SavedStateHandle()
        val first = viewModelWith(handle)
        first.press("1+1=")

        first.onAction(CalculatorAction.ClearHistory)

        assertTrue(viewModelWith(handle).state.value.history.isEmpty())
    }

    @Test
    fun restoringNeverExceedsTheCapAndKeepsTheNewest() {
        val handle = SavedStateHandle()
        handle[HistoryIdsKey] = LongArray(60) { (59 - it).toLong() }
        handle[HistoryExpressionsKey] = Array(60) { "e${59 - it}" }
        handle[HistoryResultsKey] = Array(60) { "r${59 - it}" }

        val history = viewModelWith(handle).state.value.history

        assertEquals(50, history.size)
        assertEquals(59L, history.first().id)
        assertEquals(10L, history.last().id)
    }

    @Test
    fun mismatchedSavedArraysAreTrimmedNotCrashed() {
        val handle = SavedStateHandle()
        handle[HistoryIdsKey] = longArrayOf(2, 1, 0)
        handle[HistoryExpressionsKey] = arrayOf("a", "b")
        handle[HistoryResultsKey] = arrayOf("x", "y", "z")

        val history = viewModelWith(handle).state.value.history

        assertEquals(listOf(HistoryEntry(2, "a", "x"), HistoryEntry(1, "b", "y")), history)
    }

    @Test
    fun savedStateOfTheWrongTypeIsIgnored() {
        val handle = SavedStateHandle()
        handle[HistoryIdsKey] = "not an array"

        assertTrue(viewModelWith(handle).state.value.history.isEmpty())
    }
}
