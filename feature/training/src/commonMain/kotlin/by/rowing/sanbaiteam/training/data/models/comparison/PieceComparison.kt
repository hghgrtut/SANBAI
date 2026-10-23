package by.rowing.sanbaiteam.training.data.models.comparison

internal data class PieceComparison(
    val comparedTrainingsCount: Int,
    val metrics: Map<ComparisonMetric, MetricComparison>,
)