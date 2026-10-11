package com.pulse.calculator.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * TEST_PLAN section 3 (the first block) plus the input rules of PRODUCT_SPEC sections 3 and 5
 * as agreed in the M4 plan. [keys] uses the shorthand of [keyAction].
 */
@RunWith(Parameterized::class)
class InputEditorTest(
    private val start: String,
    private val startIsResult: Boolean,
    private val keys: String,
    private val expected: String,
    private val expectedIsResult: Boolean,
) {

    @Test
    fun appliesKeys() {
        var state = CalculatorState(expression = start, isResult = startIsResult)
        for (action in keyActions(keys)) {
            val edit = InputEditor.apply(state, action)
            state = state.copy(expression = edit.expression, isResult = edit.isResult)
        }
        assertEquals("start '$start' keys '$keys'", expected, state.expression)
        assertEquals("isResult after '$keys' from '$start'", expectedIsResult, state.isResult)
    }

    companion object {
        private fun typing(start: String, keys: String, expected: String) =
            arrayOf<Any>(start, false, keys, expected, false)

        private fun fromResult(start: String, keys: String, expected: String, expectedIsResult: Boolean = false) =
            arrayOf<Any>(start, true, keys, expected, expectedIsResult)

        @JvmStatic
        @Parameterized.Parameters(name = "[{0}] + {2} -> [{3}]")
        fun cases(): List<Array<Any>> = listOf(
            // TEST_PLAN section 3.
            typing("", "007", "7"),
            typing("", ".", "0."),
            typing("1.5", ".", "1.5"),
            typing("5+", "*", "5*"),
            typing("5*", "-", "5*-"),
            typing("", "-", "-"),
            typing("2", "(", "2*("),
            typing("(2)", "(", "(2)*("),
            typing("2*(", "<", "2"),
            typing("5+", ")", "5+"),
            typing("(5+", ")", "(5+"),
            typing("(5", ")", "(5)"),
            fromResult("17", "+", "17+"),
            fromResult("17", "3", "3"),
            fromResult("17", "<", "1"),

            // Digits and leading zeros.
            typing("", "0", "0"),
            typing("0", "0", "0"),
            typing("0", "5", "5"),
            typing("10+0", "5", "10+5"),
            typing("1.0", "5", "1.05"),
            typing("(", "0", "(0"),
            typing("(2)", "3", "(2)*3"),
            typing("50%", "3", "50%*3"),

            // Decimal point.
            typing("5+", ".", "5+0."),
            typing("5.", ".", "5."),
            typing("(", ".", "(0."),
            typing("5*-", ".", "5*-0."),
            typing("(2)", ".", "(2)*0."),
            typing("12", ".", "12."),

            // Binary operators: append, replace, ignore.
            typing("5", "+", "5+"),
            typing("5+", "-", "5-"),
            typing("5-", "-", "5-"),
            typing("5+", "/", "5/"),
            typing("5*", "+", "5+"),
            typing("5/", "-", "5/-"),
            typing("5*-", "+", "5+"),
            typing("5*-", "*", "5*"),
            typing("5*-", "-", "5*-"),
            typing("", "+", ""),
            typing("", "*", ""),
            typing("", "/", ""),
            typing("(", "+", "("),
            typing("(", "*", "("),
            typing("(", "-", "(-"),
            typing("(-", "-", "(-"),
            typing("(-", "+", "(-"),
            typing("-", "+", "-"),
            typing("-", "-", "-"),
            typing("(2)", "+", "(2)+"),
            typing("50%", "*", "50%*"),

            // Parentheses.
            typing("", "(", "("),
            typing("5+", "(", "5+("),
            typing("-", "(", "-("),
            typing("50%", "(", "50%*("),
            typing("5", ")", "5"),
            typing("(", ")", "("),
            typing("((5)", ")", "((5))"),
            typing("(5%", ")", "(5%)"),
            typing("(5)", ")", "(5)"),
            typing("(5.", ")", "(5.)"),

            // Percent.
            typing("5", "%", "5%"),
            typing("5%", "%", "5%%"),
            typing("(5)", "%", "(5)%"),
            typing("", "%", ""),
            typing("5+", "%", "5+"),
            typing("(", "%", "("),

            // Backspace.
            typing("", "<", ""),
            typing("12", "<", "1"),
            typing("(2)*(", "<", "(2)"),
            typing("5+(", "<", "5+"),
            typing("5*-", "<", "5*"),
            typing("5*3", "<", "5*"),
            typing("12+5", "<<<", "1"),

            // AC.
            typing("12+5*(3", "C", ""),

            // After a result.
            fromResult("17", "*", "17*"),
            fromResult("17", "-", "17-"),
            fromResult("17", "%", "17%"),
            fromResult("17", ".", "0."),
            fromResult("17", "(", "("),
            fromResult("17", ")", "17", expectedIsResult = true),
            fromResult("-5", "+", "-5+"),
            fromResult("17", "C", ""),
            fromResult("17", "35", "35"),
            fromResult("0.5", "<", "0."),

            // Rapid sequences through several rules at once.
            typing("", "12+5*(3+2)", "12+5*(3+2)"),
            typing("", "2(3)", "2*(3)"),
            typing("", "((1+2)", "((1+2)"),
        )
    }
}

class InputEditorLimitTest {

    private fun apply(expression: String, key: Char) =
        InputEditor.apply(CalculatorState(expression = expression), keyAction(key))

    @Test
    fun digitAtMaxLengthIsIgnoredWithShake() {
        val full = "1".repeat(100)
        val edit = apply(full, '1')
        assertEquals(full, edit.expression)
        assertTrue(edit.hitLimit)
    }

    @Test
    fun autoInsertedMultiplyCountsTowardTheLimit() {
        val almostFull = "1".repeat(99)
        val edit = apply(almostFull, '(')
        assertEquals(almostFull, edit.expression)
        assertTrue(edit.hitLimit)
    }

    @Test
    fun inputThatFitsExactlyIsAccepted() {
        val edit = apply("1".repeat(99), '+')
        assertEquals("1".repeat(99) + "+", edit.expression)
        assertFalse(edit.hitLimit)
    }

    @Test
    fun leadingZeroCollapseDoesNotGrowTheExpression() {
        val full = "(" + "1+".repeat(49) + "0"
        assertEquals(100, full.length)
        val edit = apply(full, '5')
        assertEquals("(" + "1+".repeat(49) + "5", edit.expression)
        assertFalse(edit.hitLimit)
    }

    @Test
    fun backspaceAndClearWorkAtMaxLength() {
        val full = "1".repeat(100)
        assertEquals("1".repeat(99), apply(full, '<').expression)
        assertEquals("", apply(full, 'C').expression)
    }

    @Test
    fun ignoredInputAtMaxLengthDoesNotShake() {
        val full = "1".repeat(100)
        assertFalse(apply(full, ')').hitLimit)

        val fullWithDot = "1." + "1".repeat(98)
        assertEquals(fullWithDot, apply(fullWithDot, '.').expression)
        assertFalse(apply(fullWithDot, '.').hitLimit)
    }

    @Test
    fun loadHistoryIsNotLimitedByTheTypingLimit() {
        val long = "9".repeat(60) + "*" + "9".repeat(60)
        val edit = InputEditor.apply(CalculatorState(), CalculatorAction.LoadHistory(long))
        assertEquals(long, edit.expression)
        assertFalse(edit.hitLimit)
    }

    @Test
    fun equalsAndClearHistoryDoNotTouchTheExpression() {
        val state = CalculatorState(expression = "5+3", isResult = false)
        assertEquals("5+3", InputEditor.apply(state, CalculatorAction.Equals).expression)
        assertEquals("5+3", InputEditor.apply(state, CalculatorAction.ClearHistory).expression)
    }
}
