package com.pulse.calculator.ui.display

/**
 * Internal expression -> display text: `× ÷ −`, spaces around binary operators, thousands
 * grouping in numbers. Pure Kotlin so it can be unit-tested on the JVM.
 */
object ExpressionFormat {

    fun toDisplay(expression: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            when {
                c.isNumberChar() -> {
                    val start = i
                    while (i < expression.length && expression[i].isNumberChar()) i++
                    out.append(groupNumber(expression.substring(start, i)))
                    continue
                }
                c == '-' -> if (isUnary(expression, i)) out.append('−') else out.append(" − ")
                c == '+' -> out.append(" + ")
                c == '*' -> out.append(" × ")
                c == '/' -> out.append(" ÷ ")
                else -> out.append(c)
            }
            i++
        }
        return out.toString().trim()
    }

    /** A minus is unary at the start, after `(`, or right after another operator. */
    private fun isUnary(expression: String, index: Int): Boolean {
        if (index == 0) return true
        return expression[index - 1] in "(+-*/"
    }

    private fun groupNumber(number: String): String {
        val integerPart = number.substringBefore('.')
        val rest = number.substring(integerPart.length)
        return integerPart.reversed().chunked(3).joinToString(",").reversed() + rest
    }

    private fun Char.isNumberChar() = this in '0'..'9' || this == '.'
}
