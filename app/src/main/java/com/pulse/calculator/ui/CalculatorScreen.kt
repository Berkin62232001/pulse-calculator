package com.pulse.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.pulse.calculator.R
import com.pulse.calculator.engine.ErrorReason
import com.pulse.calculator.state.CalculatorAction
import com.pulse.calculator.state.CalculatorState
import com.pulse.calculator.ui.display.ExpressionDisplay
import com.pulse.calculator.ui.display.ExpressionFormat
import com.pulse.calculator.ui.display.PreviewDisplay
import com.pulse.calculator.ui.keypad.Keypad
import com.pulse.calculator.ui.theme.DisplayExpression
import com.pulse.calculator.ui.theme.GradientBottom
import com.pulse.calculator.ui.theme.GradientMid
import com.pulse.calculator.ui.theme.GradientTop
import com.pulse.calculator.ui.theme.PulseTheme
import com.pulse.calculator.ui.theme.TopBarTitleStyle

private const val MaxFontScale = 1.3f
private const val DisplayWeight = 0.4f
private const val KeypadWeight = 0.6f

private val ScreenPadding = 16.dp
private val IconTouchTarget = 48.dp

private val backgroundBrush = Brush.linearGradient(
    colors = listOf(GradientTop, GradientMid, GradientBottom),
)

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val cappedDensity = Density(density.density, fontScale = density.fontScale.coerceAtMost(MaxFontScale))

    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundBrush),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = ScreenPadding),
            ) {
                TopBar(onHistoryClick = onHistoryClick)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(DisplayWeight),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    ExpressionDisplay(text = expressionText(state.expression))
                    PreviewDisplay(
                        text = state.error?.let { stringResource(it.messageRes()) } ?: "",
                        isError = state.error != null,
                    )
                    BackspaceRow(
                        onClick = { onAction(CalculatorAction.Backspace) },
                        onLongClick = { onAction(CalculatorAction.Clear) },
                    )
                }
                Keypad(
                    onAction = onAction,
                    modifier = Modifier
                        .weight(KeypadWeight)
                        .padding(bottom = ScreenPadding),
                )
            }
        }
    }
}

@Composable
private fun TopBar(onHistoryClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.top_bar_title),
            style = TopBarTitleStyle,
            color = DisplayExpression,
        )
        IconTouchTarget(
            iconRes = R.drawable.ic_history,
            contentDescription = stringResource(R.string.history_button),
            onClick = onHistoryClick,
        )
    }
}

/** An empty expression reads as 0; the state itself stays empty. */
private fun expressionText(expression: String): String =
    if (expression.isEmpty()) "0" else ExpressionFormat.toDisplay(expression)

private fun ErrorReason.messageRes(): Int = when (this) {
    ErrorReason.DivideByZero -> R.string.error_divide_by_zero
    ErrorReason.Overflow -> R.string.error_overflow
    ErrorReason.Malformed -> R.string.error_malformed
}

@Composable
private fun BackspaceRow(onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        IconTouchTarget(
            iconRes = R.drawable.ic_backspace,
            contentDescription = stringResource(R.string.key_backspace),
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IconTouchTarget(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .size(IconTouchTarget)
            .clip(CircleShape)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = DisplayExpression,
            modifier = Modifier.size(24.dp),
        )
    }
}

private val SampleState = CalculatorState(expression = "12+5*(3+2)")
private val ErrorState = CalculatorState(expression = "8/0", error = ErrorReason.DivideByZero)

@Preview(name = "Small phone", widthDp = 360, heightDp = 640)
@Composable
private fun CalculatorScreenSmallPreview() {
    PulseTheme {
        CalculatorScreen(state = SampleState, onAction = {}, onHistoryClick = {})
    }
}

@Preview(name = "Large phone", widthDp = 412, heightDp = 915)
@Composable
private fun CalculatorScreenLargePreview() {
    PulseTheme {
        CalculatorScreen(state = SampleState, onAction = {}, onHistoryClick = {})
    }
}

@Preview(name = "Small phone, large font", widthDp = 360, heightDp = 640, fontScale = 1.3f)
@Composable
private fun CalculatorScreenLargeFontPreview() {
    PulseTheme {
        CalculatorScreen(state = SampleState, onAction = {}, onHistoryClick = {})
    }
}

@Preview(name = "Error", widthDp = 360, heightDp = 640)
@Composable
private fun CalculatorScreenErrorPreview() {
    PulseTheme {
        CalculatorScreen(state = ErrorState, onAction = {}, onHistoryClick = {})
    }
}
