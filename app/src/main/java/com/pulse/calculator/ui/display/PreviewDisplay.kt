package com.pulse.calculator.ui.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pulse.calculator.ui.theme.DisplayError
import com.pulse.calculator.ui.theme.DisplayPreview
import com.pulse.calculator.ui.theme.LocalReducedMotion
import com.pulse.calculator.ui.theme.PreviewSlideDistance
import com.pulse.calculator.ui.theme.PreviewSlideMillis
import com.pulse.calculator.ui.theme.PreviewStyle
import com.pulse.calculator.ui.theme.PulseTheme

private data class PreviewContent(val text: String, val isError: Boolean)

/** What the previous composition showed. Plain field: reading it never recomposes anything. */
private class LastPreview(var content: PreviewContent)

/**
 * The slot under the expression. A changed value slides up while the old one slides away and
 * both crossfade; empty text keeps the line's height so nothing shifts. Presentation only.
 */
@Composable
fun PreviewDisplay(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val reducedMotion = LocalReducedMotion.current
    val current = PreviewContent(text, isError)
    val last = remember { LastPreview(current) }
    val outgoing = remember(current, reducedMotion) { last.content.takeIf { it != current && !reducedMotion } }
    SideEffect { last.content = current }

    // New animatable per change; it starts at the first frame so nothing flashes at rest.
    val progress = remember(current, reducedMotion) { Animatable(if (outgoing != null) 0f else 1f) }
    LaunchedEffect(progress) {
        if (outgoing != null) progress.animateTo(1f, tween(PreviewSlideMillis))
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        PreviewText(
            content = current,
            modifier = Modifier.graphicsLayer {
                alpha = progress.value
                translationY = PreviewSlideDistance.toPx() * (1f - progress.value)
            },
        )
        if (outgoing != null) {
            PreviewText(
                content = outgoing,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1f - progress.value
                        translationY = -PreviewSlideDistance.toPx() * progress.value
                    }
                    .clearAndSetSemantics { },
            )
        }
    }
}

@Composable
private fun PreviewText(content: PreviewContent, modifier: Modifier = Modifier) {
    Text(
        text = content.text,
        style = PreviewStyle,
        color = if (content.isError) DisplayError else DisplayPreview,
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
