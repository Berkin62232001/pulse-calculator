package com.pulse.calculator.ui.keypad

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.calculator.R
import com.pulse.calculator.engine.Op
import com.pulse.calculator.state.CalculatorAction
import com.pulse.calculator.ui.theme.PulseTheme

private data class KeySpec(
    val label: String,
    val role: CalcButtonRole,
    @StringRes val descriptionRes: Int,
    val action: CalculatorAction,
)

private val keyRows: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec("AC", CalcButtonRole.Clear, R.string.key_clear, CalculatorAction.Clear),
        KeySpec("(", CalcButtonRole.Function, R.string.key_open_paren, CalculatorAction.LParen),
        KeySpec(")", CalcButtonRole.Function, R.string.key_close_paren, CalculatorAction.RParen),
        KeySpec("÷", CalcButtonRole.Operator, R.string.key_divide, CalculatorAction.Op(Op.Divide)),
    ),
    listOf(
        KeySpec("7", CalcButtonRole.Digit, R.string.key_7, CalculatorAction.Digit(7)),
        KeySpec("8", CalcButtonRole.Digit, R.string.key_8, CalculatorAction.Digit(8)),
        KeySpec("9", CalcButtonRole.Digit, R.string.key_9, CalculatorAction.Digit(9)),
        KeySpec("×", CalcButtonRole.Operator, R.string.key_multiply, CalculatorAction.Op(Op.Multiply)),
    ),
    listOf(
        KeySpec("4", CalcButtonRole.Digit, R.string.key_4, CalculatorAction.Digit(4)),
        KeySpec("5", CalcButtonRole.Digit, R.string.key_5, CalculatorAction.Digit(5)),
        KeySpec("6", CalcButtonRole.Digit, R.string.key_6, CalculatorAction.Digit(6)),
        KeySpec("−", CalcButtonRole.Operator, R.string.key_minus, CalculatorAction.Op(Op.Subtract)),
    ),
    listOf(
        KeySpec("1", CalcButtonRole.Digit, R.string.key_1, CalculatorAction.Digit(1)),
        KeySpec("2", CalcButtonRole.Digit, R.string.key_2, CalculatorAction.Digit(2)),
        KeySpec("3", CalcButtonRole.Digit, R.string.key_3, CalculatorAction.Digit(3)),
        KeySpec("+", CalcButtonRole.Operator, R.string.key_plus, CalculatorAction.Op(Op.Add)),
    ),
    listOf(
        KeySpec("%", CalcButtonRole.Function, R.string.key_percent, CalculatorAction.Percent),
        KeySpec("0", CalcButtonRole.Digit, R.string.key_0, CalculatorAction.Digit(0)),
        KeySpec(".", CalcButtonRole.Digit, R.string.key_dot, CalculatorAction.Dot),
        KeySpec("=", CalcButtonRole.Equals, R.string.key_equals, CalculatorAction.Equals),
    ),
)

private val KeyGap = 12.dp
private val MinTouchTarget = 48.dp

@Composable
fun Keypad(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyGap),
    ) {
        keyRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(KeyGap),
            ) {
                row.forEach { key ->
                    CalcButton(
                        label = key.label,
                        role = key.role,
                        contentDescription = stringResource(key.descriptionRes),
                        onClick = { onAction(key.action) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .widthIn(min = MinTouchTarget)
                            .heightIn(min = MinTouchTarget),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun KeypadPreview() {
    PulseTheme { Keypad(onAction = {}) }
}
