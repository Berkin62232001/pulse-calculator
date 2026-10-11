package com.pulse.calculator.ui

/** Semantics tags the Compose UI tests use to find things. */
object TestTags {
    const val Expression = "expression"

    fun key(label: String) = "key_$label"
}
