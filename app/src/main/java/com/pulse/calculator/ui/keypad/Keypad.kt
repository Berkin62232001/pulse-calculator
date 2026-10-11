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
import com.pulse.calculator.ui.theme.PulseTheme

private data class KeySpec(
    val label: String,
    val role: CalcButtonRole,
    @StringRes val descriptionRes: Int,
)

private val keyRows: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec("AC", CalcButtonRole.Clear, R.string.key_clear),
        KeySpec("(", CalcButtonRole.Function, R.string.key_open_paren),
        KeySpec(")", CalcButtonRole.Function, R.string.key_close_paren),
        KeySpec("÷", CalcButtonRole.Operator, R.string.key_divide),
    ),
    listOf(
        KeySpec("7", CalcButtonRole.Digit, R.string.key_7),
        KeySpec("8", CalcButtonRole.Digit, R.string.key_8),
        KeySpec("9", CalcButtonRole.Digit, R.string.key_9),
        KeySpec("×", CalcButtonRole.Operator, R.string.key_multiply),
    ),
    listOf(
        KeySpec("4", CalcButtonRole.Digit, R.string.key_4),
        KeySpec("5", CalcButtonRole.Digit, R.string.key_5),
        KeySpec("6", CalcButtonRole.Digit, R.string.key_6),
        KeySpec("−", CalcButtonRole.Operator, R.string.key_minus),
    ),
    listOf(
        KeySpec("1", CalcButtonRole.Digit, R.string.key_1),
        KeySpec("2", CalcButtonRole.Digit, R.string.key_2),
        KeySpec("3", CalcButtonRole.Digit, R.string.key_3),
        KeySpec("+", CalcButtonRole.Operator, R.string.key_plus),
    ),
    listOf(
        KeySpec("%", CalcButtonRole.Function, R.string.key_percent),
        KeySpec("0", CalcButtonRole.Digit, R.string.key_0),
        KeySpec(".", CalcButtonRole.Digit, R.string.key_dot),
        KeySpec("=", CalcButtonRole.Equals, R.string.key_equals),
    ),
)

private val KeyGap = 12.dp
private val MinTouchTarget = 48.dp

@Composable
fun Keypad(
    onKey: (String) -> Unit,
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
                        onClick = { onKey(key.label) },
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
    PulseTheme { Keypad(onKey = {}) }
}
