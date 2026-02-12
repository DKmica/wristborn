package com.wristborn.app.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.wristborn.app.engine.Element

class ElementHapticsPlayer(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    fun playElementSignature(element: Element) {
        val timings = when (element) {
            Element.FIRE -> longArrayOf(0, 40, 40, 40, 40, 40)
            Element.WIND -> longArrayOf(0, 180, 70, 60, 70, 180)
            Element.ARCANE -> longArrayOf(0, 80, 70, 80, 70, 80)
            Element.VOID -> longArrayOf(0, 240, 80, 30)
            Element.STORM -> longArrayOf(0, 30, 25, 55, 20, 35, 20, 65)
            Element.EARTH -> longArrayOf(0, 160, 120, 170)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = IntArray(timings.size) { index ->
                if (index % 2 == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE
            }
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings, -1)
        }
    }
}
