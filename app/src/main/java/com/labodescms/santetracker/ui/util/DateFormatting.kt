package com.labodescms.santetracker.ui.util

import java.time.LocalDate

private val WEEKDAY_SHORT = listOf("dim", "lun", "mar", "mer", "jeu", "ven", "sam")
private val WEEKDAY_FULL = listOf("dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")
private val MONTHS_FULL = listOf(
    "janvier", "février", "mars", "avril", "mai", "juin",
    "juillet", "août", "septembre", "octobre", "novembre", "décembre",
)
private val MONTHS_SHORT = listOf("jan", "fév", "mar", "avr", "mai", "jui", "jul", "aoû", "sep", "oct", "nov", "déc")

private fun sundayIndex(date: LocalDate) = date.dayOfWeek.value % 7

fun weekdayShort(date: LocalDate): String = WEEKDAY_SHORT[sundayIndex(date)]

fun monthShort(date: LocalDate): String = MONTHS_SHORT[date.monthValue - 1]

/** e.g. "Mercredi 8 juillet 2026" */
fun longDateLabel(date: LocalDate): String {
    val weekday = WEEKDAY_FULL[sundayIndex(date)].replaceFirstChar { it.uppercase() }
    return "$weekday ${date.dayOfMonth} ${MONTHS_FULL[date.monthValue - 1]} ${date.year}"
}

/** e.g. "6 jul." */
fun shortDateLabel(date: LocalDate): String = "${date.dayOfMonth} ${monthShort(date)}."
