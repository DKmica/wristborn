package com.wristborn.app.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.wristborn.app.engine.Element
import com.wristborn.app.engine.FormType

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
        vibrateWaveform(timings)
    }

    fun playFormAccent(form: FormType) {
        val timings = when (form) {
            FormType.SINGLE -> longArrayOf(0, 35)
            FormType.BURST -> longArrayOf(0, 30, 20, 30)
            FormType.PIERCING -> longArrayOf(0, 60)
            FormType.DELAYED -> longArrayOf(0, 20, 80, 35)
            FormType.CHARGED -> longArrayOf(0, 110)
            FormType.MULTI -> longArrayOf(0, 24, 16, 24, 16, 24)
        }
        vibrateWaveform(timings)
    }

    fun playReleaseConfirm(element: Element) {
        val timings = when (element) {
            Element.FIRE -> longArrayOf(0, 30, 20, 30)
            Element.WIND -> longArrayOf(0, 80, 30, 25)
            Element.ARCANE -> longArrayOf(0, 45, 25, 45)
            Element.VOID -> longArrayOf(0, 100, 35, 20)
            Element.STORM -> longArrayOf(0, 20, 15, 25, 15, 30)
            Element.EARTH -> longArrayOf(0, 90, 40, 50)
        }
        vibrateWaveform(timings)
    }

    private fun vibrateWaveform(timings: LongArray) {

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
