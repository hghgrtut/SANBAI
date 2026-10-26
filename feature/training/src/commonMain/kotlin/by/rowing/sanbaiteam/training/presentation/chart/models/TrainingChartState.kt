package by.rowing.sanbaiteam.training.presentation.chart.models

import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.chart.TrainingPieceChartData

internal data class TrainingChartState(
    val order: Int = 0,
    val chartData: TrainingPieceChartData? = null,
    val availableMetrics: List<TrainingChartMetric> = emptyList(),
    val selectedMetric: TrainingChartMetric? = null,
    val isLoading: Boolean = true,
    val pieceNotFound: Boolean = false,
    val sourceUnavailable: Boolean = false,
)