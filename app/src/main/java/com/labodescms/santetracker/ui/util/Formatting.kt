package com.labodescms.santetracker.ui.util

import java.util.Locale

fun formatWeight(v: Double): String = String.format(Locale.US, "%.1f", v)

/** Whole-number goals render without a decimal (e.g. "70" not "70.0"). */
fun formatGoal(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else formatWeight(v)

fun formatWater(v: Double): String = String.format(Locale.US, "%.2f", v)
