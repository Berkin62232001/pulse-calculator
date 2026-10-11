package com.pulse.calculator.ui

/** Semantics tags the Compose UI tests use to find things. */
object TestTags {
    const val Expression = "expression"
    const val Preview = "preview"
    const val HistoryButton = "history_button"
    const val HistorySheet = "history_sheet"
    const val HistoryClear = "history_clear"
    const val HistoryEmpty = "history_empty"

    fun key(label: String) = "key_$label"

    fun historyRow(index: Int) = "history_row_$index"
}
