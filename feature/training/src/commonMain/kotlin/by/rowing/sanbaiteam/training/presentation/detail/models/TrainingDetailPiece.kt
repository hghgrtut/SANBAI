package by.rowing.sanbaiteam.training.presentation.detail.models

import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.comparison.PieceComparison

internal data class TrainingDetailPiece(
    val order: Int,
    val distanceMeters: Int,
    val timeFormatted: String,
    val paceFormatted: String,
    val strokeRateFormatted: String,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val recoveryHeartRate: Int?,
    val lactate: Double?,
    val percentOfPbFormatted: String,
    val chartMetrics: Set<TrainingChartMetric> = emptySet(),
    val comparison: PieceComparison? = null,
)