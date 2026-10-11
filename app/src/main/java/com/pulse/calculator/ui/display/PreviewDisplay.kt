package com.pulse.calculator.ui.display

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pulse.calculator.ui.theme.DisplayError
import com.pulse.calculator.ui.theme.DisplayPreview
import com.pulse.calculator.ui.theme.PreviewStyle
import com.pulse.calculator.ui.theme.PulseTheme

/** The slot under the expression. Empty text keeps the line's height so nothing shifts. */
@Composable
fun PreviewDisplay(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Text(
        text = text,
        style = PreviewStyle,
        color = if (isError) DisplayError else DisplayPreview,
        textAlign = TextAlign.End,
        maxLines = 1,
        minLines = 1,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(widthDp = 360)
@Composable
private fun PreviewDisplayPreview() {
    PulseTheme { PreviewDisplay(text = "37") }
}

@Preview(widthDp = 360)
@Composable
private fun PreviewDisplayErrorPreview() {
    PulseTheme { PreviewDisplay(text = "Can't divide by zero", isError = true) }
}
