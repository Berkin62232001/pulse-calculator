package com.pulse.calculator.engine

import java.math.BigDecimal

enum class Op { Add, Subtract, Multiply, Divide }

sealed interface Token {
    data class Number(val value: BigDecimal) : Token
    data class Operator(val op: Op) : Token
    data object LParen : Token
    data object RParen : Token
    data object Percent : Token

    /** A lone `.` at the end of the input: not a number yet, but one more digit would make it one. */
    data object PartialNumber : Token
}
