package com.pulse.calculator.engine

sealed interface ParseResult {
    data class Ok(val expr: Expr) : ParseResult
    data object Incomplete : ParseResult
    data object Malformed : ParseResult
}

/**
 * Recursive-descent parser for:
 * ```
 * expression := term (('+' | '-') term)*
 * term       := unary (('*' | '/') unary)*
 * unary      := '-' unary | postfix
 * postfix    := primary '%'*
 * primary    := NUMBER | '(' expression ')'
 * ```
 * Incomplete vs Malformed is decided where parsing stops: running out of tokens while something is
 * still expected means more input could fix it (Incomplete); meeting a token that cannot appear
 * there, or having tokens left over after a full expression, cannot be fixed by appending (Malformed).
 */
class Parser private constructor(private val tokens: List<Token>) {

    private var pos = 0

    /** Control flow inside one parse only; never escapes [parse]. */
    private class Failure(val incomplete: Boolean) : Exception(null, null, false, false)

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun next(): Token? = tokens.getOrNull(pos)?.also { pos++ }

    /** Nothing left where something is required: appending input could still make it valid. */
    private fun runOut(): Nothing = throw Failure(incomplete = true)

    /** A token that cannot be here: no appended input can fix it. */
    private fun unexpected(): Nothing = throw Failure(incomplete = false)

    private fun parseAll(): Expr {
        val expr = expression()
        if (pos < tokens.size) unexpected()
        return expr
    }

    private fun expression(): Expr {
        var left = term()
        while (true) {
            val op = (peek() as? Token.Operator)?.op?.takeIf { it == Op.Add || it == Op.Subtract } ?: return left
            pos++
            left = Expr.Binary(op, left, term())
        }
    }

    private fun term(): Expr {
        var left = unary()
        while (true) {
            val op = (peek() as? Token.Operator)?.op?.takeIf { it == Op.Multiply || it == Op.Divide } ?: return left
            pos++
            left = Expr.Binary(op, left, unary())
        }
    }

    private fun unary(): Expr {
        val token = peek()
        if (token is Token.Operator && token.op == Op.Subtract) {
            pos++
            return Expr.Negate(unary())
        }
        return postfix()
    }

    private fun postfix(): Expr {
        var expr = primary()
        while (peek() == Token.Percent) {
            pos++
            expr = Expr.Percent(expr)
        }
        return expr
    }

    private fun primary(): Expr = when (val token = next()) {
        null, Token.PartialNumber -> runOut()
        is Token.Number -> Expr.Number(token.value)
        Token.LParen -> {
            val inner = expression()
            when (next()) {
                Token.RParen -> inner
                null -> runOut()
                else -> unexpected()
            }
        }
        else -> unexpected()
    }

    companion object {
        fun parse(tokens: List<Token>): ParseResult = try {
            ParseResult.Ok(Parser(tokens).parseAll())
        } catch (failure: Failure) {
            if (failure.incomplete) ParseResult.Incomplete else ParseResult.Malformed
        }
    }
}
