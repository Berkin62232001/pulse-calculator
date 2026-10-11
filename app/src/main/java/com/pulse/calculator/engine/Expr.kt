package com.pulse.calculator.engine

import java.math.BigDecimal

sealed interface Expr {
    data class Number(val value: BigDecimal) : Expr
    data class Negate(val operand: Expr) : Expr
    data class Percent(val operand: Expr) : Expr
    data class Binary(val op: Op, val left: Expr, val right: Expr) : Expr
}
