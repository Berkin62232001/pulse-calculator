package com.pulse.calculator.engine

import java.math.BigDecimal

enum class ErrorReason { DivideByZero, Overflow, Malformed }

sealed interface EvalResult {
    data class Success(val value: BigDecimal) : EvalResult

    /** Ends with an operator, has an unclosed paren, or is empty: more input could make it valid. */
    data object Incomplete : EvalResult

    data class Error(val reason: ErrorReason) : EvalResult
}
