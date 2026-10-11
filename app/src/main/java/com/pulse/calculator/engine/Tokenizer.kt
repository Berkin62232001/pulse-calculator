package com.pulse.calculator.engine

import java.math.BigDecimal

sealed interface TokenizeResult {
    data class Tokens(val tokens: List<Token>) : TokenizeResult
    data object Malformed : TokenizeResult
}

/** Splits the internal expression format (ASCII operators, no spaces) into tokens. */
object Tokenizer {

    fun tokenize(input: String): TokenizeResult {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isAsciiDigit() || c == '.' -> {
                    val start = i
                    while (i < input.length && (input[i].isAsciiDigit() || input[i] == '.')) i++
                    tokens += numberToken(input.substring(start, i), atEnd = i == input.length)
                        ?: return TokenizeResult.Malformed
                    continue
                }
                c == '+' -> tokens += Token.Operator(Op.Add)
                c == '-' -> tokens += Token.Operator(Op.Subtract)
                c == '*' -> tokens += Token.Operator(Op.Multiply)
                c == '/' -> tokens += Token.Operator(Op.Divide)
                c == '(' -> tokens += Token.LParen
                c == ')' -> tokens += Token.RParen
                c == '%' -> tokens += Token.Percent
                else -> return TokenizeResult.Malformed
            }
            i++
        }
        return TokenizeResult.Tokens(tokens)
    }

    /** Returns null when [lexeme] can never become a valid number (`1..2`, or a lone `.` that is not last). */
    private fun numberToken(lexeme: String, atEnd: Boolean): Token? {
        val dots = lexeme.count { it == '.' }
        return when {
            dots > 1 -> null
            lexeme == "." -> if (atEnd) Token.PartialNumber else null
            else -> Token.Number(BigDecimal(lexeme))
        }
    }

    private fun Char.isAsciiDigit() = this in '0'..'9'
}
