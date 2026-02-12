package com.wristborn.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.wristborn.app.sensors.GestureType
import kotlinx.coroutines.delay

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val listState = rememberScalingLazyListState()
    var duelEngine by remember { mutableStateOf(DuelEngine()) }
    var duel by remember { mutableStateOf(duelEngine.snapshot()) }

    LaunchedEffect(duel.isFinished) {
        while (!duel.isFinished) {
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
                    duelEngine = DuelEngine()
                    duel = duelEngine.snapshot()
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
