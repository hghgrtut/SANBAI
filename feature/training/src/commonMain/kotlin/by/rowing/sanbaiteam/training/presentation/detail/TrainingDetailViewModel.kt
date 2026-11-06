package by.rowing.sanbaiteam.training.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.chart.TrainingSplitCalculator
import by.rowing.sanbaiteam.training.data.chart.models.TrainingPieceSplit
import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingComparisonSelectionStore
import by.rowing.sanbaiteam.training.presentation.detail.compose.TrainingDetailActions
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
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
                .map { selection -> selection.orEmpty() }
                .distinctUntilChanged()
                .collectLatest { comparedTrainingIds -> load(comparedTrainingIds = comparedTrainingIds) }
        }
    }

    override fun navigateBack() {
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

    override fun onChartClick(
        order: Int,
        metric: TrainingChartMetric
    ) {
        TrainingNavigation.TrainingChart(
            trainingId = trainingId,
            order = order,
            metric = metric.name,
        ).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingDetail>(),
        )
    }

    override fun onSplitClick() {
        changeState { copy(showSplitDialog = true) }
    }

    override fun onSplitDismiss() {
        changeState { copy(showSplitDialog = false) }
    }

    override fun onSplitApply(length: TrainingSplitLength) {
        changeState {
            copy(
                splitLength = length,
                splitsByOrder = calculateSplits(
                    pieces = pieces,
                    length = length,
                ),
                showSplitDialog = false,
            )
        }
    }

    override fun onSplitClear() {
        changeState {
            copy(
                splitLength = null,
                splitsByOrder = emptyMap(),
                showSplitDialog = false,
            )
        }
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
        val splitsByOrder = state.splitLength
            ?.let { length ->
                calculateSplits(
                    pieces = detailState.pieces,
                    length = length,
                )
            }
            .orEmpty()

        changeState {
            detailState.copy(
                expandedComparisonOrders = expandedOrders,
                splitLength = splitLength,
                splitsByOrder = splitsByOrder,
                showSplitDialog = showSplitDialog,
            )
        }
    }

    private fun calculateSplits(
        pieces: List<TrainingDetailPiece>,
        length: TrainingSplitLength,
    ): Map<Int, List<TrainingPieceSplit>> = pieces
        .mapNotNull { piece ->
            piece.chartData?.let { chart ->
                TrainingSplitCalculator.calculate(
                    chart = chart,
                    totalDistanceMeters = piece.distanceMeters,
                    totalTimeMillis = piece.timeMillis,
                    length = length,
                )?.let { splits -> piece.order to splits }
            }
        }
        .toMap()
}