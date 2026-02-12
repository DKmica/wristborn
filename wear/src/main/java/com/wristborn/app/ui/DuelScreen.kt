package com.wristborn.app.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.rememberScalingLazyListState

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(state = listState) {
            item { Text("Wristborn Duel (placeholder)") }
            item { Text("Next: Sigil tap capture") }
            item { Text("Then: Gesture → Element") }
            item { Text("Then: Form taps + Release") }
            item {
                Button(onClick = onBack) { Text("Back") }
            }
        }
    }
}

