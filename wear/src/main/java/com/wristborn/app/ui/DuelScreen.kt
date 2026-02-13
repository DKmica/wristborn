package com.wristborn.app.ui

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import com.wristborn.app.engine.DuelEngine
import com.wristborn.app.engine.FormType
import com.wristborn.app.engine.SigilToken
import com.wristborn.app.engine.Spell
import com.wristborn.app.engine.TapUnit
import com.wristborn.app.haptics.SigilInputHaptics
import com.wristborn.app.sensors.GestureType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val LONG_PRESS_THRESHOLD_MS = 220L

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptics = remember { SigilInputHaptics(context) }
    val listState = rememberScalingLazyListState()
    var duelEngine by remember { mutableStateOf(DuelEngine().also { it.start(System.currentTimeMillis()) }) }
    var duel by remember { mutableStateOf(duelEngine.snapshot()) }
    val capturedPattern = remember { mutableStateListOf<TapUnit>() }

    LaunchedEffect(duelEngine) {
        while (isActive && !duel.isFinished) {
            delay(1_000)
            duel = duelEngine.tick(System.currentTimeMillis())
        }
    }

    fun cast(form: FormType) {
        val spell = Spell(
            sigil = listOf(SigilToken.SHORT, SigilToken.LONG),
            element = com.wristborn.app.engine.Element.ARCANE,
            form = form,
            release = GestureType.TWIST_CW,
            timestampMs = System.currentTimeMillis()
        )
        duel = duelEngine.applyPlayerSpell(spell)
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(state = listState) {
            item { Text("Live Duel") }
            item { Text("You ${duel.playerHp} HP • ${duel.playerMana} MP") }
            item { Text("Dummy ${duel.dummyHp} HP • ${duel.dummyMana} MP") }
            item { Text("${duel.remainingMs / 1000}s left") }
            item { Text(if (duel.inSuddenDeath) "SUDDEN DEATH" else "") }
            item { Text(duel.logLine) }

            item {
                Text("Sigil Field")
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(Color.DarkGray)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val startedAt = SystemClock.elapsedRealtime()
                                    val released = tryAwaitRelease()
                                    if (!released) return@detectTapGestures

                                    val pressDuration = SystemClock.elapsedRealtime() - startedAt
                                    val tapUnit = if (pressDuration >= LONG_PRESS_THRESHOLD_MS) TapUnit.LONG else TapUnit.SHORT
                                    capturedPattern.add(tapUnit)
                                    if (tapUnit == TapUnit.LONG) {
                                        haptics.vibrateLongPress()
                                    } else {
                                        haptics.vibrateShortTap()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tap / Hold")
                }
            }
            item { Text("Pattern: ${patternText(capturedPattern)}") }
            item {
                Button(onClick = { capturedPattern.clear() }) {
                    Text("Clear")
                }
            }
            item {
                Button(
                    onClick = { haptics.playCapturedRhythm(capturedPattern.toList()) },
                    enabled = capturedPattern.isNotEmpty()
                ) {
                    Text("Submit Sigil")
                }
            }

            item {
                Button(onClick = { cast(FormType.SINGLE) }, enabled = !duel.isFinished) {
                    Text("Quick Cast")
                }
            }
            item {
                Button(onClick = { cast(FormType.CHARGED) }, enabled = !duel.isFinished) {
                    Text("Charged Cast")
                }
            }
            item {
                Button(onClick = {
                    duelEngine = DuelEngine().also { it.start(System.currentTimeMillis()) }
                    duel = duelEngine.snapshot()
                    capturedPattern.clear()
                }) {
                    Text("Reset Duel")
                }
            }
            item {
                Button(onClick = onBack) { Text("Back") }
            }
        }
    }
}

private fun patternText(pattern: List<TapUnit>): String {
    if (pattern.isEmpty()) return "(empty)"
    return pattern.joinToString(" ") { unit ->
        if (unit == TapUnit.SHORT) "•" else "—"
    }
}
