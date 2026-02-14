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
import kotlin.random.Random

@Composable
fun PvPDuelScreen(
    arenaManager: ArenaManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { ElementHapticsPlayer(context) }
    val listState = rememberScalingLazyListState()
    
    // Initialize PvP Duel Engine
    var duelEngine by remember { mutableStateOf(DuelEngine(isPvP = true)) }
    var duel by remember { mutableStateOf(duelEngine.snapshot()) }

    // Connect PvPManager to this Engine instance
    LaunchedEffect(Unit) {
        arenaManager.pvpManager.setDuelEngine(duelEngine)
    }

    // Casting State
    var sigilSequence by remember { mutableStateOf(listOf<SigilToken>()) }
    var detectedElement by remember { mutableStateOf<Element?>(null) }
    var formTaps by remember { mutableIntStateOf(0) }

    // Sync detection
    LaunchedEffect(arenaManager.lastGesture) {
        val gesture = arenaManager.lastGesture
        if (gesture != null) {
            val element = when (gesture) {
                GestureType.FLICK_RIGHT -> Element.FIRE
                GestureType.FLICK_LEFT -> Element.WIND
                GestureType.TWIST_CW -> Element.ARCANE
                GestureType.TWIST_CCW -> Element.VOID
                GestureType.SHAKE -> Element.STORM
                GestureType.STEADY_HOLD -> Element.EARTH
            }
            if (detectedElement == null) {
                detectedElement = element
            } else if (detectedElement == element && sigilSequence.isNotEmpty()) {
                val spell = Spell(
                    sigil = sigilSequence,
                    element = detectedElement!!,
                    form = if (formTaps > 2) FormType.CHARGED else FormType.SINGLE,
                    release = gesture,
                    timestampMs = System.currentTimeMillis()
                )
                
                // 1. Calculate damage locally
                val damage = if (spell.form == FormType.CHARGED) Random.nextInt(25, 31) else Random.nextInt(12, 19)
                val hash = spell.hashCode()
                
                // 2. Broadcast to opponent
                arenaManager.pvpManager.broadcastSpell(damage, hash)
                
                // 3. Apply to local engine
                duel = duelEngine.applyPlayerSpell(spell)
                haptics.playReleaseConfirm(detectedElement!!)
                
                // Reset casting state
                sigilSequence = emptyList()
                detectedElement = null
                formTaps = 0
            }
        }
    }

    LaunchedEffect(duel.isFinished) {
        while (!duel.isFinished) {
            delay(500)
            duel = duelEngine.tick(System.currentTimeMillis())
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
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("YOU: ${duel.playerHp}", color = Color.Green)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("vs", style = MaterialTheme.typography.caption2)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("FOE: ${duel.opponentHp}", color = Color.Red)
                }
            }

            item {
                Text(
                    text = "${duel.remainingMs / 1000}s",
                    style = MaterialTheme.typography.title3
                )
            }

            if (!duel.isFinished) {
                item {
                    SigilField(
                        onTokenCaptured = { token ->
                            sigilSequence = (sigilSequence + token).takeLast(4)
                            formTaps++
                        }
                    )
                }
                
                item {
                    Text(
                        text = when {
                            detectedElement != null -> "Release ${detectedElement!!.name}!"
                            sigilSequence.isNotEmpty() -> "Perform Gesture..."
                            else -> "Tap Sigil to start"
                        },
                        style = MaterialTheme.typography.caption1,
                        color = Color.Cyan
                    )
                }
            } else {
                item {
                    Text(duel.logLine, style = MaterialTheme.typography.title2)
                }
                item {
                    Button(onClick = {
                        arenaManager.pvpManager.reset()
                        onBack()
                    }) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}
