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

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val arenaManager = remember { ArenaManager(context) }
    val haptics = remember { ElementHapticsPlayer(context) }
    val listState = rememberScalingLazyListState()
    var duelEngine by remember { mutableStateOf(DuelEngine()) }
    var duel by remember { mutableStateOf(duelEngine.snapshot()) }

    // Casting State
    var sigilSequence by remember { mutableStateOf(listOf<SigilToken>()) }
    var detectedElement by remember { mutableStateOf<Element?>(null) }
    var formTaps by remember { mutableIntStateOf(0) }

    // Activate sensors when in Duel
    DisposableEffect(Unit) {
        arenaManager.updateArmedState(true)
        onDispose { arenaManager.updateArmedState(false) }
    }

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
                // This counts as the "Release" if it's the same gesture again or a strong one
                // For MVP: if we have Sigil + Element, any gesture acts as Release
                val spell = Spell(
                    sigil = sigilSequence,
                    element = detectedElement!!,
                    form = if (formTaps > 2) FormType.CHARGED else FormType.SINGLE,
                    release = gesture,
                    timestampMs = System.currentTimeMillis()
                )
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
                    Text("HP: ${duel.playerHp}", color = Color.Green)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("vs", style = MaterialTheme.typography.caption2)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Dummy: ${duel.opponentHp}", color = Color.Red)
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
                        },
                        modifier = Modifier.size(80.dp)
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
                        duelEngine = DuelEngine()
                        duel = duelEngine.snapshot()
                    }) {
                        Text("Rematch")
                    }
                }
            }

            item {
                Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Quit")
                }
            }
        }
    }
}
