package com.wristborn.app.engine

import com.wristborn.app.ble.DuelEvent
import kotlin.random.Random

private const val BASE_DURATION_MS = 60_000L
private const val SUDDEN_DEATH_MS = 10_000L

data class DuelSnapshot(
    val playerHp: Int = 100,
    val opponentHp: Int = 100, // Renamed from dummyHp
    val playerMana: Int = 100,
    val opponentMana: Int = 100, // Renamed from dummyMana
    val remainingMs: Long = BASE_DURATION_MS,
    val inSuddenDeath: Boolean = false,
    val isFinished: Boolean = false,
    val logLine: String = "Duel started",
    val isPvP: Boolean = false
)

class DuelEngine(val isPvP: Boolean = false) {
    private var state = DuelSnapshot(isPvP = isPvP)
    private var lastTickAtMs: Long? = null
    
    // For Dummy AI
    private var lastDummyCastAtMs: Long? = null
    private var nextDummyCastDelayMs = randomDummyDelay()

    fun snapshot(): DuelSnapshot = state

    fun tick(nowMs: Long): DuelSnapshot {
        if (state.isFinished) return state

        val elapsedMs = computeElapsedMs(nowMs)
        if (elapsedMs <= 0L) {
            if (lastTickAtMs == null) {
                lastTickAtMs = nowMs
                if (!isPvP) lastDummyCastAtMs = nowMs
            }
            return state
        }

        lastTickAtMs = nowMs

        val nextRemaining = (state.remainingMs - elapsedMs).coerceAtLeast(0L)
        state = state.copy(remainingMs = nextRemaining)

        if (state.remainingMs == 0L) {
            if (!state.inSuddenDeath && state.playerHp == state.opponentHp) {
                state = state.copy(inSuddenDeath = true, remainingMs = SUDDEN_DEATH_MS, logLine = "Sudden death!")
            } else {
                state = state.copy(isFinished = true, logLine = winnerLine())
            }
            return state
        }

        // Only run Dummy AI if not in PvP
        if (!isPvP) {
            val lastDummyCast = lastDummyCastAtMs
            if (lastDummyCast != null && nowMs - lastDummyCast >= nextDummyCastDelayMs) {
                applyDummyCast()
                lastDummyCastAtMs = nowMs
                nextDummyCastDelayMs = randomDummyDelay()
            }
        }

        if (state.playerHp <= 0 || state.opponentHp <= 0) {
            state = state.copy(isFinished = true, logLine = winnerLine())
        }

        return state
    }

    private fun computeElapsedMs(nowMs: Long): Long {
        val lastTick = lastTickAtMs ?: return 0L
        return (nowMs - lastTick).coerceAtLeast(0L)
    }

    fun applyPlayerSpell(spell: Spell): DuelSnapshot {
        if (state.isFinished) return state

        val damage = when (spell.form) {
            FormType.CHARGED -> Random.nextInt(25, 31)
            else -> Random.nextInt(12, 19)
        }
        val manaCost = if (spell.form == FormType.CHARGED) 18 else 10
        if (state.playerMana < manaCost) {
            state = state.copy(logLine = "Not enough mana")
            return state
        }

        state = state.copy(
            playerMana = (state.playerMana - manaCost).coerceAtLeast(0),
            opponentHp = (state.opponentHp - damage).coerceAtLeast(0),
            logLine = "You hit for $damage"
        )
        checkFinish()
        return state
    }

    fun applyOpponentEvent(event: DuelEvent): DuelSnapshot {
        if (state.isFinished) return state
        
        state = when (event.type) {
            com.wristborn.app.ble.EventType.SPELL_CAST -> {
                state.copy(
                    playerHp = (state.playerHp - event.damage).coerceAtLeast(0),
                    opponentMana = (state.opponentMana - 10).coerceAtLeast(0), // Simplified mana
                    logLine = "Opponent hit for ${event.damage}"
                )
            }
            else -> state
        }
        checkFinish()
        return state
    }

    private fun checkFinish() {
        if (state.playerHp <= 0 || state.opponentHp <= 0) {
            state = state.copy(isFinished = true, logLine = winnerLine())
        }
    }

    private fun applyDummyCast() {
        val damage = Random.nextInt(10, 18)
        state = state.copy(
            playerHp = (state.playerHp - damage).coerceAtLeast(0),
            opponentMana = (state.opponentMana - 8).coerceAtLeast(0),
            logLine = "Dummy hits for $damage"
        )
    }

    private fun randomDummyDelay(): Long = Random.nextLong(2200L, 4200L)

    private fun winnerLine(): String = when {
        state.playerHp > state.opponentHp -> "Victory"
        state.playerHp < state.opponentHp -> "Defeat"
        else -> "Draw"
    }
}
