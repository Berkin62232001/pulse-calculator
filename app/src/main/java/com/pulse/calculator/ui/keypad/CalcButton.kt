package com.pulse.calculator.ui.keypad

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.pulse.calculator.ui.theme.ButtonLabelStyle
import com.pulse.calculator.ui.theme.ButtonPressSpring
import com.pulse.calculator.ui.theme.ButtonReleaseSpring
import com.pulse.calculator.ui.theme.ClearContainer
import com.pulse.calculator.ui.theme.ClearContent
import com.pulse.calculator.ui.theme.DigitContainer
import com.pulse.calculator.ui.theme.DigitContent
import com.pulse.calculator.ui.theme.EqualsContainer
import com.pulse.calculator.ui.theme.EqualsContent
import com.pulse.calculator.ui.theme.FlashAmount
import com.pulse.calculator.ui.theme.FlashInSpec
import com.pulse.calculator.ui.theme.FlashOutSpec
import com.pulse.calculator.ui.theme.FunctionContainer
import com.pulse.calculator.ui.theme.FunctionContent
import com.pulse.calculator.ui.theme.LocalReducedMotion
import com.pulse.calculator.ui.theme.OperatorContainer
import com.pulse.calculator.ui.theme.OperatorContent
import com.pulse.calculator.ui.theme.PressScale

enum class CalcButtonRole(val container: Color, val content: Color) {
    Digit(DigitContainer, DigitContent),
    Operator(OperatorContainer, OperatorContent),
    Equals(EqualsContainer, EqualsContent),
    Clear(ClearContainer, ClearContent),
    Function(FunctionContainer, FunctionContent),
}

private const val LabelToButtonHeightRatio = 0.4f

/**
 * [onClick] fires on the tap itself; the press animation is presentation only and never gates it.
 * Under reduced motion the scale and flash change instantly instead of animating.
 */
@Composable
fun CalcButton(
    label: String,
    role: CalcButtonRole,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) PressScale else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            pressed -> ButtonPressSpring
            else -> ButtonReleaseSpring
        },
        label = "buttonScale",
    )
    val container by animateColorAsState(
        targetValue = if (pressed) lerp(role.container, Color.White, FlashAmount) else role.container,
        animationSpec = when {
            reducedMotion -> snap()
            pressed -> FlashInSpec
            else -> FlashOutSpec
        },
        label = "buttonFlash",
    )

    // Scale and colour are read in the layer and draw phases, so animating them never recomposes.
    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(percent = 50))
            .drawBehind { drawRect(container) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        // Sized from the button, not from the system font scale, so labels never clip.
        val labelSize = with(LocalDensity.current) {
            (maxHeight * LabelToButtonHeightRatio).toSp()
        }
        Text(
            text = label,
            style = ButtonLabelStyle,
            fontSize = labelSize,
            color = role.content,
            textAlign = TextAlign.Center,
        )
    }
}
