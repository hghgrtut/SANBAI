package by.rowing.sanbaiteam.training.presentation.chart

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartResult
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.chart.models.TrainingChartState
import kotlinx.coroutines.launch

internal interface TrainingChartActions {

    fun onBackClick()

    fun onMetricSelected(metric: TrainingChartMetric)
}

internal class TrainingChartViewModel(
    private val repository: TrainingRepository,
    private val composeNavigator: ComposeNavigator,
    private val trainingId: Long,
    private val order: Int,
    private val initialMetric: String?,
) : ComposeBaseViewModel<TrainingChartState>(initialState = TrainingChartState(order = order)), TrainingChartActions {

    init {
        viewModelScope.launch {
            when (val result = repository.getTrainingChart(trainingId, order)) {
                is TrainingChartResult.Success -> {
                    val availableMetrics = METRIC_ORDER.filter { it in result.data.availableMetrics }
                    val requested = initialMetric
                        ?.let { name -> TrainingChartMetric.entries.firstOrNull { it.name == name } }
                    changeState {
                        copy(
                            chartData = result.data,
                            availableMetrics = availableMetrics,
                            selectedMetric = requested
                                ?.takeIf { it in availableMetrics }
                                ?: availableMetrics.firstOrNull(),
                            isLoading = false,
                        )
                    }
                }

                TrainingChartResult.PieceNotFound -> changeState {
                    copy(
                        isLoading = false,
                        pieceNotFound = true
                    )
                }

                TrainingChartResult.SourceUnavailable -> changeState {
                    copy(
                        isLoading = false,
                        sourceUnavailable = true
                    )
                }
            }
        }
    }

    override fun onBackClick() {
        composeNavigator.navigateBack()
    }

    override fun onMetricSelected(metric: TrainingChartMetric) {
        if (metric !in state.availableMetrics) return
        changeState { copy(selectedMetric = metric) }
    }

    private companion object {
        val METRIC_ORDER = listOf(
            TrainingChartMetric.SPEED,
            TrainingChartMetric.STROKE_RATE,
            TrainingChartMetric.HEART_RATE,
        )
    }
}
