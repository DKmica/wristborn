package com.wristborn.app.ui

import androidx.compose.runtime.*
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.rememberScalingLazyListState

@Composable
fun ArenaIdleScreen(
    onTraining: () -> Unit,
    onPractice: () -> Unit
) {
    var armed by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(state = listState) {
            item { Text("Wristborn") }
            item { Text(if (armed) "Arena Mode: ARMED" else "Arena Mode: PASSIVE") }

            item {
                Button(onClick = { armed = !armed }) {
                    Text(if (armed) "Disarm" else "Arm")
                }
            }

            item { Button(onClick = onTraining) { Text("Training") } }
            item { Button(onClick = onPractice) { Text("Practice Duel") } }
        }
    }
}
// Force change to ensure file is rewritten
