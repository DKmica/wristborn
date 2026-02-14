package com.wristborn.app.engine

import kotlin.random.Random

private const val V1_DURATION_MS = 45_000L
private const val V1_DAMAGE = 15

data class DuelSnapshotV1(
    val playerHp: Int = 100,
    val opponentHp: Int = 100,
    val remainingMs: Long = V1_DURATION_MS,
    val isFinished: Boolean = false,
    val logLine: String = "DUEL START",
    val winner: String? = null
)

class DuelEngineV1(val isPvP: Boolean = false) {
    private var state = DuelSnapshotV1()
    private var lastTickAtMs: Long? = null
    private var lastDummyCastAtMs: Long? = null
    private var nextDummyCastDelayMs = randomDummyDelay()

    fun snapshot(): DuelSnapshotV1 = state

    fun tick(nowMs: Long): DuelSnapshotV1 {
        if (state.isFinished) return state

        val lastTick = lastTickAtMs ?: nowMs
        val elapsedMs = nowMs - lastTick
        lastTickAtMs = nowMs

        val nextRemaining = (state.remainingMs - elapsedMs).coerceAtLeast(0L)
        state = state.copy(remainingMs = nextRemaining)

        if (state.remainingMs == 0L) {
            state = state.copy(isFinished = true, winner = calculateWinner(), logLine = "TIME UP")
            return state
        }

        if (!isPvP) {
            val lastDummyCast = lastDummyCastAtMs ?: nowMs
            if (nowMs - lastDummyCast >= nextDummyCastDelayMs) {
                applyOpponentDamage(V1_DAMAGE)
                lastDummyCastAtMs = nowMs
                nextDummyCastDelayMs = randomDummyDelay()
            }
        }

        if (state.playerHp <= 0 || state.opponentHp <= 0) {
            state = state.copy(isFinished = true, winner = calculateWinner(), logLine = "KO")
        }

        return state
    }

    fun applyPlayerCast(): DuelSnapshotV1 {
        if (state.isFinished) return state
        state = state.copy(
            opponentHp = (state.opponentHp - V1_DAMAGE).coerceAtLeast(0),
            logLine = "HIT!"
        )
        return state
    }

    fun applyOpponentDamage(damage: Int): DuelSnapshotV1 {
        if (state.isFinished) return state
        state = state.copy(
            playerHp = (state.playerHp - damage).coerceAtLeast(0),
            logLine = "OUCH!"
        )
        return state
    }

    private fun calculateWinner(): String = when {
        state.playerHp > state.opponentHp -> "PLAYER"
        state.playerHp < state.opponentHp -> "OPPONENT"
        else -> "DRAW"
    }

    private fun randomDummyDelay(): Long = Random.nextLong(2500L, 4500L)
}
