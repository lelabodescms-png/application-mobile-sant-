package com.labodescms.santetracker.domain

import java.time.LocalDate

enum class Tab { ACCUEIL, POIDS, JOURNAL, REGLAGES }

enum class ModalType { WEIGHT, ACTIVITY, MEAL }

enum class MealType(val label: String) {
    PETIT_DEJEUNER("Petit-déjeuner"),
    DEJEUNER("Déjeuner"),
    DINER("Dîner"),
    COLLATION("Collation"),
}

data class WeightPoint(val date: LocalDate, val weight: Double)

data class Meal(
    val id: Long,
    val type: MealType,
    val name: String,
    val time: String,
)

data class NotificationSettings(
    val weighIn: Boolean = true,
    val hydration: Boolean = true,
    val activity: Boolean = true,
)

data class ModalState(
    val open: ModalType? = null,
    val weightInput: String = "",
    val activityInput: String = "",
    val mealName: String = "",
    val mealType: MealType = MealType.PETIT_DEJEUNER,
)

data class AppUiState(
    val loading: Boolean = true,
    val activeTab: Tab = Tab.ACCUEIL,
    val weightHistory: List<WeightPoint> = emptyList(),
    val weightGoal: Double = 70.0,
    val water: Double = 1.25,
    val waterGoal: Double = 2.0,
    val activityMin: Int = 35,
    val activityGoal: Int = 45,
    val fastingHr: Int = 9,
    val fastingGoal: Int = 14,
    val journalSelectedDate: LocalDate = LocalDate.now(),
    val meals: Map<LocalDate, List<Meal>> = emptyMap(),
    val notifications: NotificationSettings = NotificationSettings(),
    val modal: ModalState = ModalState(),
)
