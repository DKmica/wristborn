package com.wristborn.app.engine

class SigilPrimeDetector {
    private var lastTapTime = 0L
    private var tapCount = 0

    fun onTap(now: Long): Boolean {
        if (now - lastTapTime < 900) {
            tapCount++
        } else {
            tapCount = 1
        }
        lastTapTime = now

        if (tapCount >= 2) {
            tapCount = 0
            return true
        }
        return false
    }
    
    fun reset() {
        tapCount = 0
    }
}
