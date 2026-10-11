package com.pulse.calculator.engine

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** TEST_PLAN section 1: input in internal format -> expected display string. */
@RunWith(Parameterized::class)
class EngineEvaluationTest(private val input: String, private val expectedDisplay: String) {

    @Test
    fun evaluatesToExpectedDisplay() {
        val result = CalculatorEngine.evaluate(input)
        assertTrue("'$input' should succeed but was $result", result is EvalResult.Success)
        val display = NumberFormatter.format((result as EvalResult.Success).value)
        assertEquals("input '$input'", expectedDisplay, display)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} = {1}")
        fun cases(): List<Array<String>> = listOf(
            arrayOf("12+5", "17"),
            arrayOf("12-5", "7"),
            arrayOf("2+3*4", "14"),
            arrayOf("(2+3)*4", "20"),
            arrayOf("2*(3+(4*2))", "22"),
            arrayOf("12+5*(3+2)", "37"),
            arrayOf("2.5*4", "10"),
            arrayOf("0.1+0.2", "0.3"),
            arrayOf("-5+8", "3"),
            arrayOf("4*-2", "-8"),
            arrayOf("-(3+2)", "-5"),
            arrayOf("--5", "5"),
            arrayOf("15/3", "5"),
            arrayOf("1/3", "0.333333333333"),
            arrayOf("2/3", "0.666666666667"),
            arrayOf("10-2-3", "5"),
            arrayOf("100/10/2", "5"),
            arrayOf("50%", "0.5"),
            arrayOf("200*10%", "20"),
            arrayOf("200+10%", "200.1"),
            arrayOf("(50+50)%", "1"),
            arrayOf("999999999999*10", "9.99999999999E12"),
            arrayOf("0.0000000001*1", "1E-10"),
            arrayOf("5-5", "0"),
            arrayOf("-0*1", "0"),
            // Extra cases agreed in the M3 plan.
            arrayOf("5%%", "0.0005"),
            arrayOf("-5%", "-0.05"),
            arrayOf("1.", "1"),
            arrayOf(".5", "0.5"),
            arrayOf("1" + "0".repeat(100), "1E100"),
        )
    }
}

class EngineExactValueTest {

    @Test
    fun pointOnePlusPointTwoIsExactlyPointThree() {
        val result = CalculatorEngine.evaluate("0.1+0.2") as EvalResult.Success
        assertEquals(0, result.value.compareTo(BigDecimal("0.3")))
    }

    @Test
    fun divisionKeepsThirtyFourSignificantDigits() {
        val result = CalculatorEngine.evaluate("1/3") as EvalResult.Success
        assertEquals("0." + "3".repeat(34), result.value.toPlainString())
    }
}
