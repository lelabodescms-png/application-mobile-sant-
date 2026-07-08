package com.labodescms.santetracker.data.repository

import com.labodescms.santetracker.data.local.AppSettings
import com.labodescms.santetracker.data.local.MealDao
import com.labodescms.santetracker.data.local.MealEntry
import com.labodescms.santetracker.data.local.SettingsDataStore
import com.labodescms.santetracker.data.local.WeightDao
import com.labodescms.santetracker.data.local.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Seed dataset from the design handoff: "Dataset d'exemple (historique de poids, Jan → Jul 2026)". */
private val SEED_WEIGHT_HISTORY = listOf(
    "2026-01-05" to 83.5,
    "2026-01-19" to 82.9,
    "2026-02-02" to 82.3,
    "2026-02-16" to 81.7,
    "2026-03-02" to 81.1,
    "2026-03-16" to 80.5,
    "2026-03-30" to 79.9,
    "2026-04-13" to 79.3,
    "2026-04-27" to 78.5,
    "2026-05-11" to 77.7,
    "2026-05-25" to 77.1,
    "2026-06-08" to 76.6,
    "2026-06-22" to 76.0,
    "2026-07-06" to 75.6,
)

class HealthRepository(
    private val weightDao: WeightDao,
    private val mealDao: MealDao,
    private val settingsDataStore: SettingsDataStore,
) {
    val weightHistory: Flow<List<WeightEntry>> = weightDao.observeAll()
    val meals: Flow<List<MealEntry>> = mealDao.observeAll()
    val settings: Flow<AppSettings> = settingsDataStore.settings

    suspend fun seedIfEmpty(todayKey: String, todayMeals: List<MealEntry>) {
        if (weightHistory.first().isEmpty()) {
            SEED_WEIGHT_HISTORY.forEach { (date, weight) ->
                weightDao.upsert(WeightEntry(date = date, weight = weight))
            }
        }
        if (meals.first().isEmpty()) {
            todayMeals.forEach { mealDao.insert(it.copy(date = todayKey)) }
        }
    }

    suspend fun recordWeight(todayKey: String, weight: Double) {
        weightDao.upsert(WeightEntry(date = todayKey, weight = weight))
    }

    suspend fun addMeal(date: String, type: String, name: String, time: String) {
        mealDao.insert(MealEntry(date = date, type = type, name = name, time = time))
    }

    suspend fun setWater(value: Double) = settingsDataStore.setWater(value)
    suspend fun setActivityMin(value: Int) = settingsDataStore.setActivityMin(value)
    suspend fun setFastingHr(value: Int) = settingsDataStore.setFastingHr(value)
    suspend fun setWeighInReminder(value: Boolean) = settingsDataStore.setWeighInReminder(value)
    suspend fun setHydrationReminder(value: Boolean) = settingsDataStore.setHydrationReminder(value)
    suspend fun setActivityReminder(value: Boolean) = settingsDataStore.setActivityReminder(value)
}
