package com.pulse.calculator.ui.keypad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.pulse.calculator.ui.theme.ButtonLabelStyle
import com.pulse.calculator.ui.theme.ClearContainer
import com.pulse.calculator.ui.theme.ClearContent
import com.pulse.calculator.ui.theme.DigitContainer
import com.pulse.calculator.ui.theme.DigitContent
import com.pulse.calculator.ui.theme.EqualsContainer
import com.pulse.calculator.ui.theme.EqualsContent
import com.pulse.calculator.ui.theme.FunctionContainer
import com.pulse.calculator.ui.theme.FunctionContent
import com.pulse.calculator.ui.theme.OperatorContainer
import com.pulse.calculator.ui.theme.OperatorContent

enum class CalcButtonRole(val container: Color, val content: Color) {
    Digit(DigitContainer, DigitContent),
    Operator(OperatorContainer, OperatorContent),
    Equals(EqualsContainer, EqualsContent),
    Clear(ClearContainer, ClearContent),
    Function(FunctionContainer, FunctionContent),
}

private const val LabelToButtonHeightRatio = 0.4f

@Composable
fun CalcButton(
    label: String,
    role: CalcButtonRole,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(role.container)
            .clickable(role = Role.Button, onClick = onClick)
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
