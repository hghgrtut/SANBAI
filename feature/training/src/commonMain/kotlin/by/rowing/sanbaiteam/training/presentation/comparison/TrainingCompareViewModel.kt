package by.rowing.sanbaiteam.training.presentation.comparison

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.comparison.models.TrainingCompareState
import kotlinx.coroutines.launch

internal class TrainingCompareViewModel(
    private val repository: TrainingRepository,
    private val composeNavigator: ComposeNavigator,
    private val comparisonSelectionStore: TrainingComparisonSelectionStore,
    private val trainingId: Long,
) : ComposeBaseViewModel<TrainingCompareState>(initialState = TrainingCompareState()) {

    init {
        viewModelScope.launch {
            val candidates = repository.getComparisonCandidates(trainingId)
            val availableIds = candidates.map { it.trainingId }.toSet()
            val storedSelection = comparisonSelectionStore.selection.value?.comparedTrainingIdsFor(trainingId).orEmpty()
            val selectedIds = storedSelection.intersect(availableIds)

            changeState {
                TrainingCompareState(
                    typeResId = candidates.firstOrNull()?.trainingTypeResId,
                    items = candidates,
                    selectedIds = selectedIds,
                    hasAppliedComparison = storedSelection.isNotEmpty(),
                    isLoading = false,
                )
            }
        }
    }

    fun onToggleSelection(selectedTrainingId: Long) {
        changeState {
            copy(
                selectedIds = if (selectedTrainingId in selectedIds) {
                    selectedIds - selectedTrainingId
                } else {
                    selectedIds + selectedTrainingId
                }
            )
        }
    }

    fun onConfirm() {
        if (state.selectedIds.isEmpty()) return
        comparisonSelectionStore.select(
            trainingId = trainingId,
            comparedTrainingIds = state.selectedIds,
        )
        composeNavigator.navigateBack()
    }

    fun onReset() {
        comparisonSelectionStore.clear()
        composeNavigator.navigateBack()
    }

    fun onBack() {
        composeNavigator.navigateBack()
    }
}
