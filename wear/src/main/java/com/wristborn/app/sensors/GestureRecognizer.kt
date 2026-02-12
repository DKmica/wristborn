package com.wristborn.app.sensors

import kotlin.math.abs

class GestureRecognizer {
    private data class MotionSample(
        val timestampMs: Long,
        val ax: Float,
        val ay: Float,
        val az: Float,
        val gz: Float
    )

    private val samples = ArrayDeque<MotionSample>()
    private var latestAccel = floatArrayOf(0f, 0f, 9.8f)
    private var latestGyroZ = 0f
    private var lowMotionStartMs: Long? = null

    fun onAccelerometer(ax: Float, ay: Float, az: Float, timestampMs: Long): GestureResult? {
        latestAccel[0] = ax
        latestAccel[1] = ay
        latestAccel[2] = az
        return pushAndEvaluate(timestampMs)
    }

    fun onGyroscope(gz: Float, timestampMs: Long): GestureResult? {
        latestGyroZ = gz
        return pushAndEvaluate(timestampMs)
    }

    private fun pushAndEvaluate(timestampMs: Long): GestureResult? {
        samples.addLast(
            MotionSample(
                timestampMs = timestampMs,
                ax = latestAccel[0],
                ay = latestAccel[1],
                az = latestAccel[2],
                gz = latestGyroZ
            )
        )
        trimOldSamples(timestampMs)
        return detectGesture(timestampMs)
    }

    private fun trimOldSamples(timestampMs: Long) {
        val cutoff = timestampMs - WINDOW_MS
        while (samples.isNotEmpty() && samples.first().timestampMs < cutoff) {
            samples.removeFirst()
        }
    }

    private fun detectGesture(timestampMs: Long): GestureResult? {
        if (samples.isEmpty()) return null

        val maxAx = samples.maxOf { it.ax }
        val minAx = samples.minOf { it.ax }
        val maxGz = samples.maxOf { it.gz }
        val minGz = samples.minOf { it.gz }

        if (maxAx >= FLICK_THRESHOLD) {
            return GestureResult(
                type = GestureType.FLICK_RIGHT,
                confidence = (maxAx / (FLICK_THRESHOLD * 1.2f)).coerceIn(0f, 1f)
            )
        }

        if (minAx <= -FLICK_THRESHOLD) {
            return GestureResult(
                type = GestureType.FLICK_LEFT,
                confidence = (abs(minAx) / (FLICK_THRESHOLD * 1.2f)).coerceIn(0f, 1f)
            )
        }

        if (maxGz >= TWIST_THRESHOLD) {
            return GestureResult(
                type = GestureType.TWIST_CW,
                confidence = (maxGz / (TWIST_THRESHOLD * 1.2f)).coerceIn(0f, 1f)
            )
        }

        if (minGz <= -TWIST_THRESHOLD) {
            return GestureResult(
                type = GestureType.TWIST_CCW,
                confidence = (abs(minGz) / (TWIST_THRESHOLD * 1.2f)).coerceIn(0f, 1f)
            )
        }

        val shakeScore = (maxAx - minAx) / SHAKE_THRESHOLD
        if (shakeScore >= 1f) {
            return GestureResult(
                type = GestureType.SHAKE,
                confidence = (shakeScore / 1.3f).coerceIn(0f, 1f)
            )
        }

        val currentAccelMagnitude = abs(latestAccel[0]) + abs(latestAccel[1]) + abs(latestAccel[2] - 9.8f)
        val currentGyroMagnitude = abs(latestGyroZ)
        val inLowMotion = currentAccelMagnitude <= LOW_MOTION_ACCEL_DELTA && currentGyroMagnitude <= LOW_MOTION_GYRO_DELTA

        if (inLowMotion) {
            if (lowMotionStartMs == null) {
                lowMotionStartMs = timestampMs
            }
            val holdDurationMs = timestampMs - (lowMotionStartMs ?: timestampMs)
            if (holdDurationMs >= STEADY_HOLD_MS) {
                return GestureResult(type = GestureType.STEADY_HOLD, confidence = 0.95f)
            }
        } else {
            lowMotionStartMs = null
        }

        return null
    }

    companion object {
        private const val WINDOW_MS = 300L
        private const val STEADY_HOLD_MS = 800L
        private const val FLICK_THRESHOLD = 12f
        private const val TWIST_THRESHOLD = 4f
        private const val SHAKE_THRESHOLD = 16f
        private const val LOW_MOTION_ACCEL_DELTA = 1.6f
        private const val LOW_MOTION_GYRO_DELTA = 0.4f

        const val MIN_CONFIDENCE = 0.8f
    }
}
