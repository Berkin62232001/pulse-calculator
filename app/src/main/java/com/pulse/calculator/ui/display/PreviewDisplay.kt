package com.pulse.calculator.ui.display

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pulse.calculator.ui.theme.DisplayPreview
import com.pulse.calculator.ui.theme.PreviewStyle
import com.pulse.calculator.ui.theme.PulseTheme

@Composable
fun PreviewDisplay(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = PreviewStyle,
        color = DisplayPreview,
        textAlign = TextAlign.End,
        maxLines = 1,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(widthDp = 360)
@Composable
private fun PreviewDisplayPreview() {
    PulseTheme { PreviewDisplay(text = "37") }
}
