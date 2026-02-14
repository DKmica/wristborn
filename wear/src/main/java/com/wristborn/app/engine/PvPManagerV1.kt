package com.wristborn.app.engine

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wristborn.app.ble.BleManager
import com.wristborn.app.ble.DuelEvent
import com.wristborn.app.ble.EventType

enum class PvPStatusV1 {
    IDLE,
    HANDSHAKE,
    COUNTDOWN,
    ACTIVE,
    FINISHED,
    TIMEOUT
}

class PvPManagerV1(private val bleManager: BleManager) {
    var status by mutableStateOf(PvPStatusV1.IDLE)
        private set
    var countdown by mutableIntStateOf(3)
        private set
    
    private var duelEngine: DuelEngineV1? = null
    private val handler = Handler(Looper.getMainLooper())

    fun startHandshake() {
        status = PvPStatusV1.HANDSHAKE
        bleManager.sendEvent(DuelEvent(EventType.HANDSHAKE_REQ, 0L))
    }

    fun onEventReceived(event: DuelEvent) {
        when (event.type) {
            EventType.HANDSHAKE_REQ -> {
                status = PvPStatusV1.HANDSHAKE
                bleManager.sendEvent(DuelEvent(EventType.HANDSHAKE_ACK, 0L))
            }
            EventType.HANDSHAKE_ACK -> {
                if (status == PvPStatusV1.HANDSHAKE) {
                    startCountdown()
                }
            }
            EventType.MATCH_START -> {
                if (status == PvPStatusV1.COUNTDOWN) {
                    status = PvPStatusV1.ACTIVE
                }
            }
            EventType.SPELL_CAST -> {
                duelEngine?.applyOpponentDamage(event.damage)
            }
            EventType.MATCH_END -> {
                status = PvPStatusV1.FINISHED
            }
            else -> {}
        }
    }
    
    private fun startCountdown() {
        status = PvPStatusV1.COUNTDOWN
        countdown = 3
        handler.post(object : Runnable {
            override fun run() {
                if (countdown > 1) {
                    countdown--
                    handler.postDelayed(this, 1000)
                } else {
                    bleManager.sendEvent(DuelEvent(EventType.MATCH_START, 0L))
                    status = PvPStatusV1.ACTIVE
                }
            }
        })
    }
    
    fun setDuelEngine(engine: DuelEngineV1) {
        this.duelEngine = engine
    }

    fun broadcastCast(damage: Int) {
        bleManager.sendEvent(DuelEvent(EventType.SPELL_CAST, 0L, 0, damage))
    }
    
    fun reset() {
        handler.removeCallbacksAndMessages(null)
        status = PvPStatusV1.IDLE
        countdown = 3
        duelEngine = null
    }
}
