package com.pulse.calculator

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulse.calculator.state.CalculatorViewModel
import com.pulse.calculator.ui.CalculatorScreen
import com.pulse.calculator.ui.theme.PulseTheme

private const val LogTag = "Pulse"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            PulseTheme {
                val viewModel: CalculatorViewModel = viewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                CalculatorScreen(
                    state = state,
                    events = viewModel.events,
                    onAction = viewModel::onAction,
                    onHistoryClick = { Log.d(LogTag, "history") },
                )
            }
        }
    }
}
