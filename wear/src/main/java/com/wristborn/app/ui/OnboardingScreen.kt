package com.wristborn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.wristborn.app.engine.SigilToken
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    
    Scaffold(timeText = { TimeText() }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                0 -> {
                    Text("Welcome, Duelist", style = MaterialTheme.typography.title3)
                    Text("Spells require Sigils and Gestures.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Button(onClick = { step++ }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Next")
                    }
                }
                1 -> {
                    Text("The Sigil", color = Color.Cyan)
                    Text("Tap the center field.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    SigilField(onTokenCaptured = { step++ }, modifier = Modifier.size(60.dp))
                }
                2 -> {
                    Text("The Gesture", color = Color.Yellow)
                    Text("Flick or Twist to release your power.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Button(onClick = { step++ }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("I'm Ready")
                    }
                }
                3 -> {
                    Text("Arena Armed", color = Color.Red)
                    Text("Find others nearby to duel.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Button(onClick = onComplete, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Start")
                    }
                }
            }
        }
    }
}
