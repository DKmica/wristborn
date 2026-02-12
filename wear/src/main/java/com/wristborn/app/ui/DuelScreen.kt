package com.wristborn.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.wristborn.app.engine.DuelEngine
import com.wristborn.app.engine.DuelSnapshot
import com.wristborn.app.engine.Element
import com.wristborn.app.engine.FormType
import com.wristborn.app.engine.SigilToken
import com.wristborn.app.engine.Spell
import com.wristborn.app.engine.SigilToken
import com.wristborn.app.haptics.ElementHapticsPlayer
import com.wristborn.app.haptics.HapticRhythmPlayer
import com.wristborn.app.sensors.GestureRecognizer
import com.wristborn.app.sensors.GestureType
import kotlinx.coroutines.delay

private const val LONG_PRESS_THRESHOLD_MS = 220L

@Composable
fun DuelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sigilPattern = remember { mutableStateListOf<SigilToken>() }
    val formTapTimesMs = remember { mutableStateListOf<Long>() }
    val hapticsPlayer = remember(context) { HapticRhythmPlayer(context) }
    val elementHapticsPlayer = remember(context) { ElementHapticsPlayer(context) }
    val gestureRecognizer = remember { GestureRecognizer() }
    val duelEngine = remember { DuelEngine() }

    var duel by remember { mutableStateOf(duelEngine.snapshot()) }
    var awaitingElement by remember { mutableStateOf(false) }
    var selectedElement by remember { mutableStateOf<Element?>(null) }
    var awaitingForm by remember { mutableStateOf(false) }
    var selectedForm by remember { mutableStateOf<FormType?>(null) }

    var awaitingElement by remember { mutableStateOf(false) }
    var selectedElement by remember { mutableStateOf<Element?>(null) }

    var awaitingForm by remember { mutableStateOf(false) }
    var selectedForm by remember { mutableStateOf<FormType?>(null) }

    var awaitingRelease by remember { mutableStateOf(false) }
    var releaseGesture by remember { mutableStateOf<GestureType?>(null) }
    var latestSpell by remember { mutableStateOf<Spell?>(null) }

    LaunchedEffect(duel.isFinished) {
        while (!duel.isFinished) {
            delay(1000L)
            duel = duelEngine.tick(System.currentTimeMillis())
        }
    }

    DuelSensorsEffect(
        context = context,
        enabled = (awaitingElement || awaitingRelease) && !duel.isFinished,
        onGestureDetected = { gestureType, confidence ->
            if (confidence < GestureRecognizer.MIN_CONFIDENCE) return@DuelSensorsEffect
    DuelSensorsEffect(
        context = context,
        enabled = awaitingElement || awaitingRelease,
        onGestureDetected = { gestureType, confidence ->
            if (confidence < GestureRecognizer.MIN_CONFIDENCE) return@DuelSensorsEffect

            if (awaitingElement && selectedElement == null) {
                selectedElement = gestureType.toElement()
                awaitingElement = false
                awaitingForm = true
                elementHapticsPlayer.playElementSignature(selectedElement ?: return@DuelSensorsEffect)
            } else if (awaitingRelease && selectedElement != null && selectedForm != null && releaseGesture == null) {
                releaseGesture = gestureType
                awaitingRelease = false
                latestSpell = Spell(
                    sigil = sigilPattern.toList(),
                    element = selectedElement ?: return@DuelSensorsEffect,
                    form = selectedForm ?: return@DuelSensorsEffect,
                    release = gestureType,
                    timestampMs = System.currentTimeMillis()
                )
                elementHapticsPlayer.playFormAccent(selectedForm ?: return@DuelSensorsEffect)
                elementHapticsPlayer.playReleaseConfirm(selectedElement ?: return@DuelSensorsEffect)
                duel = duelEngine.applyPlayerSpell(latestSpell ?: return@DuelSensorsEffect)
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item { DuelRings(snapshot = duel) }
        item { Text("Time ${duel.remainingMs / 1000}s ${if (duel.inSuddenDeath) "SD" else ""}") }
        item { Text("HP ${duel.playerHp} / ${duel.dummyHp}  MP ${duel.playerMana}") }
        item { Text(duel.logLine) }

        item { Text(text = "Sigil Field", style = MaterialTheme.typography.title3) }
        item {
            SigilCapturePad(onShortTap = { sigilPattern += SigilToken.SHORT }, onLongPress = { sigilPattern += SigilToken.LONG })
        }
        item {
            val debugPattern = sigilPattern.joinToString(" ") { if (it == SigilToken.SHORT) "•" else "—" }.ifEmpty { "(empty)" }
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text

@Composable
fun DuelScreen(onBack: () -> Unit) {
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(text = "Sigil Field", style = MaterialTheme.typography.title3)
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
                sigilPattern.clear(); formTapTimesMs.clear(); awaitingElement = false; awaitingForm = false; awaitingRelease = false
                selectedElement = null; selectedForm = null; releaseGesture = null; latestSpell = null
            }) { Text("Clear") }
        }
        item {
            Button(enabled = sigilPattern.isNotEmpty() && !duel.isFinished, onClick = {
                hapticsPlayer.playSigil(sigilPattern.toList())
                awaitingElement = true; awaitingForm = false; awaitingRelease = false
                selectedElement = null; selectedForm = null; releaseGesture = null; latestSpell = null; formTapTimesMs.clear()
            }) { Text("Submit Sigil") }
        }

        if (awaitingElement) item { Text("Perform Element Gesture") }
        item { Text("Element: ${selectedElement?.name ?: "None"}") }
        item {
            DebugElementButtons(enabled = awaitingElement && !duel.isFinished, onPick = { picked ->
                selectedElement = picked; awaitingElement = false; awaitingForm = true
                elementHapticsPlayer.playElementSignature(picked)
            })
        }

        if (awaitingForm || selectedForm != null) {
            item { Text(if (selectedForm == null) "Tap Form Pattern" else "Form: ${selectedForm?.name}") }
            item { Button(enabled = awaitingForm, onClick = { formTapTimesMs += System.currentTimeMillis() }) { Text("Form Tap") } }
            item { Text("Form taps: ${formTapTimesMs.size}") }
            item {
                Button(enabled = awaitingForm && formTapTimesMs.isNotEmpty(), onClick = {
                    selectedForm = classifyForm(formTapTimesMs.toList())
                    awaitingForm = false
                    awaitingRelease = true
                    elementHapticsPlayer.playFormAccent(selectedForm ?: return@Button)
                }) { Text("Submit Form") }
            }
        }

        if (awaitingRelease) item { Text("Perform Release Gesture") }
        item { Text("Release: ${releaseGesture?.name ?: "None"}") }
        latestSpell?.let { item { Text("Spell Cast! ${it.element.name} ${it.form.name} ${it.release.name}") } }

        item { Button(onClick = onBack) { Text("Back") } }
    }
}

@Composable
private fun DuelRings(snapshot: DuelSnapshot) {
    val playerSweep = (snapshot.playerHp / 100f) * 360f
    val dummySweep = (snapshot.dummyHp / 100f) * 360f
    Canvas(modifier = Modifier.size(90.dp)) {
        drawArc(
            color = Color(0xFF3DDC84),
            startAngle = -90f,
            sweepAngle = playerSweep,
            useCenter = false,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFFF5252),
            startAngle = -90f,
            sweepAngle = dummySweep,
            useCenter = false,
            topLeft = Offset(10f, 10f),
            size = Size(size.width - 20f, size.height - 20f),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
                sigilPattern.clear()
                formTapTimesMs.clear()
                awaitingElement = false
                awaitingForm = false
                awaitingRelease = false
                selectedElement = null
                selectedForm = null
                releaseGesture = null
                latestSpell = null
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
                    awaitingForm = false
                    awaitingRelease = false
                    selectedElement = null
                    selectedForm = null
                    releaseGesture = null
                    latestSpell = null
                    formTapTimesMs.clear()
                    selectedElement = null
                }
            ) {
                Text("Submit Sigil")
            }
        }
        if (awaitingElement) {
            item { Text("Perform Element Gesture") }
        }

        item { Text("Element: ${selectedElement?.name ?: "None"}") }

        item {
            DebugElementButtons(
                enabled = awaitingElement,
                onPick = { picked ->
                    selectedElement = picked
                    awaitingElement = false
                    awaitingForm = true
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

        if (awaitingForm || selectedForm != null) {
            item {
                Text(if (selectedForm == null) "Tap Form Pattern" else "Form: ${selectedForm?.name}")
            }
            item {
                Button(
                    enabled = awaitingForm,
                    onClick = { formTapTimesMs += System.currentTimeMillis() }
                ) {
                    Text("Form Tap")
                }
            }
            item { Text("Form taps: ${formTapTimesMs.size}") }
            item {
                Button(
                    enabled = awaitingForm && formTapTimesMs.isNotEmpty(),
                    onClick = {
                        selectedForm = classifyForm(formTapTimesMs.toList())
                        awaitingForm = false
                        awaitingRelease = true
                        elementHapticsPlayer.playFormAccent(selectedForm ?: return@Button)
                    }
                ) {
                    Text("Submit Form")
                }
            }
        }

        if (awaitingRelease) {
            item { Text("Perform Release Gesture") }
        }
        item { Text("Release: ${releaseGesture?.name ?: "None"}") }

        latestSpell?.let { spell ->
            item {
                Text("Spell Cast! ${spell.element.name} ${spell.form.name} ${spell.release.name}")
            }
        }

        item { Text("Duel (placeholder)") }
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
        if (!enabled) onDispose { } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
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
                        Sensor.TYPE_ACCELEROMETER -> gestureRecognizer.onAccelerometer(event.values[0], event.values[1], event.values[2], timestampMs)
                        Sensor.TYPE_GYROSCOPE -> gestureRecognizer.onGyroscope(event.values[2], timestampMs)
                        else -> null
                    }
                    result?.let { onGestureDetected(it.type, it.confidence) }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            gyroSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            onDispose { sensorManager.unregisterListener(listener) }
                        Sensor.TYPE_ACCELEROMETER -> gestureRecognizer.onAccelerometer(
                            ax = event.values[0],
                            ay = event.values[1],
                            az = event.values[2],
                            timestampMs = timestampMs
                        )

                        Sensor.TYPE_GYROSCOPE -> gestureRecognizer.onGyroscope(
                            gz = event.values[2],
                            timestampMs = timestampMs
                        )
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

            accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            gyroSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
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
private fun DebugElementButtons(enabled: Boolean, onPick: (Element) -> Unit) {
    ScalingLazyColumn(modifier = Modifier.height(160.dp), horizontalAlignment = Alignment.CenterHorizontally) {
private fun DebugElementButtons(
    enabled: Boolean,
    onPick: (Element) -> Unit
) {
private fun DebugElementButtons(onPick: (Element) -> Unit) {
    ScalingLazyColumn(
        modifier = Modifier.height(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Button(enabled = enabled, onClick = { onPick(Element.FIRE) }) { Text("FIRE") } }
        item { Button(enabled = enabled, onClick = { onPick(Element.WIND) }) { Text("WIND") } }
        item { Button(enabled = enabled, onClick = { onPick(Element.ARCANE) }) { Text("ARCANE") } }
        item { Button(enabled = enabled, onClick = { onPick(Element.VOID) }) { Text("VOID") } }
        item { Button(enabled = enabled, onClick = { onPick(Element.STORM) }) { Text("STORM") } }
        item { Button(enabled = enabled, onClick = { onPick(Element.EARTH) }) { Text("EARTH") } }
        item { Button(onClick = { onPick(Element.FIRE) }) { Text("FIRE") } }
        item { Button(onClick = { onPick(Element.WIND) }) { Text("WIND") } }
        item { Button(onClick = { onPick(Element.ARCANE) }) { Text("ARCANE") } }
        item { Button(onClick = { onPick(Element.VOID) }) { Text("VOID") } }
        item { Button(onClick = { onPick(Element.STORM) }) { Text("STORM") } }
        item { Button(onClick = { onPick(Element.EARTH) }) { Text("EARTH") } }
    }
}

@Composable
private fun SigilCapturePad(onShortTap: () -> Unit, onLongPress: () -> Unit) {
    Box(
        modifier = Modifier.width(140.dp).height(72.dp)
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
                        if (pressDuration >= LONG_PRESS_THRESHOLD_MS) onLongPress() else onShortTap()
                        if (pressDuration >= LONG_PRESS_THRESHOLD_MS) {
                            onLongPress()
                        } else {
                            onShortTap()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) { Text(text = "Tap / Hold") }
}

private fun classifyForm(timestampsMs: List<Long>): FormType {
    if (timestampsMs.size <= 1) return FormType.SINGLE
    val intervals = timestampsMs.zipWithNext { a, b -> b - a }
    val avgInterval = intervals.average()
    ) {
        Text(text = "Tap / Hold")
    }
}

private fun classifyForm(timestampsMs: List<Long>): FormType {
    if (timestampsMs.isEmpty()) return FormType.SINGLE
    if (timestampsMs.size == 1) return FormType.SINGLE

    val intervals = timestampsMs.zipWithNext { a, b -> b - a }
    val avgInterval = intervals.average()

    return when {
        timestampsMs.size >= 4 -> FormType.MULTI
        timestampsMs.size == 3 && avgInterval < 220 -> FormType.PIERCING
        timestampsMs.size == 2 && avgInterval < 180 -> FormType.BURST
        avgInterval > 520 -> FormType.CHARGED
        avgInterval > 300 -> FormType.DELAYED
        else -> FormType.BURST
    }
}

private fun GestureType.toElement(): Element = when (this) {
    GestureType.FLICK_RIGHT -> Element.FIRE
    GestureType.FLICK_LEFT -> Element.WIND
    GestureType.TWIST_CW -> Element.ARCANE
    GestureType.TWIST_CCW -> Element.VOID
    GestureType.SHAKE -> Element.STORM
    GestureType.STEADY_HOLD -> Element.EARTH
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
