package com.pulse.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PulseColorScheme = darkColorScheme(
    primary = PulsePurple,
    background = PulseNavy,
    surface = PulseNavy,
    onBackground = PulseOnNavy,
    onSurface = PulseOnNavy,
)

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PulseColorScheme,
        typography = PulseTypography,
        content = content,
    )
}
