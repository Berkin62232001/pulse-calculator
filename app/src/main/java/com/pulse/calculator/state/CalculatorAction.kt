package com.pulse.calculator.state

sealed interface CalculatorAction {
    data class Digit(val digit: Int) : CalculatorAction
    data object Dot : CalculatorAction
    data class Op(val op: com.pulse.calculator.engine.Op) : CalculatorAction
    data object LParen : CalculatorAction
    data object RParen : CalculatorAction
    data object Percent : CalculatorAction
    data object Backspace : CalculatorAction
    data object Clear : CalculatorAction
    data object Equals : CalculatorAction
    data class LoadHistory(val expression: String) : CalculatorAction
    data object ClearHistory : CalculatorAction
}
