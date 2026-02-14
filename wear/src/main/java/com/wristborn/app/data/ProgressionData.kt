package com.wristborn.app.data

data class ProgressionData(
    val xp: Long = 0,
    val level: Int = 1,
    val fireMastery: Int = 0,
    val windMastery: Int = 0,
    val arcaneMastery: Int = 0,
    val voidMastery: Int = 0,
    val stormMastery: Int = 0,
    val earthMastery: Int = 0,
    val winStreak: Int = 0,
    val totalWins: Int = 0,
    val dailyTrialHighscore: Int = 0,
    val lastTrialDate: Long = 0,
    val hasCompletedOnboarding: Boolean = false,
    
    // Viral V1 Fields
    val affinity: String? = null,
    val shareCode: String? = null
)
