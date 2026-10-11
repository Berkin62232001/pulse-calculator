package com.pulse.calculator.state

import com.pulse.calculator.engine.CalculatorEngine
import com.pulse.calculator.engine.ErrorReason
import com.pulse.calculator.engine.EvalResult
import com.pulse.calculator.engine.NumberFormatter
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Edge cases found while auditing engine/ and state/ in M8: very long numbers, deep nesting, repeated %, backspacing results. */
class EdgeCasesTest {

    private fun viewModel(keys: String) = CalculatorViewModel().also { vm -> keyActions(keys).forEach(vm::onAction) }

    private fun evaluate(input: String) = CalculatorEngine.evaluate(input)

    private fun display(input: String): String =
        NumberFormatter.format((evaluate(input) as EvalResult.Success).value)

    // Very long numbers

    @Test
    fun aNinetyNineDigitNumberIsHandled() {
        val value = (evaluate("9".repeat(99)) as EvalResult.Success).value
        assertEquals(BigDecimal("9".repeat(99)), value)
        assertEquals("1E99", NumberFormatter.format(value))
    }

    @Test
    fun theLargestProductThatFitsIsExactlyRepresentable() {
        assertEquals("1E99", display("9".repeat(49) + "*" + "9".repeat(50)))
    }

    @Test
    fun longNumbersTypedOnTheKeypadStayWithinTheLimit() {
        val vm = viewModel("9".repeat(120))
        assertEquals(100, vm.state.value.expression.length)
    }

    @Test
    fun aVeryLongResultCanStillBeBackspaced() {
        val vm = viewModel("9".repeat(49) + "*" + "9".repeat(49) + "=")
        val before = vm.state.value.expression.length
        keyActions("<").forEach(vm::onAction)
        assertEquals(before - 1, vm.state.value.expression.length)
        assertFalse(vm.state.value.isResult)
    }

    @Test
    fun divisionStaysWithinThirtyFourDigitsAndRoundsTheDisplay() {
        assertEquals("0.333333333333", display("1/3"))
        assertEquals("1", display("1/3*3"))
        assertEquals("1", display("2/3+1/3"))
        assertEquals("0.1", display("1-0.9"))
    }

    // Nested parentheses

    @Test
    fun fortyNineNestedParenthesesEvaluate() {
        assertEquals("1", display("(".repeat(49) + "1" + ")".repeat(49)))
    }

    @Test
    fun unclosedNestedParenthesesAreIncomplete() {
        assertEquals(EvalResult.Incomplete, evaluate("(".repeat(50) + "1"))
    }

    @Test
    fun deeplyNestedUnaryMinusesFold() {
        assertEquals("5", display("-(-(-(-(5))))"))
        assertEquals("5", display("-".repeat(40) + "5"))
    }

    @Test
    fun thirtyNestedParenthesesTypedOnTheKeypadEvaluate() {
        val vm = viewModel("(".repeat(30) + "5" + ")".repeat(30) + "=")
        assertEquals("5", vm.state.value.expression)
    }

    @Test
    fun closingMoreParenthesesThanWereOpenedIsIgnored() {
        val vm = viewModel("((5" + ")".repeat(10))
        assertEquals("((5))", vm.state.value.expression)
    }

    @Test
    fun implicitMultiplicationChainsEvaluate() {
        val vm = viewModel("2(3)(4)5=")
        assertEquals("120", vm.state.value.expression)
    }

    // Repeated %

    @Test
    fun repeatedPercentDividesByOneHundredEachTime() {
        assertEquals("0.05", display("5%"))
        assertEquals("0.0005", display("5%%"))
        assertEquals("5E-40", display("5" + "%".repeat(20)))
        assertEquals("0.0005", display("(5%)%"))
        assertEquals("1", display("200*50%%"))
    }

    @Test
    fun ninetyNinePercentSignsDoNotOverflowOrCrash() {
        assertEquals("1E-198", display("1" + "%".repeat(99)))
    }

    @Test
    fun percentOnItsOwnIsMalformed() {
        assertEquals(EvalResult.Error(ErrorReason.Malformed), evaluate("%%"))
    }

    @Test
    fun repeatedPercentFromTheKeypadKeepsEveryPercent() {
        val vm = viewModel("5%%%%%%%%%%")
        assertEquals("5%%%%%%%%%%", vm.state.value.expression)
        assertTrue(vm.state.value.preview != null)
    }

    // Backspace through results

    @Test
    fun backspacingABigResultTurnsItIntoAnEditableNumber() {
        val vm = viewModel("999999999999*10=")
        assertEquals("9999999999990", vm.state.value.expression)
        keyActions("<<<").forEach(vm::onAction)
        assertEquals("9999999999", vm.state.value.expression)
        assertFalse(vm.state.value.isResult)
    }

    @Test
    fun backspacingAFractionalResultAllTheWayDown() {
        val vm = viewModel("1/3=")
        keyActions("<".repeat(14)).forEach(vm::onAction)
        assertEquals("", vm.state.value.expression)
    }

    @Test
    fun backspacingANegativeResultLeavesTheSignThenNothing() {
        val vm = viewModel("0-5=")
        assertEquals("-5", vm.state.value.expression)
        keyActions("<").forEach(vm::onAction)
        assertEquals("-", vm.state.value.expression)
        keyActions("<").forEach(vm::onAction)
        assertEquals("", vm.state.value.expression)
    }

    @Test
    fun typingAfterBackspacingAResultContinuesTheNumber() {
        val vm = viewModel("2+3=<7")
        assertEquals("7", vm.state.value.expression)
        keyActions("*2=").forEach(vm::onAction)
        assertEquals("14", vm.state.value.expression)
    }

    @Test
    fun aTinyResultContinuesAsAPlainDecimal() {
        val vm = viewModel("0.0000000001*1=")
        keyActions("*5=").forEach(vm::onAction)
        assertEquals("0.0000000005", vm.state.value.expression)
    }

    @Test
    fun backspaceOnAnErrorKeepsEditingTheExpression() {
        val vm = viewModel("5/0=")
        assertEquals(ErrorReason.DivideByZero, vm.state.value.error)
        keyActions("<").forEach(vm::onAction)
        assertEquals("5/", vm.state.value.expression)
        assertEquals(null, vm.state.value.error)
    }

    // Far beyond the 100-character input limit: the engine must still never throw.

    @Test
    fun absurdlyDeepInputIsMalformedNotACrash() {
        assertEquals(
            EvalResult.Error(ErrorReason.Malformed),
            evaluate("(".repeat(200_000) + "1" + ")".repeat(200_000)),
        )
        assertEquals(EvalResult.Error(ErrorReason.Malformed), evaluate("-".repeat(1_000_000) + "5"))
        assertTrue(evaluate("1" + "%".repeat(100_000)) is EvalResult.Error)
    }
}
