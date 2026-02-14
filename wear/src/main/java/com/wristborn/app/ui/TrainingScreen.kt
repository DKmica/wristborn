package com.wristborn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.wristborn.app.engine.Element
import com.wristborn.app.engine.SigilToken
import com.wristborn.app.haptics.HapticRhythmPlayer

@Composable
fun TrainingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val hapticPlayer = remember { HapticRhythmPlayer(context) }
    val listState = rememberScalingLazyListState()
    
    var sigilSequence by remember { mutableStateOf(listOf<SigilToken>()) }

    Scaffold(timeText = { TimeText() }) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Text("Training Grounds", style = MaterialTheme.typography.caption1) }
            
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                SigilField(
                    onTokenCaptured = { token ->
                        sigilSequence = (sigilSequence + token).takeLast(4)
                        hapticPlayer.playSigil(listOf(token))
                    }
                )
            }

            item {
                Text(
                    text = if (sigilSequence.isEmpty()) "Tap Sigil" else sigilSequence.joinToString(" ") { it.name },
                    style = MaterialTheme.typography.body2
                )
            }

            item {
                Row {
                    Button(onClick = { sigilSequence = emptyList() }) {
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { hapticPlayer.playSigil(sigilSequence) }) {
                        Text("Replay")
                    }
                }
            }

            item {
                Button(onClick = onBack) { Text("Finish") }
            }
        }
    }
}
