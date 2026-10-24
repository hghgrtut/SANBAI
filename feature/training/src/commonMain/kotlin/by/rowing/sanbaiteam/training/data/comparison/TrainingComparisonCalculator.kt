package by.rowing.sanbaiteam.training.data.comparison

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.roundToTenths
import by.rowing.sanbaiteam.training.data.models.comparison.ComparablePiece
import by.rowing.sanbaiteam.training.data.models.comparison.ComparisonMetric
import by.rowing.sanbaiteam.training.data.models.comparison.ComparisonTrend
import by.rowing.sanbaiteam.training.data.models.comparison.MetricComparison
import by.rowing.sanbaiteam.training.data.models.comparison.PieceComparison
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Считает средние и дельты по отрезкам с одинаковым номером.
 * В среднее попадают только непустые значения метрик.
 */
internal object TrainingComparisonCalculator {

    private const val MILLIS_PER_TENTH = 100.0

    fun compare(
        base: ComparablePiece,
        compared: List<ComparablePiece>
    ): PieceComparison {
        val metrics = mutableMapOf<ComparisonMetric, MetricComparison>()
        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.DISTANCE,
            values = compared.map { it.distanceMeters.toDouble() },
            base = base.distanceMeters.toDouble(),
            higherIsBetter = true,
        )
        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.TIME,
            values = compared.map { it.timeMillis.toDouble() },
            base = base.timeMillis.toDouble(),
            higherIsBetter = false,
            round = { ((it / MILLIS_PER_TENTH).roundToLong() * MILLIS_PER_TENTH.toLong()).toDouble() }
        )
        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.PACE,
            values = compared.mapNotNull { paceOf(it) },
            base = paceOf(base),
            higherIsBetter = false,
        )

        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.STROKE_RATE,
            values = compared.map { it.strokeRate },
            base = base.strokeRate,
            higherIsBetter = null,
            round = { it.roundToTenths() }
        )

        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.AVG_HEART_RATE,
            values = compared.mapNotNull { it.avgHeartRate },
            base = base.avgHeartRate,
            higherIsBetter = false,
            round = { it.roundToInt().toDouble() },
        )
        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.MAX_HEART_RATE,
            values = compared.mapNotNull { it.maxHeartRate },
            base = base.maxHeartRate,
            higherIsBetter = false,
            round = { it.roundToInt().toDouble() },
        )
        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.RECOVERY_HEART_RATE,
            values = compared.mapNotNull { it.recoveryHeartRate },
            base = base.recoveryHeartRate,
            higherIsBetter = false,
            round = { it.roundToInt().toDouble() },
        )

        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.LACTATE,
            values = compared.mapNotNull { it.lactate },
            base = base.lactate,
            higherIsBetter = false,
            round = { it.roundToTenths() },
        )
        putMetricIfPresent(
            metrics = metrics,
            metric = ComparisonMetric.PERCENT_OF_PB,
            values = compared.mapNotNull { it.percentOfPb },
            base = base.percentOfPb,
            higherIsBetter = true,
            round = { it.roundToTenths() },
        )

        return PieceComparison(
            comparedTrainingsCount = compared.size,
            metrics = metrics,
        )
    }

    private fun paceOf(piece: ComparablePiece): Double? = RowingTimeFormat.paceMillis(
        timeMillis = piece.timeMillis,
        distanceMeters = piece.distanceMeters,
    )?.toDouble()

    private fun <T> putMetricIfPresent(
        metrics: MutableMap<ComparisonMetric, MetricComparison>,
        metric: ComparisonMetric,
        values: List<T>,
        base: T?,
        higherIsBetter: Boolean?,
        round: (Double) -> Double = { it },
    ) {
        if (values.isEmpty()) return
        val average = round(values.map { (it as Number).toDouble() }.average())
        val delta = base?.let { (it as Number).toDouble() }?.let { it - average }
        metrics[metric] = MetricComparison(
            average = average,
            delta = delta,
            trend = when {
                delta == null || delta == 0.0 || higherIsBetter == null -> ComparisonTrend.NEUTRAL
                higherIsBetter -> if (delta > 0) ComparisonTrend.BETTER else ComparisonTrend.WORSE
                else -> if (delta < 0) ComparisonTrend.BETTER else ComparisonTrend.WORSE
            },
        )
    }
}