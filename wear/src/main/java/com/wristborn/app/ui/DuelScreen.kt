package com.wristborn.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.wristborn.app.engine.Element
import com.wristborn.app.engine.SigilToken
import com.wristborn.app.haptics.ElementHapticsPlayer
import com.wristborn.app.haptics.HapticRhythmPlayer
import com.wristborn.app.sensors.GestureRecognizer
import com.wristborn.app.sensors.GestureType

private const val LONG_PRESS_THRESHOLD_MS = 220L

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sigilPattern = remember { mutableStateListOf<SigilToken>() }
    val hapticsPlayer = remember(context) { HapticRhythmPlayer(context) }
    val elementHapticsPlayer = remember(context) { ElementHapticsPlayer(context) }
    val gestureRecognizer = remember { GestureRecognizer() }

    var awaitingElement by remember { mutableStateOf(false) }
    var selectedElement by remember { mutableStateOf<Element?>(null) }

    DuelSensorsEffect(
        context = context,
        enabled = awaitingElement && selectedElement == null,
        onGestureDetected = { gestureType, confidence ->
            if (confidence >= GestureRecognizer.MIN_CONFIDENCE && selectedElement == null) {
                selectedElement = gestureType.toElement()
                awaitingElement = false
                elementHapticsPlayer.playElementSignature(selectedElement ?: return@DuelSensorsEffect)
            }
        },
        gestureRecognizer = gestureRecognizer
    )

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
            Button(onClick = {
                sigilPattern.clear()
                awaitingElement = false
                selectedElement = null
            }) {
                Text("Clear")
            }
        }
        item {
            Button(
                enabled = sigilPattern.isNotEmpty(),
                onClick = {
                    hapticsPlayer.playSigil(sigilPattern.toList())
                    awaitingElement = true
                    selectedElement = null
                }
            ) {
                Text("Submit Sigil")
            }
        }
        if (awaitingElement) {
            item { Text("Perform Element Gesture") }
        }

        item {
            Text("Element: ${selectedElement?.name ?: "None"}")
        }

        item {
            DebugElementButtons(
                onPick = { picked ->
                    selectedElement = picked
                    awaitingElement = false
                    elementHapticsPlayer.playElementSignature(picked)
                }
            )
        }

        item {
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun DuelSensorsEffect(
    context: Context,
    enabled: Boolean,
    gestureRecognizer: GestureRecognizer,
    onGestureDetected: (GestureType, Float) -> Unit
) {
    DisposableEffect(enabled) {
        if (!enabled) {
            onDispose { }
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val timestampMs = event.timestamp / 1_000_000L
                    val result = when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            gestureRecognizer.onAccelerometer(
                                ax = event.values[0],
                                ay = event.values[1],
                                az = event.values[2],
                                timestampMs = timestampMs
                            )
                        }

                        Sensor.TYPE_GYROSCOPE -> {
                            gestureRecognizer.onGyroscope(
                                gz = event.values[2],
                                timestampMs = timestampMs
                            )
                        }

                        else -> null
                    }
                    if (result != null) {
                        onGestureDetected(result.type, result.confidence)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            if (accelSensor != null) {
                sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            }
            if (gyroSensor != null) {
                sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }
}

@Composable
private fun DebugElementButtons(onPick: (Element) -> Unit) {
    ScalingLazyColumn(
        modifier = Modifier.height(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Button(onClick = { onPick(Element.FIRE) }) { Text("FIRE") } }
        item { Button(onClick = { onPick(Element.WIND) }) { Text("WIND") } }
        item { Button(onClick = { onPick(Element.ARCANE) }) { Text("ARCANE") } }
        item { Button(onClick = { onPick(Element.VOID) }) { Text("VOID") } }
        item { Button(onClick = { onPick(Element.STORM) }) { Text("STORM") } }
        item { Button(onClick = { onPick(Element.EARTH) }) { Text("EARTH") } }
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

private fun GestureType.toElement(): Element {
    return when (this) {
        GestureType.FLICK_RIGHT -> Element.FIRE
        GestureType.FLICK_LEFT -> Element.WIND
        GestureType.TWIST_CW -> Element.ARCANE
        GestureType.TWIST_CCW -> Element.VOID
        GestureType.SHAKE -> Element.STORM
        GestureType.STEADY_HOLD -> Element.EARTH
    }
}
