package com.wristborn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.wristborn.app.engine.*
import com.wristborn.app.haptics.ElementHapticsPlayer
import com.wristborn.app.sensors.GestureType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DailyTrialScreen(
    arenaManager: ArenaManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { ElementHapticsPlayer(context) }
    val scope = rememberCoroutineScope()
    
    var score by remember { mutableIntStateOf(0) }
    var timeLeftMs by remember { mutableLongStateOf(30000L) }
    var isFinished by remember { mutableStateOf(false) }
    var targetElement by remember { mutableStateOf(Element.entries.random()) }
    
    // Casting State
    var sigilSequence by remember { mutableStateOf(listOf<SigilToken>()) }
    var detectedElement by remember { mutableStateOf<Element?>(null) }

    // Activate sensors
    DisposableEffect(Unit) {
        arenaManager.updateArmedState(true)
        onDispose { arenaManager.updateArmedState(false) }
    }

    // Timer logic
    LaunchedEffect(isFinished) {
        while (timeLeftMs > 0 && !isFinished) {
            delay(100)
            timeLeftMs -= 100
        }
        if (timeLeftMs <= 0) {
            isFinished = true
            arenaManager.progressionManager.updateDailyTrialScore(score)
        }
    }

    // Detection logic
    LaunchedEffect(arenaManager.lastGesture) {
        val gesture = arenaManager.lastGesture
        if (gesture != null && !isFinished) {
            val element = when (gesture) {
                GestureType.FLICK_RIGHT -> Element.FIRE
                GestureType.FLICK_LEFT -> Element.WIND
                GestureType.TWIST_CW -> Element.ARCANE
                GestureType.TWIST_CCW -> Element.VOID
                GestureType.SHAKE -> Element.STORM
                GestureType.STEADY_HOLD -> Element.EARTH
            }
            
            if (element == targetElement && sigilSequence.isNotEmpty()) {
                score += 100 + (timeLeftMs / 1000).toInt() // Bonus for speed
                haptics.playReleaseConfirm(element)
                targetElement = Element.entries.random()
                sigilSequence = emptyList()
                detectedElement = null
            }
        }
    }

    Scaffold(timeText = { TimeText() }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isFinished) {
                Text("Daily Trial", style = MaterialTheme.typography.caption1)
                Text("${timeLeftMs / 1000}s", style = MaterialTheme.typography.title2, color = Color.Red)
                Text("Cast: ${targetElement.name}", color = Color.Cyan)
                Text("Score: $score", style = MaterialTheme.typography.body2)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SigilField(
                    onTokenCaptured = { token ->
                        sigilSequence = (sigilSequence + token).takeLast(1)
                    },
                    modifier = Modifier.size(60.dp)
                )
            } else {
                Text("Trial Finished!", style = MaterialTheme.typography.title3)
                Text("Final Score: $score", color = Color.Yellow)
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Done")
                }
            }
        }
    }
}
