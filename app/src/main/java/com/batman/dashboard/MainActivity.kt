package com.batman.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.batman.dashboard.ui.theme.BatmanDashboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isStealthMode by remember { mutableStateOf(false) }
            var threatLevel    by remember { mutableStateOf(0f) }

            BatmanDashboardTheme(
                isStealthMode = isStealthMode,
                threatLevel   = threatLevel
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BatmanNavigation(
                        isStealthMode   = isStealthMode,
                        onToggleStealth = { isStealthMode = !isStealthMode },
                        onThreatUpdate  = { threatLevel = it }
                    )
                }
            }
        }
    }
}
