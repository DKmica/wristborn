package com.wristborn.app.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.wristborn.app.engine.TapUnit

class SigilInputHaptics(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    fun vibrateShortTap() {
        vibrateOneShot(SHORT_MS)
    }

    fun vibrateLongPress() {
        vibrateOneShot(LONG_MS)
    }

    fun playCapturedRhythm(pattern: List<TapUnit>) {
        if (pattern.isEmpty()) return

        val timings = mutableListOf<Long>()
        timings += 0L
        pattern.forEachIndexed { index, tap ->
            timings += if (tap == TapUnit.SHORT) SHORT_MS else LONG_MS
            if (index != pattern.lastIndex) {
                timings += GAP_MS
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = IntArray(timings.size) { i ->
                if (i % 2 == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE
            }
            vibrator?.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings.toLongArray(), -1)
        }
    }

    private fun vibrateOneShot(durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }

    companion object {
        private const val SHORT_MS = 60L
        private const val LONG_MS = 180L
        private const val GAP_MS = 90L
    }
}
