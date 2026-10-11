package com.pulse.calculator.ui.display

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Internal expression -> what the user reads: × ÷ −, spaced binary operators, grouped numbers. */
@RunWith(Parameterized::class)
class ExpressionFormatTest(private val internal: String, private val expected: String) {

    @Test
    fun mapsToDisplayText() {
        assertEquals("expression '$internal'", expected, ExpressionFormat.toDisplay(internal))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "[{0}] -> [{1}]")
        fun cases(): List<Array<String>> = listOf(
            arrayOf("", ""),
            arrayOf("12+5*(3+2)", "12 + 5 × (3 + 2)"),
            arrayOf("12/4", "12 ÷ 4"),
            arrayOf("5-3", "5 − 3"),
            arrayOf("-5", "−5"),
            arrayOf("4*-2", "4 × −2"),
            arrayOf("(-3)", "(−3)"),
            arrayOf("(2)*(3)", "(2) × (3)"),
            arrayOf("12+", "12 +"),
            arrayOf("50%", "50%"),
            arrayOf("200*10%", "200 × 10%"),
            arrayOf("1234567", "1,234,567"),
            arrayOf("-1234", "−1,234"),
            arrayOf("1234.5678", "1,234.5678"),
            arrayOf("1000000.5", "1,000,000.5"),
            arrayOf("0.0000000001", "0.0000000001"),
            arrayOf("9999999999990", "9,999,999,999,990"),
            arrayOf("5.", "5."),
            arrayOf("1000+2000", "1,000 + 2,000"),
        )
    }
}
