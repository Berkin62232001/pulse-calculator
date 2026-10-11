package com.pulse.calculator

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pulse.calculator.ui.CalculatorScreen
import com.pulse.calculator.ui.theme.PulseTheme

private const val LogTag = "Pulse"
private const val SampleExpression = "12 + 5 × (3 + 2)"
private const val SamplePreview = "37"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            PulseTheme {
                CalculatorScreen(
                    expression = SampleExpression,
                    preview = SamplePreview,
                    onKey = { label -> Log.d(LogTag, "key: $label") },
                )
            }
        }
    }
}
