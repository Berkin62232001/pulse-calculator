package com.pulse.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** BigDecimal -> display string, following PRODUCT_SPEC section 6. */
object NumberFormatter {

    private val SignificantDigits = MathContext(12, RoundingMode.HALF_UP)
    private val ScientificUpper = BigDecimal.TEN.pow(12)
    private val ScientificLower = BigDecimal("1E-9")

    /** [grouping] adds thousands separators; it is for display only and never applies to scientific notation. */
    fun format(value: BigDecimal, grouping: Boolean = true): String {
        if (value.signum() == 0) return "0"
        // Rounding comes first so the notation is chosen from the value that will actually be shown.
        val rounded = value.round(SignificantDigits).stripTrailingZeros()
        val magnitude = rounded.abs()
        return if (magnitude >= ScientificUpper || magnitude < ScientificLower) {
            scientific(rounded)
        } else {
            plain(rounded, grouping)
        }
    }

    private fun scientific(value: BigDecimal): String {
        val digits = value.unscaledValue().abs().toString()
        val exponent = digits.length - value.scale() - 1
        val mantissa = if (digits.length == 1) digits else "${digits[0]}.${digits.substring(1)}"
        val sign = if (value.signum() < 0) "-" else ""
        return "$sign${mantissa}E$exponent"
    }

    private fun plain(value: BigDecimal, grouping: Boolean): String {
        val text = value.toPlainString()
        if (!grouping) return text
        val sign = if (text.startsWith("-")) "-" else ""
        val unsigned = text.removePrefix("-")
        val integerPart = unsigned.substringBefore('.')
        val fraction = unsigned.substring(integerPart.length)
        return sign + group(integerPart) + fraction
    }

    private fun group(integerPart: String): String =
        integerPart.reversed().chunked(3).joinToString(",").reversed()
}
