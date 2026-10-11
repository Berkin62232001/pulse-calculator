package com.pulse.calculator.state

import com.pulse.calculator.engine.ErrorReason

data class CalculatorState(
    /** Internal format: ASCII operators, ungrouped numbers. */
    val expression: String = "",
    /** Formatted live preview; always null until the preview milestone. */
    val preview: String? = null,
    /** The expression currently shows a confirmed result. */
    val isResult: Boolean = false,
    val error: ErrorReason? = null,
    val history: List<HistoryEntry> = emptyList(),
)

/** [expression] is in internal format; [result] is the grouped display string. */
data class HistoryEntry(val id: Long, val expression: String, val result: String)
