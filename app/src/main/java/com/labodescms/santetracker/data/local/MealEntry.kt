package com.labodescms.santetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One meal-journal row for a given day, keyed by [date] ("YYYY-MM-DD"). */
@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val type: String,
    val name: String,
    val time: String,
)
