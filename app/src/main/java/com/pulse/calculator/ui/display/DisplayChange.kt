package com.pulse.calculator.ui.display

/**
 * What changed between two display strings, as far as animation is concerned. Pure Kotlin: it
 * only decides which glyph to animate, it never touches state or results.
 */
sealed interface DisplayChange {
    data object None : DisplayChange

    /** The glyph at [index] was just typed and pops in. */
    data class Pop(val index: Int) : DisplayChange

    /** The operator at [index] was replaced by another operator and flips over. */
    data class Flip(val index: Int) : DisplayChange

    companion object {
        private const val Operators = "+−×÷"

        fun between(previous: String, previousIsResult: Boolean, next: String, isResult: Boolean): DisplayChange = when {
            // A confirmed result gets its own roll-up animation, not per-glyph ones.
            isResult || next.isEmpty() -> None
            isOperatorReplacement(previous, next) -> Flip(next.lastIndex)
            next.length > previous.length -> Pop(next.lastIndex)
            previousIsResult && !isBackspaceOf(previous, next) -> Pop(next.lastIndex)
            else -> None
        }

        private fun isOperatorReplacement(previous: String, next: String): Boolean =
            previous.isNotEmpty() &&
                previous.length == next.length &&
                previous.dropLast(1) == next.dropLast(1) &&
                previous.last() != next.last() &&
                previous.last() in Operators &&
                next.last() in Operators

        /** Grouping separators are ignored, so deleting from `1,234` reads as deleting from `1234`. */
        private fun isBackspaceOf(previous: String, next: String): Boolean =
            next.replace(",", "") == previous.replace(",", "").dropLast(1)
    }
}
