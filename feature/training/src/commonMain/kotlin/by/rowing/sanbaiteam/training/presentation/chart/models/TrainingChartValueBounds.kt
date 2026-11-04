package by.rowing.sanbaiteam.training.presentation.chart.models

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint

private const val WATER_PACE_MARGIN_SECONDS = 50.0
private const val ERG_PACE_MARGIN_SECONDS = 15.0
private const val BIKE_PACE_MARGIN_SECONDS = 30.0
private const val WATER_STROKE_RATE_MIN = 9.0
private const val WATER_STROKE_RATE_MAX = 50.0
private const val ERG_STROKE_RATE_MARGIN = 10.0

/**
 * Возвращает окно значений для оси графика или null, если ограничивать диапазон не нужно.
 *
 * Окно всегда пересекается с текущим диапазоном точек, поэтому существующие ограничения
 * (например, отбрасывание темпа вне 60..500 секунд на 500 м в TrainingChartReader) остаются в силе.
 * Для BikeErg темп считается на 1000 м, поэтому и запас для него вдвое больше.
 */
internal fun trainingChartValueBounds(
    metric: TrainingChartMetric,
    pieceType: TrainingPieceType,
    points: List<TrainingChartPoint>,
): ClosedFloatingPointRange<Double>? {
    if (points.isEmpty()) return null

    val values = points.map { it.value }
    val dataMin = values.minOrNull() ?: return null
    val dataMax = values.maxOrNull() ?: return null
    val average = values.average()

    val suggested = when (metric) {
        TrainingChartMetric.SPEED -> {
            val margin = when {
                pieceType.isWater -> WATER_PACE_MARGIN_SECONDS
                pieceType == TrainingPieceType.CONCEPT_BIKE_ERG -> BIKE_PACE_MARGIN_SECONDS
                else -> ERG_PACE_MARGIN_SECONDS
            }
            average - margin to average + margin
        }

        TrainingChartMetric.STROKE_RATE -> if (pieceType.isWater) {
            WATER_STROKE_RATE_MIN to WATER_STROKE_RATE_MAX
        } else {
            average - ERG_STROKE_RATE_MARGIN to average + ERG_STROKE_RATE_MARGIN
        }

        TrainingChartMetric.HEART_RATE -> return null
    }
    val (suggestedMin, suggestedMax) = suggested

    val lower = maxOf(dataMin, suggestedMin)
    val upper = minOf(dataMax, suggestedMax)
    val applies = lower < upper && average in lower..upper && (lower != dataMin || upper != dataMax)

    return if (applies) lower..upper else null
}
