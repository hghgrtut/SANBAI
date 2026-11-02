package by.rowing.sanbaiteam.training.data.models.chart

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal sealed interface TrainingChartResult {

    data class Success(
        val data: TrainingPieceChartData,
        val pieceType: TrainingPieceType,
    ) : TrainingChartResult

    data object PieceNotFound : TrainingChartResult

    data object SourceUnavailable : TrainingChartResult
}