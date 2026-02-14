package com.wristborn.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.wristborn.app.engine.ArenaManager
import com.wristborn.app.engine.DuelEngineV1
import com.wristborn.app.engine.DuelSnapshotV1
import kotlinx.coroutines.delay

@Composable
fun DuelV1Screen(
    arenaManager: ArenaManager,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    val duelEngine = remember { DuelEngineV1(isPvP = false) }
    var state by remember { mutableStateOf(duelEngine.snapshot()) }

    // Start sensors & Arm for duel
    DisposableEffect(Unit) {
        arenaManager.updateArmedState(true)
        onDispose {
            arenaManager.updateArmedState(false)
        }
    }

    // Engine Tick
    LaunchedEffect(Unit) {
        while (!state.isFinished) {
            state = duelEngine.tick(System.currentTimeMillis())
            delay(100)
        }
        delay(1500) // Brief pause to see result
        onResult(state.winner ?: "DRAW")
    }

    // Gesture casting listener
    LaunchedEffect(arenaManager.lastGesture) {
        if (arenaManager.isPrimed) {
            if (arenaManager.lastGesture != null) {
                state = duelEngine.applyPlayerCast()
            }
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // HP Rings
            HPRing(hp = state.playerHp, color = Color.Green, modifier = Modifier.fillMaxSize(0.95f))
            HPRing(hp = state.opponentHp, color = Color.Red, modifier = Modifier.fillMaxSize(0.85f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${state.remainingMs / 1000}s",
                    style = MaterialTheme.typography.caption1,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                SigilField(
                    modifier = Modifier.size(80.dp),
                    onTokenCaptured = { },
                    onTap = { arenaManager.handleTap() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.logLine,
                    style = MaterialTheme.typography.caption2,
                    color = if (arenaManager.isPrimed) Color.Cyan else Color.White
                )
            }
        }
    }
}

@Composable
fun HPRing(hp: Int, color: Color, modifier: Modifier = Modifier) {
    val animatedHp by animateFloatAsState(targetValue = hp / 100f)
    CircularProgressIndicator(
        progress = animatedHp,
        modifier = modifier,
        startAngle = 270f,
        indicatorColor = color,
        trackColor = color.copy(alpha = 0.1f),
        strokeWidth = 4.dp
    )
}
