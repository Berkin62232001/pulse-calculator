package com.pulse.calculator.engine

import java.math.BigDecimal
import java.math.MathContext

/** Evaluates an [Expr] with exact decimal arithmetic; only division is rounded (DECIMAL128). */
object Evaluator {

    private val OverflowLimit: BigDecimal = BigDecimal.TEN.pow(100)

    private class Failure(val reason: ErrorReason) : Exception(null, null, false, false)

    fun evaluate(expr: Expr): EvalResult = try {
        val value = eval(expr)
        if (value.abs() > OverflowLimit) EvalResult.Error(ErrorReason.Overflow) else EvalResult.Success(value)
    } catch (failure: Failure) {
        EvalResult.Error(failure.reason)
    }

    private fun eval(expr: Expr): BigDecimal = when (expr) {
        is Expr.Number -> expr.value
        is Expr.Negate -> eval(expr.operand).negate()
        is Expr.Percent -> eval(expr.operand).movePointLeft(2)
        is Expr.Binary -> {
            val left = eval(expr.left)
            val right = eval(expr.right)
            when (expr.op) {
                Op.Add -> left.add(right)
                Op.Subtract -> left.subtract(right)
                Op.Multiply -> left.multiply(right)
                Op.Divide -> {
                    if (right.signum() == 0) throw Failure(ErrorReason.DivideByZero)
                    left.divide(right, MathContext.DECIMAL128)
                }
            }
        }
    }
}
