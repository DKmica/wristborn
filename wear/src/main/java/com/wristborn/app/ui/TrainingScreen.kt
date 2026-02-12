package com.wristborn.app.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.rememberScalingLazyListState

@Composable
fun TrainingScreen(onBack: () -> Unit) {
    val listState = rememberScalingLazyListState()

    Scaffold(timeText = { TimeText() }) {
        ScalingLazyColumn(state = listState) {
            item { Text("Training (placeholder)") }
            item { Text("Next: teach first spell in < 30 seconds") }
            item {
                Button(onClick = onBack) { Text("Back") }
            }
        }
    }
}

