package com.pulse.calculator.ui.display

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * M8 finding B1: a confirmed result must be shown in the notation of PRODUCT_SPEC section 6
 * (12 significant digits, scientific for >= 1e12 and < 1e-9), not as the plain decimal that is
 * kept internally for editing. Anything being typed, including a result after backspace,
 * is shown as typed.
 */
@RunWith(Parameterized::class)
class ResultDisplayTest(private val internal: String, private val expected: String) {

    @Test
    fun aConfirmedResultUsesTheSpecNotation() {
        assertEquals("result '$internal'", expected, ExpressionFormat.toDisplay(internal, isResult = true))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "[{0}] -> [{1}]")
        fun cases(): List<Array<String>> = listOf(
            // TEST_PLAN section 1.
            arrayOf("9999999999990", "9.99999999999E12"),
            arrayOf("0.0000000001", "1E-10"),
            arrayOf("0.333333333333", "0.333333333333"),
            arrayOf("0.666666666667", "0.666666666667"),
            // Ordinary results are unchanged, with grouping.
            arrayOf("17", "17"),
            arrayOf("0", "0"),
            arrayOf("0.3", "0.3"),
            arrayOf("1234567", "1,234,567"),
            arrayOf("999999999999", "999,999,999,999"),
            // Scientific boundaries.
            arrayOf("1000000000000", "1E12"),
            arrayOf("0.000000001", "0.000000001"),
            arrayOf("0.0000000009", "9E-10"),
            arrayOf("1234500000000000", "1.2345E15"),
            // The sign is the true minus sign everywhere else in the display.
            arrayOf("-5", "−5"),
            arrayOf("-1234", "−1,234"),
            arrayOf("-0.0000000001", "−1E-10"),
            arrayOf("-9999999999990", "−9.99999999999E12"),
            // The longest result there can be.
            arrayOf("1" + "0".repeat(99), "1E99"),
            arrayOf("0." + "0".repeat(197) + "1", "1E-198"),
        )
    }
}

class TypedDisplayTest {

    @Test
    fun anExpressionBeingTypedIsNeverRewrittenInScientificNotation() {
        assertEquals("9,999,999,999,990", ExpressionFormat.toDisplay("9999999999990", isResult = false))
        assertEquals("0.0000000001", ExpressionFormat.toDisplay("0.0000000001", isResult = false))
        assertEquals("12 + 5", ExpressionFormat.toDisplay("12+5", isResult = false))
    }

    @Test
    fun anUnparseableResultFallsBackToTheTypedForm() {
        assertEquals("12 + 5", ExpressionFormat.toDisplay("12+5", isResult = true))
    }

    @Test
    fun anEmptyResultIsEmpty() {
        assertEquals("", ExpressionFormat.toDisplay("", isResult = true))
    }
}
