package com.pulse.calculator.ui.display

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pulse.calculator.ui.theme.DisplayExpression
import com.pulse.calculator.ui.theme.ExpressionStyle
import com.pulse.calculator.ui.theme.PulseTheme

@Composable
fun ExpressionDisplay(
    text: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = ExpressionStyle,
            color = DisplayExpression,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .horizontalScroll(rememberScrollState(), reverseScrolling = true)
                .widthIn(min = maxWidth),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun ExpressionDisplayPreview() {
    PulseTheme { ExpressionDisplay(text = "12 + 5 × (3 + 2)") }
}
