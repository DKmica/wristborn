package com.wristborn.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.wristborn.app.engine.Element
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "progression")

class ProgressionManager(private val context: Context) {

    private object PreferencesKeys {
        val XP = longPreferencesKey("xp")
        val LEVEL = intPreferencesKey("level")
        val FIRE_MASTERY = intPreferencesKey("fire_mastery")
        val WIND_MASTERY = intPreferencesKey("wind_mastery")
        val ARCANE_MASTERY = intPreferencesKey("arcane_mastery")
        val VOID_MASTERY = intPreferencesKey("void_mastery")
        val STORM_MASTERY = intPreferencesKey("storm_mastery")
        val EARTH_MASTERY = intPreferencesKey("earth_mastery")
        val TOTAL_WINS = intPreferencesKey("total_wins")
        val WIN_STREAK = intPreferencesKey("win_streak")
        val DAILY_TRIAL_HIGHSCORE = intPreferencesKey("daily_trial_highscore")
        val LAST_TRIAL_DATE = longPreferencesKey("last_trial_date")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }

    val progressionFlow: Flow<ProgressionData> = context.dataStore.data.map { preferences ->
        ProgressionData(
            xp = preferences[PreferencesKeys.XP] ?: 0,
            level = preferences[PreferencesKeys.LEVEL] ?: 1,
            fireMastery = preferences[PreferencesKeys.FIRE_MASTERY] ?: 0,
            windMastery = preferences[PreferencesKeys.WIND_MASTERY] ?: 0,
            arcaneMastery = preferences[PreferencesKeys.ARCANE_MASTERY] ?: 0,
            voidMastery = preferences[PreferencesKeys.VOID_MASTERY] ?: 0,
            stormMastery = preferences[PreferencesKeys.STORM_MASTERY] ?: 0,
            earthMastery = preferences[PreferencesKeys.EARTH_MASTERY] ?: 0,
            totalWins = preferences[PreferencesKeys.TOTAL_WINS] ?: 0,
            winStreak = preferences[PreferencesKeys.WIN_STREAK] ?: 0,
            dailyTrialHighscore = preferences[PreferencesKeys.DAILY_TRIAL_HIGHSCORE] ?: 0,
            lastTrialDate = preferences[PreferencesKeys.LAST_TRIAL_DATE] ?: 0,
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
        )
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }

    suspend fun addXp(amount: Long) {
        context.dataStore.edit { preferences ->
            val currentXp = preferences[PreferencesKeys.XP] ?: 0
            val newXp = currentXp + amount
            preferences[PreferencesKeys.XP] = newXp
            preferences[PreferencesKeys.LEVEL] = (newXp / 1000).toInt() + 1
        }
    }

    suspend fun recordWin() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_WINS] = (preferences[PreferencesKeys.TOTAL_WINS] ?: 0) + 1
            preferences[PreferencesKeys.WIN_STREAK] = (preferences[PreferencesKeys.WIN_STREAK] ?: 0) + 1
        }
    }

    suspend fun resetStreak() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIN_STREAK] = 0
        }
    }

    suspend fun incrementMastery(element: Element) {
        context.dataStore.edit { preferences ->
            val key = when (element) {
                Element.FIRE -> PreferencesKeys.FIRE_MASTERY
                Element.WIND -> PreferencesKeys.WIND_MASTERY
                Element.ARCANE -> PreferencesKeys.ARCANE_MASTERY
                Element.VOID -> PreferencesKeys.VOID_MASTERY
                Element.STORM -> PreferencesKeys.STORM_MASTERY
                Element.EARTH -> PreferencesKeys.EARTH_MASTERY
            }
            preferences[key] = (preferences[key] ?: 0) + 1
        }
    }

    suspend fun updateDailyTrialScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentHigh = preferences[PreferencesKeys.DAILY_TRIAL_HIGHSCORE] ?: 0
            if (score > currentHigh) {
                preferences[PreferencesKeys.DAILY_TRIAL_HIGHSCORE] = score
            }
            preferences[PreferencesKeys.LAST_TRIAL_DATE] = System.currentTimeMillis()
        }
    }
}
