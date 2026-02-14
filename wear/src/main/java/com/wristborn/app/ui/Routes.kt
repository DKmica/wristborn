package com.wristborn.app.ui

object Routes {
    const val ARENA = "arena"
    const val TRAINING = "training"
    const val DUEL = "duel"
    const val PVP_DUEL = "pvp_duel"
    const val PROFILE = "profile"
    const val ONBOARDING = "onboarding"
    const val DAILY_TRIAL = "daily_trial"
    
    // Viral V1 Routes
    const val ARENA_V1 = "arena_v1"
    const val DUEL_V1 = "duel_v1"
    const val PVP_DUEL_V1 = "pvp_duel_v1"
    const val RESULT_V1 = "result_v1/{winner}"
    
    fun resultV1(winner: String) = "result_v1/$winner"
}
