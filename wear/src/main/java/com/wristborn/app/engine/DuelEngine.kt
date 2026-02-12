package com.wristborn.app.engine

import kotlin.random.Random

private const val BASE_DURATION_MS = 60_000L
private const val SUDDEN_DEATH_MS = 10_000L

data class DuelSnapshot(
    val playerHp: Int = 100,
    val dummyHp: Int = 100,
    val playerMana: Int = 100,
    val dummyMana: Int = 100,
    val remainingMs: Long = BASE_DURATION_MS,
    val inSuddenDeath: Boolean = false,
    val isFinished: Boolean = false,
    val logLine: String = "Duel started"
)

class DuelEngine {
    private var state = DuelSnapshot()
    private var lastDummyCastAtMs = 0L
    private var nextDummyCastDelayMs = randomDummyDelay()

    fun snapshot(): DuelSnapshot = state

    fun tick(nowMs: Long): DuelSnapshot {
        if (state.isFinished) return state

        val nextRemaining = (state.remainingMs - 1000L).coerceAtLeast(0L)
        state = state.copy(remainingMs = nextRemaining)

        if (state.remainingMs == 0L) {
            if (!state.inSuddenDeath && state.playerHp == state.dummyHp) {
                state = state.copy(inSuddenDeath = true, remainingMs = SUDDEN_DEATH_MS, logLine = "Sudden death!")
            } else {
                state = state.copy(isFinished = true, logLine = winnerLine())
            }
            return state
        }

        if (nowMs - lastDummyCastAtMs >= nextDummyCastDelayMs) {
            applyDummyCast()
            lastDummyCastAtMs = nowMs
            nextDummyCastDelayMs = randomDummyDelay()
        }

        if (state.playerHp <= 0 || state.dummyHp <= 0) {
            state = state.copy(isFinished = true, logLine = winnerLine())
        }

        return state
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
            dummyHp = (state.dummyHp - damage).coerceAtLeast(0),
            logLine = "You hit dummy for $damage"
        )
        if (state.dummyHp <= 0) {
            state = state.copy(isFinished = true, logLine = winnerLine())
        }
        return state
    }

    private fun applyDummyCast() {
        val damage = Random.nextInt(10, 18)
        state = state.copy(
            playerHp = (state.playerHp - damage).coerceAtLeast(0),
            dummyMana = (state.dummyMana - 8).coerceAtLeast(0),
            logLine = "Dummy hits for $damage"
        )
    }

    private fun randomDummyDelay(): Long = Random.nextLong(2200L, 4200L)

    private fun winnerLine(): String = when {
        state.playerHp > state.dummyHp -> "Victory"
        state.playerHp < state.dummyHp -> "Defeat"
        else -> "Draw"
    }
}
