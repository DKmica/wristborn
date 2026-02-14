package com.wristborn.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.wristborn.app.data.ProgressionData
import com.wristborn.app.engine.ArenaManager
import com.wristborn.app.engine.PvPStatusV1
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ArenaV1Screen(
    arenaManager: ArenaManager,
    onPractice: () -> Unit,
    onProfile: () -> Unit,
    onPvPDuel: () -> Unit
) {
    var isDigital by remember { mutableStateOf(true) }
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val progression by arenaManager.progressionManager.progressionFlow.collectAsState(initial = ProgressionData())
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormat = if (isDigital) "HH:mm" else "hh:mm"
    val timeStr = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date(currentTime.value))
    val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(currentTime.value))

    // Affinity Color
    val ringColor = when (progression.affinity) {
        "FIRE" -> Color.Red
        "WIND" -> Color.Cyan
        "EARTH" -> Color.Green
        else -> Color.White
    }

    // Streak "Flame" Intensity
    val isFlame = progression.winStreak >= 3
    val duration = if (isFlame) 800 else 1500
    
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isFlame) 0.4f else 0.2f,
        targetValue = if (isFlame) 1.0f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Proximity Dialog State
    var showProximityAlert by remember { mutableStateOf(false) }
    val duelistName = arenaManager.bleManager.nearbyDuelistFound

    LaunchedEffect(duelistName) {
        if (duelistName != null) {
            showProximityAlert = true
        }
    }

    LaunchedEffect(arenaManager.pvpManagerV1.status) {
        if (arenaManager.pvpManagerV1.status == PvPStatusV1.ACTIVE) {
            onPvPDuel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Armed Glowing Ring
        if (arenaManager.isArmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.95f)
                    .graphicsLayer(alpha = glowAlpha)
                    .drawBehind {
                        drawCircle(
                            color = ringColor,
                            style = Stroke(width = (if (isFlame) 6.dp else 4.dp).toPx())
                        )
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.caption2,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = timeStr,
                style = MaterialTheme.typography.display1.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable { isDigital = !isDigital }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { arenaManager.updateArmedState(!arenaManager.isArmed) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (arenaManager.isArmed) ringColor else Color.DarkGray
                ),
                modifier = Modifier.size(64.dp)
            ) {
                Text(if (arenaManager.isArmed) "DISARM" else "ARM")
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompactButton(
                onClick = onPractice,
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Text("P", fontWeight = FontWeight.Bold)
            }
            CompactButton(
                onClick = onProfile,
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Text("U", fontWeight = FontWeight.Bold)
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
            title = { Text("DUELIST NEARBY") },
            content = { Text(duelistName ?: "Unknown") },
            positiveButton = {
                Button(onClick = { 
                    showProximityAlert = false
                    val device = arenaManager.bleManager.discoveredDevice
                    if (device != null) {
                        arenaManager.bleManager.connectToDuelist(device) { event ->
                            arenaManager.pvpManagerV1.onEventReceived(event)
                        }
                        arenaManager.pvpManagerV1.startHandshake()
                    }
                }) {
                    Text("ACCEPT")
                }
            },
            negativeButton = {
                Button(onClick = { 
                    showProximityAlert = false
                    arenaManager.bleManager.clearDuelist()
                }) {
                    Text("IGNORE")
                }
            }
        )
    }
}
