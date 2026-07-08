package com.labodescms.santetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.labodescms.santetracker.domain.ModalType
import com.labodescms.santetracker.domain.Tab
import com.labodescms.santetracker.ui.components.ActivityInputSheet
import com.labodescms.santetracker.ui.components.BottomNavBar
import com.labodescms.santetracker.ui.components.MealInputSheet
import com.labodescms.santetracker.ui.components.WeightInputSheet
import com.labodescms.santetracker.ui.screens.dashboard.DashboardScreen
import com.labodescms.santetracker.ui.screens.journal.JournalScreen
import com.labodescms.santetracker.ui.screens.settings.SettingsScreen
import com.labodescms.santetracker.ui.screens.weight.WeightScreen
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.SanteTrackerTheme
import com.labodescms.santetracker.viewmodel.HealthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanteTrackerTheme {
                val app = application as SanteTrackerApp
                val viewModel: HealthViewModel = viewModel(factory = HealthViewModel.factory(app.repository))
                SanteTrackerScaffold(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SanteTrackerScaffold(viewModel: HealthViewModel) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            BottomNavBar(activeTab = state.activeTab, onTabSelected = viewModel::selectTab)
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            when (state.activeTab) {
                Tab.ACCUEIL -> DashboardScreen(
                    state = state,
                    onWeighInClick = { viewModel.openModal(ModalType.WEIGHT) },
                    onIncWater = viewModel::incWater,
                    onDecWater = viewModel::decWater,
                    onIncFasting = viewModel::incFasting,
                    onDecFasting = viewModel::decFasting,
                    onAddActivityClick = { viewModel.openModal(ModalType.ACTIVITY) },
                    onGoToJournal = { viewModel.selectTab(Tab.JOURNAL) },
                )
                Tab.POIDS -> WeightScreen(
                    state = state,
                    onAddWeightClick = { viewModel.openModal(ModalType.WEIGHT) },
                )
                Tab.JOURNAL -> JournalScreen(
                    state = state,
                    onSelectDate = viewModel::selectJournalDate,
                    onAddMealClick = { viewModel.openModal(ModalType.MEAL) },
                )
                Tab.REGLAGES -> SettingsScreen(
                    state = state,
                    onToggleWeighIn = viewModel::toggleWeighInReminder,
                    onToggleHydration = viewModel::toggleHydrationReminder,
                    onToggleActivity = viewModel::toggleActivityReminder,
                )
            }
        }
    }

    // Animates the sheet to hidden first, then applies the state change — submitting must not
    // yank the sheet out of composition before its hide animation gets to play.
    fun dismissThen(sheetState: SheetState, action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    when (state.modal.open) {
        ModalType.WEIGHT -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            WeightInputSheet(
                sheetState = sheetState,
                value = state.modal.weightInput,
                onValueChange = viewModel::updateWeightInput,
                onDismissRequest = { viewModel.closeModal() },
                onSubmit = { dismissThen(sheetState) { viewModel.submitWeight() } },
            )
        }
        ModalType.ACTIVITY -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ActivityInputSheet(
                sheetState = sheetState,
                value = state.modal.activityInput,
                onValueChange = viewModel::updateActivityInput,
                onPreset = viewModel::applyActivityPreset,
                onDismissRequest = { viewModel.closeModal() },
                onSubmit = { dismissThen(sheetState) { viewModel.submitActivity() } },
            )
        }
        ModalType.MEAL -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            MealInputSheet(
                sheetState = sheetState,
                mealName = state.modal.mealName,
                onMealNameChange = viewModel::updateMealName,
                selectedType = state.modal.mealType,
                onTypeSelected = viewModel::selectMealType,
                onDismissRequest = { viewModel.closeModal() },
                onSubmit = { dismissThen(sheetState) { viewModel.submitMeal() } },
            )
        }
        null -> Unit
    }
}
