package com.wristborn.app.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wristborn.app.FeatureFlags
import com.wristborn.app.ble.BleManager
import com.wristborn.app.data.ProgressionManager
import com.wristborn.app.haptics.ElementHapticsPlayer
import com.wristborn.app.haptics.HapticRhythmPlayer
import com.wristborn.app.sensors.GestureRecognizer
import com.wristborn.app.sensors.GestureType

class ArenaManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    private val baseRecognizer = GestureRecognizer()
    private val v1Recognizer = SimplifiedGestureRecognizer(baseRecognizer)
    private val primeDetector = SigilPrimeDetector()
    private val elementHaptics = ElementHapticsPlayer(context)
    private val rhythmPlayer = HapticRhythmPlayer(context)
    
    val bleManager = BleManager(context)
    val pvpManager = PvPManager(bleManager)
    val pvpManagerV1 = PvPManagerV1(bleManager)
    val progressionManager = ProgressionManager(context)

    var isArmed by mutableStateOf(false)
        private set
    
    var lastGesture by mutableStateOf<GestureType?>(null)
        private set

    var isPrimed by mutableStateOf(false)
        private set
    
    private var primeExpiredAt = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val autoDisarmRunnable = Runnable { updateArmedState(false) }

    fun updateArmedState(armed: Boolean) {
        if (isArmed == armed) return
        isArmed = armed
        if (armed) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
            bleManager.startDiscovery()
            resetInactivityTimer()
        } else {
            sensorManager.unregisterListener(this)
            bleManager.stopDiscovery()
            if (FeatureFlags.V1_VIRAL_MODE) pvpManagerV1.reset() else pvpManager.reset()
            lastGesture = null
            isPrimed = false
            handler.removeCallbacks(autoDisarmRunnable)
        }
    }

    private fun resetInactivityTimer() {
        handler.removeCallbacks(autoDisarmRunnable)
        handler.postDelayed(autoDisarmRunnable, 120_000) // 2 minutes
    }

    fun handleTap() {
        if (!isArmed) return
        resetInactivityTimer()
        val now = System.currentTimeMillis()
        if (primeDetector.onTap(now)) {
            isPrimed = true
            primeExpiredAt = now + 1200
            // Haptic: two quick pulses for prime
            rhythmPlayer.playSigil(listOf(SigilToken.SHORT, SigilToken.SHORT))
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isArmed) return
        
        val now = System.currentTimeMillis()
        
        if (isPrimed && now > primeExpiredAt) {
            isPrimed = false
        }

        if (FeatureFlags.V1_VIRAL_MODE && !isPrimed) return

        val result = when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                if (FeatureFlags.V1_VIRAL_MODE) v1Recognizer.onAccelerometer(event.values[0], event.values[1], event.values[2], now)
                else baseRecognizer.onAccelerometer(event.values[0], event.values[1], event.values[2], now)
            }
            Sensor.TYPE_GYROSCOPE -> {
                if (FeatureFlags.V1_VIRAL_MODE) v1Recognizer.onGyroscope(event.values[2], now)
                else baseRecognizer.onGyroscope(event.values[2], now)
            }
            else -> null
        }

        if (result != null && result.confidence >= GestureRecognizer.MIN_CONFIDENCE) {
            if (lastGesture != result.type) {
                lastGesture = result.type
                val element = mapGestureToElement(result.type)
                if (element != null) {
                    elementHaptics.playElementSignature(element)
                    if (FeatureFlags.V1_VIRAL_MODE) {
                        isPrimed = false
                        resetInactivityTimer()
                    }
                }
            }
        }
    }

    private fun mapGestureToElement(gesture: GestureType): Element? {
        return when (gesture) {
            GestureType.FLICK_RIGHT -> Element.FIRE
            GestureType.FLICK_LEFT -> Element.WIND
            GestureType.STEADY_HOLD -> Element.EARTH
            else -> if (FeatureFlags.V1_VIRAL_MODE) null else when (gesture) {
                GestureType.TWIST_CW -> Element.ARCANE
                GestureType.TWIST_CCW -> Element.VOID
                GestureType.SHAKE -> Element.STORM
                else -> null
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
