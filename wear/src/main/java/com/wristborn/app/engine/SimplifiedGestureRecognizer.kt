package com.wristborn.app.engine

import com.wristborn.app.sensors.GestureRecognizer
import com.wristborn.app.sensors.GestureResult
import com.wristborn.app.sensors.GestureType

class SimplifiedGestureRecognizer(private val baseRecognizer: GestureRecognizer) {
    
    fun onAccelerometer(ax: Float, ay: Float, az: Float, timestampMs: Long): GestureResult? {
        val result = baseRecognizer.onAccelerometer(ax, ay, az, timestampMs)
        return filterResult(result)
    }

    fun onGyroscope(gz: Float, timestampMs: Long): GestureResult? {
        val result = baseRecognizer.onGyroscope(gz, timestampMs)
        return filterResult(result)
    }

    private fun filterResult(result: GestureResult?): GestureResult? {
        if (result == null) return null
        return when (result.type) {
            GestureType.FLICK_RIGHT,
            GestureType.FLICK_LEFT,
            GestureType.STEADY_HOLD -> result
            else -> null
        }
    }
}
