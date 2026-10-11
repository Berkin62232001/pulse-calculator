package com.pulse.calculator

import androidx.lifecycle.SavedStateHandle
import com.pulse.calculator.state.CalculatorAction
import com.pulse.calculator.state.CalculatorViewModel
import com.pulse.calculator.engine.Op
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * What the system does when it recreates the process: the saved state goes through a real
 * Bundle. This proves the history survives that trip with the types it is stored in.
 */
class SavedStateRoundTripTest {

    private fun press(viewModel: CalculatorViewModel, keys: String) = keys.forEach { key ->
        viewModel.onAction(
            when (key) {
                in '0'..'9' -> CalculatorAction.Digit(key - '0')
                '+' -> CalculatorAction.Op(Op.Add)
                '*' -> CalculatorAction.Op(Op.Multiply)
                '=' -> CalculatorAction.Equals
                'C' -> CalculatorAction.Clear
                else -> error("No key for '$key'")
            },
        )
    }

    @Test
    fun historySurvivesARealBundleRoundTrip() {
        val handle = SavedStateHandle()
        val original = CalculatorViewModel(handle)
        press(original, "1+1=C2+2=C12*3=")

        val bundle = handle.savedStateProvider().saveState()
        val restored = CalculatorViewModel(SavedStateHandle.createHandle(bundle, null))

        assertEquals(original.state.value.history, restored.state.value.history)
        assertEquals(listOf("12*3", "2+2", "1+1"), restored.state.value.history.map { it.expression })
        assertEquals(listOf("36", "4", "2"), restored.state.value.history.map { it.result })

        restored.onAction(CalculatorAction.ClearHistory)
        assertEquals(0, restored.state.value.history.size)
    }
}
