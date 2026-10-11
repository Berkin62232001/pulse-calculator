package com.pulse.calculator.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.pulse.calculator.engine.CalculatorEngine
import com.pulse.calculator.engine.EvalResult
import com.pulse.calculator.engine.NumberFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the calculator state. Everything is synchronous: an action updates [state] before
 * [onAction] returns, so rapid input can never be reordered or dropped.
 * History is also kept in [savedState]: it survives rotation and the system recreating the
 * process, but it is never written to disk and is dropped when the user closes the app.
 */
class CalculatorViewModel(
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState(history = restoreHistory()))
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = EventBuffer)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var nextHistoryId = (_state.value.history.maxOfOrNull { it.id } ?: -1L) + 1

    fun onAction(action: CalculatorAction) {
        when (action) {
            CalculatorAction.Equals -> evaluate()
            CalculatorAction.ClearHistory -> setHistory(emptyList())
            else -> edit(action)
        }
    }

    private fun edit(action: CalculatorAction) {
        val current = _state.value
        val edit = InputEditor.apply(current, action)
        // Any input dismisses a shown error and then applies to the expression as usual.
        _state.value = current.copy(
            expression = edit.expression,
            isResult = edit.isResult,
            error = null,
            preview = previewFor(edit.expression, edit.isResult),
        )
        if (edit.hitLimit) _events.tryEmit(UiEvent.Shake)
    }

    /**
     * Live preview (PRODUCT_SPEC section 4): only a successful evaluation of an expression that
     * contains an operation. Incomplete input, math errors and plain numbers show nothing; errors
     * only surface on `=`.
     */
    private fun previewFor(expression: String, isResult: Boolean): String? {
        if (isResult || !hasOperation(expression)) return null
        return (CalculatorEngine.evaluate(expression) as? EvalResult.Success)
            ?.let { NumberFormatter.format(it.value) }
    }

    private fun evaluate() {
        val current = _state.value
        if (current.isResult || current.expression.isEmpty() || SingleNumber.matches(current.expression)) return

        when (val result = CalculatorEngine.evaluate(current.expression)) {
            is EvalResult.Success -> {
                val entry = HistoryEntry(
                    id = nextHistoryId++,
                    expression = current.expression,
                    result = NumberFormatter.format(result.value),
                )
                _state.value = current.copy(
                    expression = NumberFormatter.toInternal(result.value),
                    isResult = true,
                    error = null,
                    preview = null,
                )
                setHistory((listOf(entry) + current.history).take(MaxHistory))
                _events.tryEmit(UiEvent.ResultConfirmed)
            }
            EvalResult.Incomplete -> _events.tryEmit(UiEvent.Shake)
            is EvalResult.Error -> {
                _state.value = current.copy(error = result.reason, preview = null)
                _events.tryEmit(UiEvent.ErrorShown)
            }
        }
    }

    /** Updates the history in the state and in the saved state, in one place. */
    private fun setHistory(history: List<HistoryEntry>) {
        _state.value = _state.value.copy(history = history)
        savedState[HistoryIdsKey] = LongArray(history.size) { history[it].id }
        savedState[HistoryExpressionsKey] = Array(history.size) { history[it].expression }
        savedState[HistoryResultsKey] = Array(history.size) { history[it].result }
    }

    /** Whatever is missing, mismatched or of the wrong type is ignored: restoring never crashes. */
    private fun restoreHistory(): List<HistoryEntry> = runCatching {
        val ids = savedState.get<LongArray>(HistoryIdsKey)
        val expressions = savedState.get<Array<String>>(HistoryExpressionsKey)
        val results = savedState.get<Array<String>>(HistoryResultsKey)
        if (ids == null || expressions == null || results == null) {
            emptyList()
        } else {
            val count = minOf(ids.size, expressions.size, results.size, MaxHistory)
            List(count) { HistoryEntry(ids[it], expressions[it], results[it]) }
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val MaxHistory = 50
        const val EventBuffer = 16
        val SingleNumber = Regex("-?(\\d+\\.?\\d*|\\.\\d+)")

        /** `+ * / %` always count; `-` counts only between two terms, not as a sign. */
        fun hasOperation(expression: String): Boolean = expression.indices.any { i ->
            when (expression[i]) {
                '+', '*', '/', '%' -> true
                '-' -> i > 0 && expression[i - 1].let { it.isDigit() || it == '.' || it == ')' || it == '%' }
                else -> false
            }
        }
    }
}

internal const val HistoryIdsKey = "history_ids"
internal const val HistoryExpressionsKey = "history_expressions"
internal const val HistoryResultsKey = "history_results"
