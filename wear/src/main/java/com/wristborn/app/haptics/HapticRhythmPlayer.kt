package com.wristborn.app.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.wristborn.app.engine.SigilToken

class HapticRhythmPlayer(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    fun playSigil(pattern: List<SigilToken>) {
        if (pattern.isEmpty()) return

        val timings = mutableListOf<Long>()
        timings += 0L
        pattern.forEachIndexed { index, token ->
            timings += if (token == SigilToken.SHORT) 60L else 180L
            if (index != pattern.lastIndex) {
                timings += 90L
            }
        }

        val amplitudes = mutableListOf<Int>()
        amplitudes += 0
        pattern.forEachIndexed { index, _ ->
            amplitudes += VibrationEffect.DEFAULT_AMPLITUDE
            if (index != pattern.lastIndex) {
                amplitudes += 0
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), amplitudes.toIntArray(), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings.toLongArray(), -1)
        }
    }
}
