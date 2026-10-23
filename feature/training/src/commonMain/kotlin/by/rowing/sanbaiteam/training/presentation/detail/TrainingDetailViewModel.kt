package by.rowing.sanbaiteam.training.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingComparisonSelectionStore
import by.rowing.sanbaiteam.training.presentation.detail.compose.TrainingDetailActions
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavigation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class TrainingDetailViewModel(
    private val repository: TrainingRepository,
    private val composeNavigator: ComposeNavigator,
    private val comparisonSelectionStore: TrainingComparisonSelectionStore,
    private val trainingId: Long,
) : ComposeBaseViewModel<TrainingDetailState>(
    initialState = TrainingDetailState(trainingId = trainingId)
), TrainingDetailActions {

    private var isClosing = false

    init {
        viewModelScope.launch {
            comparisonSelectionStore.selection
                .map { selection -> selection?.comparedTrainingIdsFor(trainingId).orEmpty() }
                .distinctUntilChanged()
                .collectLatest { comparedTrainingIds ->
                    load(comparedTrainingIds)
                }
        }
    }

    override fun onBackClick() {
        isClosing = true
        onClearComparison()
        composeNavigator.navigateBack()
    }

    override fun onCompareClick() {
        TrainingNavigation.TrainingCompare(trainingId = trainingId).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingDetail>(),
        )
    }

    override fun onToggleComparison(order: Int) {
        changeState {
            copy(
                expandedComparisonOrders = if (order in expandedComparisonOrders) {
                    expandedComparisonOrders - order
                } else {
                    expandedComparisonOrders + order
                }
            )
        }
    }

    override fun onClearComparison() {
        comparisonSelectionStore.clear()
    }

    override fun onCleared() {
        isClosing = true
        onClearComparison()
        super.onCleared()
    }

    private suspend fun load(comparedTrainingIds: Set<Long>) {
        if (isClosing) return

        val detailState = repository.getTrainingDetail(
            trainingId = trainingId,
            comparedTrainingIds = comparedTrainingIds.toList(),
        )
        if (isClosing) return

        val expandedOrders = if (comparedTrainingIds.isEmpty()) {
            emptySet()
        } else {
            detailState.pieces
                .filter { it.comparison != null }
                .map { it.order }
                .toSet()
        }

        changeState {
            detailState.copy(expandedComparisonOrders = expandedOrders)
        }
    }
}