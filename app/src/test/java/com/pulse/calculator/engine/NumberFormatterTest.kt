package com.pulse.calculator.engine

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** PRODUCT_SPEC section 6, with grouping on (the default). */
@RunWith(Parameterized::class)
class NumberFormatterTest(private val value: String, private val expected: String) {

    @Test
    fun formatsForDisplay() {
        assertEquals("value $value", expected, NumberFormatter.format(BigDecimal(value)))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun cases(): List<Array<String>> = listOf(
            // Thousands grouping.
            arrayOf("999", "999"),
            arrayOf("1000", "1,000"),
            arrayOf("1234567", "1,234,567"),
            arrayOf("-1234567", "-1,234,567"),
            arrayOf("1234567.891", "1,234,567.891"),
            arrayOf("-1234.5", "-1,234.5"),
            // Trailing zeros.
            arrayOf("2.50", "2.5"),
            arrayOf("10.000", "10"),
            arrayOf("100", "100"),
            arrayOf("1E+2", "100"),
            // Twelve significant digits, half-up.
            arrayOf("0.333333333333333333", "0.333333333333"),
            arrayOf("0.666666666666666666", "0.666666666667"),
            arrayOf("0.1234567890125", "0.123456789013"),
            arrayOf("0.1234567890124", "0.123456789012"),
            arrayOf("123456789012.5", "123,456,789,013"),
            arrayOf("0.9999999999999", "1"),
            // Scientific notation boundaries.
            arrayOf("999999999999", "999,999,999,999"),
            arrayOf("1000000000000", "1E12"),
            arrayOf("9999999999990", "9.99999999999E12"),
            arrayOf("1.2345E15", "1.2345E15"),
            arrayOf("-1.2345E15", "-1.2345E15"),
            arrayOf("0.000000001", "0.000000001"),
            arrayOf("0.0000000009", "9E-10"),
            arrayOf("0.0000000001", "1E-10"),
            arrayOf("-0.0000000001", "-1E-10"),
            // Rounding decides the notation (M3 plan, question 3).
            arrayOf("999999999999.5", "1E12"),
            arrayOf("9.9999999999999E-10", "0.000000001"),
            // Zero is never signed.
            arrayOf("0", "0"),
            arrayOf("0.000", "0"),
            arrayOf("-0.0", "0"),
            arrayOf("-0", "0"),
        )
    }
}

class NumberFormatterGroupingOffTest {

    @Test
    fun groupingCanBeDisabled() {
        assertEquals("1234567", NumberFormatter.format(BigDecimal("1234567"), grouping = false))
        assertEquals("-1234567.5", NumberFormatter.format(BigDecimal("-1234567.5"), grouping = false))
    }

    @Test
    fun scientificNotationIsNeverGrouped() {
        assertEquals("9.99999999999E12", NumberFormatter.format(BigDecimal("9999999999990"), grouping = false))
        assertEquals("9.99999999999E12", NumberFormatter.format(BigDecimal("9999999999990"), grouping = true))
    }
}
