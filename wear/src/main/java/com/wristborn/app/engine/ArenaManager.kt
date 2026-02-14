package com.wristborn.app.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wristborn.app.ble.BleManager
import com.wristborn.app.haptics.ElementHapticsPlayer
import com.wristborn.app.sensors.GestureRecognizer
import com.wristborn.app.sensors.GestureType

class ArenaManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    private val recognizer = GestureRecognizer()
    private val haptics = ElementHapticsPlayer(context)
    val bleManager = BleManager(context)

    var isArmed by mutableStateOf(false)
        private set
    
    var lastGesture by mutableStateOf<GestureType?>(null)
        private set

    fun updateArmedState(armed: Boolean) {
        if (isArmed == armed) return
        isArmed = armed
        if (armed) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
            bleManager.startDiscovery()
        } else {
            sensorManager.unregisterListener(this)
            bleManager.stopDiscovery()
            lastGesture = null
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isArmed) return
        
        val now = System.currentTimeMillis()
        val result = when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> recognizer.onAccelerometer(event.values[0], event.values[1], event.values[2], now)
            Sensor.TYPE_GYROSCOPE -> recognizer.onGyroscope(event.values[2], now)
            else -> null
        }

        if (result != null && result.confidence >= GestureRecognizer.MIN_CONFIDENCE) {
            if (lastGesture != result.type) {
                lastGesture = result.type
                val element = mapGestureToElement(result.type)
                if (element != null) {
                    haptics.playElementSignature(element)
                }
            }
        }
    }

    private fun mapGestureToElement(gesture: GestureType): Element? {
        return when (gesture) {
            GestureType.FLICK_RIGHT -> Element.FIRE
            GestureType.FLICK_LEFT -> Element.WIND
            GestureType.TWIST_CW -> Element.ARCANE
            GestureType.TWIST_CCW -> Element.VOID
            GestureType.SHAKE -> Element.STORM
            GestureType.STEADY_HOLD -> Element.EARTH
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
