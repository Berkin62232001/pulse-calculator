package com.pulse.calculator.state

import com.pulse.calculator.engine.Op

/**
 * Applies a keypress to the expression string (PRODUCT_SPEC section 3 and "after a result is shown").
 * Pure Kotlin: no Android or Compose imports. Inputs that make no sense are ignored, not errors.
 */
object InputEditor {

    const val MaxLength = 100

    /** [hitLimit] is true when input was rejected only because the expression is at [MaxLength]. */
    data class Edit(val expression: String, val isResult: Boolean, val hitLimit: Boolean = false)

    fun apply(state: CalculatorState, action: CalculatorAction): Edit {
        val candidate = when (action) {
            is CalculatorAction.Digit -> digit(state, action.digit)
            CalculatorAction.Dot -> dot(state)
            is CalculatorAction.Op -> operator(state, action.op)
            CalculatorAction.LParen -> openParen(state)
            CalculatorAction.RParen -> closeParen(state)
            CalculatorAction.Percent -> percent(state)
            CalculatorAction.Backspace -> backspace(state.expression)
            CalculatorAction.Clear -> ""
            is CalculatorAction.LoadHistory -> action.expression
            CalculatorAction.Equals, CalculatorAction.ClearHistory -> null
        } ?: return Edit(state.expression, state.isResult)

        val addsInput = action !is CalculatorAction.Backspace &&
            action !is CalculatorAction.Clear &&
            action !is CalculatorAction.LoadHistory
        if (addsInput && candidate.length > MaxLength) {
            return Edit(state.expression, state.isResult, hitLimit = true)
        }
        return Edit(candidate, isResult = false)
    }

    private fun digit(state: CalculatorState, digit: Int): String {
        if (state.isResult) return digit.toString()
        val expression = state.expression
        if (expression.endsWithCloser()) return "$expression*$digit"
        // A lone leading zero is replaced, so 007 collapses to 7.
        return if (trailingNumber(expression) == "0") expression.dropLast(1) + digit else expression + digit
    }

    private fun dot(state: CalculatorState): String? {
        if (state.isResult) return "0."
        val expression = state.expression
        if (expression.endsWithCloser()) return "$expression*0."
        val number = trailingNumber(expression)
        return when {
            '.' in number -> null
            number.isNotEmpty() -> "$expression."
            else -> "${expression}0."
        }
    }

    private fun operator(state: CalculatorState, op: Op): String? {
        val expression = state.expression
        val symbol = op.symbol
        if (state.isResult) return expression + symbol

        val base = expression.trimEnd { it.isOperatorChar() }
        val trailingOperators = expression.substring(base.length)

        if (trailingOperators.isEmpty()) {
            val nothingBefore = expression.isEmpty() || expression.last() == '('
            return if (!nothingBefore || op == Op.Subtract) expression + symbol else null
        }
        // Only a unary minus so far (start of input or right after an opening parenthesis).
        if (base.isEmpty() || base.last() == '(') return null

        return when (op) {
            Op.Subtract -> when (trailingOperators) {
                "*", "/" -> expression + symbol
                "+", "-" -> base + symbol
                else -> null
            }
            else -> base + symbol
        }
    }

    private fun openParen(state: CalculatorState): String {
        if (state.isResult) return "("
        val expression = state.expression
        return if (expression.endsWithClosingOperand()) "$expression*(" else "$expression("
    }

    private fun closeParen(state: CalculatorState): String? {
        if (state.isResult) return null
        val expression = state.expression
        val unmatched = expression.count { it == '(' } - expression.count { it == ')' }
        return if (unmatched > 0 && expression.endsWithClosingOperand()) "$expression)" else null
    }

    private fun percent(state: CalculatorState): String? =
        if (state.expression.endsWithClosingOperand()) state.expression + "%" else null

    /** An auto-inserted `*` and its `(` go as one unit; the string cannot tell them from typed ones. */
    private fun backspace(expression: String): String =
        if (expression.endsWith("*(")) expression.dropLast(2) else expression.dropLast(1)

    private fun trailingNumber(expression: String): String = expression.takeLastWhile { it.isNumberChar() }

    private fun Char.isNumberChar() = this in '0'..'9' || this == '.'

    private fun Char.isOperatorChar() = this == '+' || this == '-' || this == '*' || this == '/'

    /** Ends with `)` or `%`: a following digit or `.` needs an implicit `*`. */
    private fun String.endsWithCloser(): Boolean = lastOrNull()?.let { it == ')' || it == '%' } ?: false

    /** Ends with a number, `)` or `%`: something a following `(`, `)` or `%` attaches to. */
    private fun String.endsWithClosingOperand(): Boolean =
        endsWithCloser() || (lastOrNull()?.isNumberChar() ?: false)

    private val Op.symbol: Char
        get() = when (this) {
            Op.Add -> '+'
            Op.Subtract -> '-'
            Op.Multiply -> '*'
            Op.Divide -> '/'
        }
}
