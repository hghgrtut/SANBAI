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
) : ComposeBaseViewModel<TrainingCompareState>(
    initialState = TrainingCompareState()
) {

    init {
        viewModelScope.launch {
            val candidates = repository.getComparisonCandidates(trainingId)
            val availableIds = candidates.items.map { it.trainingId }.toSet()
            val storedSelection = comparisonSelectionStore.selection.value
                .comparedTrainingIdsFor(trainingId)
            val selectedIds = storedSelection.intersect(availableIds)

            changeState {
                TrainingCompareState(
                    typeResId = candidates.typeResId,
                    items = candidates.items,
                    selectedIds = selectedIds,
                    hasAppliedComparison = storedSelection.isNotEmpty(),
                    isLoading = false,
                )
            }
        }
    }

    fun onToggleSelection(selectedTrainingId: Long) {
        changeState {
            val selectedIds = if (selectedTrainingId in selectedIds) {
                selectedIds - selectedTrainingId
            } else {
                selectedIds + selectedTrainingId
            }
            copy(selectedIds = selectedIds)
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
        comparisonSelectionStore.clear(trainingId)
        composeNavigator.navigateBack()
    }

    fun onBack() {
        composeNavigator.navigateBack()
    }
}
