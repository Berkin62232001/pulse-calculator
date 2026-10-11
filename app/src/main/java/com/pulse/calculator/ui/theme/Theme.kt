package com.pulse.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val PulseColorScheme = darkColorScheme(
    primary = PulsePurple,
    background = PulseNavy,
    surface = PulseNavy,
    onBackground = PulseOnNavy,
    onSurface = PulseOnNavy,
)

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalReducedMotion provides rememberReducedMotion()) {
        MaterialTheme(
            colorScheme = PulseColorScheme,
            typography = PulseTypography,
            content = content,
        )
    }
}
