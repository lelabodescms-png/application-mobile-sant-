package com.labodescms.santetracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One weigh-in. [date] is a "YYYY-MM-DD" key so there can only be one entry per day
 * (enforced by a unique index) — matching the "replace if already weighed today" rule.
 */
@Entity(tableName = "weight_entries", indices = [Index(value = ["date"], unique = true)])
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val weight: Double,
)
