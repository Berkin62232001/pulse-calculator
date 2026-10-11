package com.pulse.calculator.ui.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.pulse.calculator.ui.TestTags
import com.pulse.calculator.ui.theme.ClearSweepDistance
import com.pulse.calculator.ui.theme.ClearSweepMillis
import com.pulse.calculator.ui.theme.ClearSweepMinLength
import com.pulse.calculator.ui.theme.DisplayExpression
import com.pulse.calculator.ui.theme.ExpressionStyle
import com.pulse.calculator.ui.theme.FlipCameraDistance
import com.pulse.calculator.ui.theme.GlyphFadeInMillis
import com.pulse.calculator.ui.theme.GlyphPopSpring
import com.pulse.calculator.ui.theme.GlyphPopStartScale
import com.pulse.calculator.ui.theme.LocalReducedMotion
import com.pulse.calculator.ui.theme.OperatorFlipMillis
import com.pulse.calculator.ui.theme.PulseTheme

private const val EmptyPlaceholder = "0"

/** What the previous composition showed, to work out what just changed. Plain fields: no recomposition. */
private class LastDisplay(var text: String, var isResult: Boolean)

private sealed interface GlyphMotion {
    data object None : GlyphMotion

    /** [token] is the whole text at the time, so each change is a new motion. */
    data class Pop(val token: String) : GlyphMotion
    data class Flip(val token: String, val from: Char) : GlyphMotion
}

/**
 * The expression, with the glyph that just changed as its own node, so only it animates.
 * Purely presentational: it reads [text] and never affects it, and nothing here waits on an animation.
 */
@Composable
fun ExpressionDisplay(
    text: String,
    isResult: Boolean,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val last = remember { LastDisplay(text, isResult) }
    val change = remember(text, isResult) { DisplayChange.between(last.text, last.isResult, text, isResult) }
    val sweepFrom = remember(text) {
        last.text.takeIf { text.isEmpty() && it.length >= ClearSweepMinLength }
    }
    val flipFrom = remember(text, isResult) { last.text.lastOrNull() }
    SideEffect {
        last.text = text
        last.isResult = isResult
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState(), reverseScrolling = true)
                .widthIn(min = maxWidth)
                .semantics {
                    this.testTag = TestTags.Expression
                    this.text = AnnotatedString(text.ifEmpty { EmptyPlaceholder })
                    // Only a confirmed result is announced; typing must stay silent.
                    if (isResult) liveRegion = LiveRegionMode.Polite
                },
            horizontalArrangement = Arrangement.End,
        ) {
            if (text.isEmpty()) {
                Glyph(char = EmptyPlaceholder.single(), motion = GlyphMotion.None)
            } else {
                // Only the last glyph ever animates, so everything before it is one plain Text.
                // That keeps the node count constant however long the expression gets.
                if (text.length > 1) {
                    Text(
                        text = text.dropLast(1),
                        style = ExpressionStyle,
                        color = DisplayExpression,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.clearAndSetSemantics { },
                    )
                }
                Glyph(char = text.last(), motion = motionFor(change, text.lastIndex, text, flipFrom, reducedMotion))
            }
        }
        if (sweepFrom != null && !reducedMotion) {
            SweepGhost(text = sweepFrom, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

private fun motionFor(
    change: DisplayChange,
    index: Int,
    text: String,
    flipFrom: Char?,
    reducedMotion: Boolean,
): GlyphMotion = when {
    reducedMotion -> GlyphMotion.None
    change is DisplayChange.Pop && change.index == index -> GlyphMotion.Pop(text)
    change is DisplayChange.Flip && change.index == index && flipFrom != null -> GlyphMotion.Flip(text, flipFrom)
    else -> GlyphMotion.None
}

@Composable
private fun Glyph(char: Char, motion: GlyphMotion) {
    // A new motion means a new set of animatables that start at the motion's first frame,
    // so there is no frame where the glyph is drawn at rest before the animation begins.
    val scale = remember(motion) { Animatable(if (motion is GlyphMotion.Pop) GlyphPopStartScale else 1f) }
    val alpha = remember(motion) { Animatable(if (motion is GlyphMotion.Pop) 0f else 1f) }
    val flip = remember(motion) { Animatable(if (motion is GlyphMotion.Flip) 0f else 1f) }

    LaunchedEffect(motion) {
        when (motion) {
            GlyphMotion.None -> Unit
            is GlyphMotion.Pop -> {
                launch { scale.animateTo(1f, GlyphPopSpring) }
                alpha.animateTo(1f, tween(GlyphFadeInMillis))
            }
            is GlyphMotion.Flip -> {
                flip.animateTo(FlipHalf, tween(OperatorFlipMillis / 2, easing = FastOutLinearInEasing))
                flip.animateTo(1f, tween(OperatorFlipMillis / 2, easing = LinearOutSlowInEasing))
            }
        }
    }

    val shown = if (motion is GlyphMotion.Flip && flip.value < FlipHalf) motion.from else char
    Text(
        text = shown.toString(),
        style = ExpressionStyle,
        color = DisplayExpression,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
                rotationX = flipRotation(flip.value)
                cameraDistance = FlipCameraDistance * density
            }
            .clearAndSetSemantics { },
    )
}

private const val FlipHalf = 0.5f

/** 0..0.5 turns the old glyph edge-on (0 to 90 degrees); 0.5..1 turns the new one in (-90 to 0). */
private fun flipRotation(progress: Float): Float =
    if (progress < FlipHalf) progress * 180f else (progress - 1f) * 180f

/** The cleared content sweeps away while the real, already empty display sits underneath. */
@Composable
private fun SweepGhost(text: String, modifier: Modifier = Modifier) {
    val progress = remember(text) { Animatable(0f) }
    var finished by remember(text) { mutableStateOf(false) }
    LaunchedEffect(progress) {
        progress.animateTo(1f, tween(ClearSweepMillis, easing = FastOutLinearInEasing))
        finished = true
    }
    if (!finished) {
        Text(
            text = text,
            style = ExpressionStyle,
            color = DisplayExpression,
            maxLines = 1,
            softWrap = false,
            modifier = modifier
                .graphicsLayer {
                    translationX = -ClearSweepDistance.toPx() * progress.value
                    alpha = 1f - progress.value
                }
                .clearAndSetSemantics { },
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun ExpressionDisplayPreview() {
    PulseTheme { ExpressionDisplay(text = "12 + 5 × (3 + 2)", isResult = false) }
}

@Preview(widthDp = 360)
@Composable
private fun ExpressionDisplayEmptyPreview() {
    PulseTheme { ExpressionDisplay(text = "", isResult = false) }
}
