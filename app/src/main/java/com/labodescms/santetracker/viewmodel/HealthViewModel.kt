package com.labodescms.santetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.labodescms.santetracker.data.local.MealEntry
import com.labodescms.santetracker.data.repository.HealthRepository
import com.labodescms.santetracker.domain.AppUiState
import com.labodescms.santetracker.domain.Meal
import com.labodescms.santetracker.domain.MealType
import com.labodescms.santetracker.domain.ModalState
import com.labodescms.santetracker.domain.ModalType
import com.labodescms.santetracker.domain.NotificationSettings
import com.labodescms.santetracker.domain.Tab
import com.labodescms.santetracker.domain.WeightPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

/** Ephemeral, session-only UI state that isn't persisted (mirrors the prototype's in-memory state). */
private data class SessionState(
    val activeTab: Tab = Tab.ACCUEIL,
    val journalSelectedDate: LocalDate = LocalDate.now(),
    val modal: ModalState = ModalState(),
)

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    private val session = MutableStateFlow(SessionState())

    val uiState: StateFlow<AppUiState> = combine(
        repository.weightHistory,
        repository.meals,
        repository.settings,
        session,
    ) { weightEntries, mealEntries, settings, sess ->
        AppUiState(
            loading = false,
            activeTab = sess.activeTab,
            weightHistory = weightEntries.map { WeightPoint(LocalDate.parse(it.date, ISO_DATE), it.weight) },
            weightGoal = settings.weightGoal,
            water = settings.water,
            waterGoal = settings.waterGoal,
            activityMin = settings.activityMin,
            activityGoal = settings.activityGoal,
            fastingHr = settings.fastingHr,
            fastingGoal = settings.fastingGoal,
            journalSelectedDate = sess.journalSelectedDate,
            meals = mealEntries
                .mapNotNull { entry -> entry.toDomain()?.let { LocalDate.parse(entry.date, ISO_DATE) to it } }
                .groupBy({ it.first }, { it.second }),
            notifications = NotificationSettings(
                weighIn = settings.weighInReminder,
                hydration = settings.hydrationReminder,
                activity = settings.activityReminder,
            ),
            modal = sess.modal,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState(loading = true))

    init {
        viewModelScope.launch {
            val todayKey = LocalDate.now().format(ISO_DATE)
            repository.seedIfEmpty(
                todayKey = todayKey,
                todayMeals = listOf(
                    MealEntry(date = todayKey, type = MealType.PETIT_DEJEUNER.label, name = "Yaourt grec & granola", time = "07:30"),
                    MealEntry(date = todayKey, type = MealType.DEJEUNER.label, name = "Salade poulet grillé & quinoa", time = "12:45"),
                ),
            )
        }
    }

    fun selectTab(tab: Tab) {
        session.value = session.value.copy(activeTab = tab)
    }

    fun openModal(type: ModalType) {
        session.value = session.value.copy(
            modal = ModalState(open = type),
        )
    }

    fun closeModal() {
        session.value = session.value.copy(modal = ModalState())
    }

    fun updateWeightInput(value: String) {
        session.value = session.value.copy(modal = session.value.modal.copy(weightInput = value))
    }

    fun updateActivityInput(value: String) {
        session.value = session.value.copy(modal = session.value.modal.copy(activityInput = value))
    }

    fun applyActivityPreset(minutes: Int) {
        session.value = session.value.copy(modal = session.value.modal.copy(activityInput = minutes.toString()))
    }

    fun updateMealName(value: String) {
        session.value = session.value.copy(modal = session.value.modal.copy(mealName = value))
    }

    fun selectMealType(type: MealType) {
        session.value = session.value.copy(modal = session.value.modal.copy(mealType = type))
    }

    fun selectJournalDate(date: LocalDate) {
        session.value = session.value.copy(journalSelectedDate = date)
    }

    fun submitWeight() {
        val raw = session.value.modal.weightInput.replace(',', '.')
        val value = raw.toDoubleOrNull()
        if (value == null || value <= 0) {
            closeModal()
            return
        }
        viewModelScope.launch {
            repository.recordWeight(LocalDate.now().format(ISO_DATE), value)
        }
        closeModal()
    }

    fun submitActivity() {
        val minutes = session.value.modal.activityInput.toIntOrNull()
        if (minutes == null || minutes <= 0) {
            closeModal()
            return
        }
        viewModelScope.launch {
            repository.setActivityMin(uiState.value.activityMin + minutes)
        }
        closeModal()
    }

    fun submitMeal() {
        val name = session.value.modal.mealName.trim()
        if (name.isEmpty()) {
            closeModal()
            return
        }
        val type = session.value.modal.mealType
        val date = session.value.journalSelectedDate
        viewModelScope.launch {
            repository.addMeal(
                date = date.format(ISO_DATE),
                type = type.label,
                name = name,
                time = LocalTime.now().format(TIME_FORMAT),
            )
        }
        closeModal()
    }

    fun incWater() = viewModelScope.launch {
        val next = Math.round((uiState.value.water + 0.25) * 100) / 100.0
        repository.setWater(next)
    }

    fun decWater() = viewModelScope.launch {
        val next = (Math.round((uiState.value.water - 0.25) * 100) / 100.0).coerceAtLeast(0.0)
        repository.setWater(next)
    }

    fun incFasting() = viewModelScope.launch {
        repository.setFastingHr(uiState.value.fastingHr + 1)
    }

    fun decFasting() = viewModelScope.launch {
        repository.setFastingHr((uiState.value.fastingHr - 1).coerceAtLeast(0))
    }

    fun toggleWeighInReminder() = viewModelScope.launch {
        repository.setWeighInReminder(!uiState.value.notifications.weighIn)
    }

    fun toggleHydrationReminder() = viewModelScope.launch {
        repository.setHydrationReminder(!uiState.value.notifications.hydration)
    }

    fun toggleActivityReminder() = viewModelScope.launch {
        repository.setActivityReminder(!uiState.value.notifications.activity)
    }

    companion object {
        fun factory(repository: HealthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HealthViewModel(repository) as T
            }
    }
}

private fun MealEntry.toDomain(): Meal? {
    val mealType = MealType.entries.firstOrNull { it.label == type } ?: return null
    return Meal(id = id, type = mealType, name = name, time = time)
}
