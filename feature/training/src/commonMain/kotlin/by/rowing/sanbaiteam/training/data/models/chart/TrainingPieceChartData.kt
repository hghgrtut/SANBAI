package by.rowing.sanbaiteam.training.data.models.chart

internal data class TrainingPieceChartData(
    val durationMillis: Long,
    val speed: List<TrainingChartPoint> = emptyList(),
    val strokeRate: List<TrainingChartPoint> = emptyList(),
    val heartRate: List<TrainingChartPoint> = emptyList(),
) {

    val availableMetrics: Set<TrainingChartMetric>
        get() = buildSet {
            if (speed.isNotEmpty()) add(TrainingChartMetric.SPEED)
            if (strokeRate.isNotEmpty()) add(TrainingChartMetric.STROKE_RATE)
            if (heartRate.isNotEmpty()) add(TrainingChartMetric.HEART_RATE)
        }

    fun points(metric: TrainingChartMetric): List<TrainingChartPoint> = when (metric) {
        TrainingChartMetric.SPEED -> speed
        TrainingChartMetric.STROKE_RATE -> strokeRate
        TrainingChartMetric.HEART_RATE -> heartRate
    }
}