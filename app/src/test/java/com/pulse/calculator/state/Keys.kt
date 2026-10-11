package com.pulse.calculator.state

import com.pulse.calculator.engine.Op

/**
 * Test shorthand: every character is one key press.
 * Digits, `.`, `+ - * /`, `( ) %` map directly; `<` is backspace, `C` is AC, `=` is equals.
 */
fun keyAction(key: Char): CalculatorAction = when (key) {
    in '0'..'9' -> CalculatorAction.Digit(key - '0')
    '.' -> CalculatorAction.Dot
    '+' -> CalculatorAction.Op(Op.Add)
    '-' -> CalculatorAction.Op(Op.Subtract)
    '*' -> CalculatorAction.Op(Op.Multiply)
    '/' -> CalculatorAction.Op(Op.Divide)
    '(' -> CalculatorAction.LParen
    ')' -> CalculatorAction.RParen
    '%' -> CalculatorAction.Percent
    '<' -> CalculatorAction.Backspace
    'C' -> CalculatorAction.Clear
    '=' -> CalculatorAction.Equals
    else -> error("No key for '$key'")
}

fun keyActions(keys: String): List<CalculatorAction> = keys.map(::keyAction)
