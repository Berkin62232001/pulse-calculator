package com.pulse.calculator.state

/** One-shot presentation cues. They never carry data the UI needs to stay correct. */
sealed interface UiEvent {
    data object Shake : UiEvent
    data object ResultConfirmed : UiEvent
    data object ErrorShown : UiEvent
}
