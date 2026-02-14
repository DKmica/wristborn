package com.wristborn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.wristborn.app.engine.ArenaManager
import com.wristborn.app.engine.PvPStatus

@Composable
fun ArenaIdleScreen(
    arenaManager: ArenaManager,
    onTraining: () -> Unit,
    onPractice: () -> Unit,
    onPvPDuel: () -> Unit,
    onProfile: () -> Unit,
    onDailyTrial: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    
    // Proximity Dialog State
    var showProximityAlert by remember { mutableStateOf(false) }
    val duelistName = arenaManager.bleManager.nearbyDuelistFound

    LaunchedEffect(duelistName) {
        if (duelistName != null) {
            showProximityAlert = true
        }
    }

    // Auto-navigate to Duel if PvP status is ACTIVE
    LaunchedEffect(arenaManager.pvpManager.status) {
        if (arenaManager.pvpManager.status == PvPStatus.ACTIVE) {
            onPvPDuel()
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Text("Wristborn", style = MaterialTheme.typography.title2) }
            
            item {
                Text(
                    text = if (arenaManager.isArmed) "Arena: ARMED" else "Arena: PASSIVE",
                    color = if (arenaManager.isArmed) Color.Red else Color.Gray,
                    style = MaterialTheme.typography.caption2
                )
            }

            item {
                Button(
                    onClick = { arenaManager.updateArmedState(!arenaManager.isArmed) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(if (arenaManager.isArmed) "DISARM" else "ARM")
                }
            }

            if (arenaManager.isArmed) {
                item {
                    Text(
                        text = "Last Gesture: ${arenaManager.lastGesture?.name ?: "None"}",
                        style = MaterialTheme.typography.body2
                    )
                }
                
                if (arenaManager.pvpManager.status == PvPStatus.NEGOTIATING) {
                    item { CircularProgressIndicator() }
                    item { Text("Connecting...", style = MaterialTheme.typography.caption2) }
                }
            }

            item {
                Button(onClick = onTraining, modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text("Training")
                }
            }
            
            item {
                Button(onClick = onPractice, modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text("Practice Duel")
                }
            }

            item {
                Button(onClick = onDailyTrial, modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text("Daily Trial")
                }
            }

            item {
                Button(onClick = onProfile, modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text("Profile")
                }
            }
        }

        // Proximity Alert Dialog
        Dialog(
            showDialog = showProximityAlert,
            onDismissRequest = { 
                showProximityAlert = false 
                arenaManager.bleManager.clearDuelist()
            }
        ) {
            Alert(
                title = { Text("Duelist Nearby") },
                content = { Text(duelistName ?: "Unknown") },
                positiveButton = {
                    Button(onClick = { 
                        showProximityAlert = false
                        // Start real GATT connection and handshake
                        val device = arenaManager.bleManager.discoveredDevice
                        if (device != null) {
                            arenaManager.bleManager.connectToDuelist(device) { event ->
                                arenaManager.pvpManager.onEventReceived(event)
                            }
                            arenaManager.pvpManager.startHandshake()
                        }
                    }) {
                        Text("Duel")
                    }
                },
                negativeButton = {
                    Button(onClick = { 
                        showProximityAlert = false
                        arenaManager.bleManager.clearDuelist()
                    }) {
                        Text("Ignore")
                    }
                }
            )
        }
    }
}
