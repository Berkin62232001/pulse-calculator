package com.pulse.calculator.engine

/** Facade over tokenizer, parser and evaluator. Never throws: every failure is an [EvalResult]. */
object CalculatorEngine {

    fun evaluate(input: String): EvalResult = try {
        when (val tokenized = Tokenizer.tokenize(input)) {
            TokenizeResult.Malformed -> EvalResult.Error(ErrorReason.Malformed)
            is TokenizeResult.Tokens -> when (val parsed = Parser.parse(tokenized.tokens)) {
                ParseResult.Incomplete -> EvalResult.Incomplete
                ParseResult.Malformed -> EvalResult.Error(ErrorReason.Malformed)
                is ParseResult.Ok -> Evaluator.evaluate(parsed.expr)
            }
        }
    } catch (error: StackOverflowError) {
        // Absurdly deep nesting (far beyond the 100-character input limit) is treated as unparseable.
        EvalResult.Error(ErrorReason.Malformed)
    }
}
