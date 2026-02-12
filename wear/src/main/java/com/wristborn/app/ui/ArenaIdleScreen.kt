package com.wristborn.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.wristborn.app.engine.ArenaState

@Composable
fun ArenaIdleScreen(
    arenaState: ArenaState,
    onToggleArm: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenDuel: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = if (arenaState == ArenaState.ARMED) "Arena Armed" else "Arena Passive",
                style = MaterialTheme.typography.title3
            )
        }
        item {
            Button(onClick = onToggleArm) {
                Text(if (arenaState == ArenaState.ARMED) "Disarm" else "Arm")
            }
        }
        item {
            Button(onClick = onOpenTraining) {
                Text("Training")
            }
        }
        item {
            Button(onClick = onOpenDuel) {
                Text("Duel")
            }
        }
    }
}
