package com.pulse.calculator.engine

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** TEST_PLAN section 2 plus the edge cases agreed in the M3 plan. */
@RunWith(Parameterized::class)
class EngineIncompleteVsErrorTest(private val input: String, private val expected: EvalResult) {

    @Test
    fun classifiesInput() {
        assertEquals("input '$input'", expected, CalculatorEngine.evaluate(input))
    }

    companion object {
        private val malformed = EvalResult.Error(ErrorReason.Malformed)
        private val divideByZero = EvalResult.Error(ErrorReason.DivideByZero)
        private val overflow = EvalResult.Error(ErrorReason.Overflow)

        @JvmStatic
        @Parameterized.Parameters(name = "[{0}]")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("", EvalResult.Incomplete),
            arrayOf("12+", EvalResult.Incomplete),
            arrayOf("(12+5", EvalResult.Incomplete),
            arrayOf("3*", EvalResult.Incomplete),
            arrayOf("(", EvalResult.Incomplete),
            arrayOf("-", EvalResult.Incomplete),
            arrayOf("12/0", divideByZero),
            arrayOf("5/(3-3)", divideByZero),
            arrayOf("1)", malformed),
            arrayOf("()", malformed),
            arrayOf("1..2", malformed),
            arrayOf("9".repeat(60) + "*" + "9".repeat(60), overflow),
            // Extra cases agreed in the M3 plan.
            arrayOf("1" + "0".repeat(99) + "1", overflow),
            arrayOf("8/0+", EvalResult.Incomplete),
            arrayOf("5*-", EvalResult.Incomplete),
            arrayOf("5+.", EvalResult.Incomplete),
            arrayOf(".", EvalResult.Incomplete),
            arrayOf("*5", malformed),
            arrayOf("+5", malformed),
            arrayOf(")", malformed),
            arrayOf("%", malformed),
            arrayOf("2(3)", malformed),
            arrayOf("2**3", malformed),
            arrayOf("5 5", malformed),
            arrayOf("5a", malformed),
            arrayOf(".+1", malformed),
        )
    }
}
