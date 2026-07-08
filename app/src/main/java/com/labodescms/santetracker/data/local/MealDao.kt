package com.labodescms.santetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries ORDER BY time ASC")
    fun observeAll(): Flow<List<MealEntry>>

    @Insert
    suspend fun insert(entry: MealEntry)
}
