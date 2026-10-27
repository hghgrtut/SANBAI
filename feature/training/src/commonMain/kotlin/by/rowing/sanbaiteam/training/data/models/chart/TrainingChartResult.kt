package by.rowing.sanbaiteam.training.data.models.chart

internal sealed interface TrainingChartResult {

    data class Success(val data: TrainingPieceChartData) : TrainingChartResult

    data object PieceNotFound : TrainingChartResult

    data object SourceUnavailable : TrainingChartResult
}