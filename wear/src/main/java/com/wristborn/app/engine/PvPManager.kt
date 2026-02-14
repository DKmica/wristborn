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

enum class PvPStatus {
    IDLE,
    NEGOTIATING,
    COUNTDOWN,
    ACTIVE,
    FINISHED,
    DISCONNECTED,
    TIMEOUT
}

class PvPManager(private val bleManager: BleManager) {
    var status by mutableStateOf(PvPStatus.IDLE)
        private set
    var countdown by mutableIntStateOf(3)
        private set
    
    private var duelEngine: DuelEngine? = null
    private val handler = Handler(Looper.getMainLooper())
    private val handshakeTimeoutRunnable = Runnable {
        if (status == PvPStatus.NEGOTIATING) {
            status = PvPStatus.TIMEOUT
            bleManager.close()
        }
    }

    fun startHandshake() {
        status = PvPStatus.NEGOTIATING
        bleManager.sendEvent(DuelEvent(EventType.HANDSHAKE_REQ, 0L))
        handler.postDelayed(handshakeTimeoutRunnable, 10000) // 10s timeout
    }

    fun onEventReceived(event: DuelEvent) {
        when (event.type) {
            EventType.HANDSHAKE_REQ -> {
                status = PvPStatus.NEGOTIATING
                bleManager.sendEvent(DuelEvent(EventType.HANDSHAKE_ACK, 0L))
                handler.postDelayed(handshakeTimeoutRunnable, 10000)
            }
            EventType.HANDSHAKE_ACK -> {
                if (status == PvPStatus.NEGOTIATING) {
                    handler.removeCallbacks(handshakeTimeoutRunnable)
                    startCountdown()
                }
            }
            EventType.MATCH_START -> {
                if (status == PvPStatus.COUNTDOWN) {
                    status = PvPStatus.ACTIVE
                }
            }
            EventType.SPELL_CAST -> {
                duelEngine?.applyOpponentEvent(event)
            }
            EventType.MATCH_END -> {
                status = PvPStatus.FINISHED
            }
            EventType.HEARTBEAT -> {
                // Keep-alive or sync check
            }
        }
    }
    
    private fun startCountdown() {
        status = PvPStatus.COUNTDOWN
        countdown = 3
        handler.post(object : Runnable {
            override fun run() {
                if (countdown > 1) {
                    countdown--
                    handler.postDelayed(this, 1000)
                } else {
                    bleManager.sendEvent(DuelEvent(EventType.MATCH_START, 0L))
                    status = PvPStatus.ACTIVE
                }
            }
        })
    }
    
    fun onDisconnected() {
        if (status == PvPStatus.ACTIVE) {
            status = PvPStatus.DISCONNECTED
        } else {
            reset()
        }
    }
    
    fun setDuelEngine(engine: DuelEngine) {
        this.duelEngine = engine
    }

    fun broadcastSpell(damage: Int, hash: Int) {
        bleManager.sendEvent(DuelEvent(EventType.SPELL_CAST, 0L, hash, damage))
    }
    
    fun reset() {
        handler.removeCallbacksAndMessages(null)
        status = PvPStatus.IDLE
        countdown = 3
        duelEngine = null
    }
}
