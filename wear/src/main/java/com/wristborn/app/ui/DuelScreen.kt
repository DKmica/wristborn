package com.wristborn.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.wristborn.app.engine.SigilToken
import com.wristborn.app.haptics.HapticRhythmPlayer

private const val LONG_PRESS_THRESHOLD_MS = 220L

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sigilPattern = remember { mutableStateListOf<SigilToken>() }
    val hapticsPlayer = remember(context) { HapticRhythmPlayer(context) }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = "Sigil Field",
                style = MaterialTheme.typography.title3
            )
        }
        item {
            SigilCapturePad(
                onShortTap = { sigilPattern += SigilToken.SHORT },
                onLongPress = { sigilPattern += SigilToken.LONG }
            )
        }
        item {
            val debugPattern = sigilPattern.joinToString(" ") { token ->
                if (token == SigilToken.SHORT) "•" else "—"
            }.ifEmpty { "(empty)" }
            Text(debugPattern)
        }
        item {
            Button(onClick = { sigilPattern.clear() }) {
                Text("Clear")
            }
        }
        item {
            Button(
                enabled = sigilPattern.isNotEmpty(),
                onClick = { hapticsPlayer.playSigil(sigilPattern.toList()) }
            ) {
                Text("Submit Sigil")
            }
        }
        item {
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun SigilCapturePad(
    onShortTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(72.dp)
            .background(color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        val pressDuration = up.uptimeMillis - down.uptimeMillis
                        if (pressDuration >= LONG_PRESS_THRESHOLD_MS) {
                            onLongPress()
                        } else {
                            onShortTap()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Tap / Hold")
    }
}
