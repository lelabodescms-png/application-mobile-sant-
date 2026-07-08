package com.labodescms.santetracker

import android.app.Application
import com.labodescms.santetracker.data.local.AppDatabase
import com.labodescms.santetracker.data.local.SettingsDataStore
import com.labodescms.santetracker.data.repository.HealthRepository

class SanteTrackerApp : Application() {

    val repository: HealthRepository by lazy {
        val db = AppDatabase.get(this)
        HealthRepository(
            weightDao = db.weightDao(),
            mealDao = db.mealDao(),
            settingsDataStore = SettingsDataStore(this),
        )
    }
}
