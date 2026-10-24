package by.rowing.sanbaiteam.training.data.models.comparison

internal data class MetricComparison(
    val average: Double,
    val delta: Double?,
    val trend: ComparisonTrend,
)