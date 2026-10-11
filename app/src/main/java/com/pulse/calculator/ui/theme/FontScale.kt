package com.pulse.calculator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** Text follows the system font scale only up to this (PRODUCT_SPEC section 10). */
const val MaxFontScale = 1.3f

/**
 * Applies the font scale cap to [content]. A window of its own, like the history sheet, starts
 * from the uncapped system density again, so each such window needs its own wrapper.
 */
@Composable
fun CappedFontScale(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val capped = Density(density.density, fontScale = density.fontScale.coerceAtMost(MaxFontScale))
    CompositionLocalProvider(LocalDensity provides capped, content = content)
}
