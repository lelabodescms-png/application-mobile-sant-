package com.labodescms.santetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sante_tracker_settings")

data class AppSettings(
    val weightGoal: Double = 70.0,
    val water: Double = 1.25,
    val waterGoal: Double = 2.0,
    val activityMin: Int = 35,
    val activityGoal: Int = 45,
    val fastingHr: Int = 9,
    val fastingGoal: Int = 14,
    val weighInReminder: Boolean = true,
    val hydrationReminder: Boolean = true,
    val activityReminder: Boolean = true,
)

/** Persists the durable scalar state from the README's "State Management" section (not the
 * ephemeral UI-only bits like activeTab, journalSelectedDate or modal draft fields). */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val WEIGHT_GOAL = doublePreferencesKey("weight_goal")
        val WATER = doublePreferencesKey("water")
        val WATER_GOAL = doublePreferencesKey("water_goal")
        val ACTIVITY_MIN = intPreferencesKey("activity_min")
        val ACTIVITY_GOAL = intPreferencesKey("activity_goal")
        val FASTING_HR = intPreferencesKey("fasting_hr")
        val FASTING_GOAL = intPreferencesKey("fasting_goal")
        val WEIGH_IN_REMINDER = booleanPreferencesKey("weigh_in_reminder")
        val HYDRATION_REMINDER = booleanPreferencesKey("hydration_reminder")
        val ACTIVITY_REMINDER = booleanPreferencesKey("activity_reminder")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            weightGoal = prefs[Keys.WEIGHT_GOAL] ?: 70.0,
            water = prefs[Keys.WATER] ?: 1.25,
            waterGoal = prefs[Keys.WATER_GOAL] ?: 2.0,
            activityMin = prefs[Keys.ACTIVITY_MIN] ?: 35,
            activityGoal = prefs[Keys.ACTIVITY_GOAL] ?: 45,
            fastingHr = prefs[Keys.FASTING_HR] ?: 9,
            fastingGoal = prefs[Keys.FASTING_GOAL] ?: 14,
            weighInReminder = prefs[Keys.WEIGH_IN_REMINDER] ?: true,
            hydrationReminder = prefs[Keys.HYDRATION_REMINDER] ?: true,
            activityReminder = prefs[Keys.ACTIVITY_REMINDER] ?: true,
        )
    }

    suspend fun setWater(value: Double) = context.dataStore.edit { it[Keys.WATER] = value }
    suspend fun setActivityMin(value: Int) = context.dataStore.edit { it[Keys.ACTIVITY_MIN] = value }
    suspend fun setFastingHr(value: Int) = context.dataStore.edit { it[Keys.FASTING_HR] = value }

    suspend fun setWeighInReminder(value: Boolean) =
        context.dataStore.edit { it[Keys.WEIGH_IN_REMINDER] = value }
    suspend fun setHydrationReminder(value: Boolean) =
        context.dataStore.edit { it[Keys.HYDRATION_REMINDER] = value }
    suspend fun setActivityReminder(value: Boolean) =
        context.dataStore.edit { it[Keys.ACTIVITY_REMINDER] = value }
}
