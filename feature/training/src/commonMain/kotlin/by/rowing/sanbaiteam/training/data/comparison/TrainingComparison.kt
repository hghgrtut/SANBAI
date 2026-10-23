package by.rowing.sanbaiteam.training.data.comparison

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal data class ComparablePiece(
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val recoveryHeartRate: Int?,
    val lactate: Double?,
    val percentOfPb: Double?,
)

internal enum class ComparisonMetric {
    DISTANCE,
    TIME,
    PACE,
    STROKE_RATE,
    AVG_HEART_RATE,
    MAX_HEART_RATE,
    RECOVERY_HEART_RATE,
    LACTATE,
    PERCENT_OF_PB,
}

internal enum class ComparisonTrend {
    BETTER,
    WORSE,
    NEUTRAL,
}

internal data class MetricComparison(
    val average: Double,
    val delta: Double?,
    val trend: ComparisonTrend,
)

internal data class PieceComparison(
    val comparedTrainingsCount: Int,
    val metrics: Map<ComparisonMetric, MetricComparison>,
)

/**
 * Считает средние и дельты по отрезкам с одинаковым номером.
 * В среднее попадают только непустые значения метрик.
 */
internal object TrainingComparisonCalculator {

    private const val MILLIS_PER_TENTH = 100.0
    private const val TENTHS_IN_ONE = 10.0

    fun compare(base: ComparablePiece, compared: List<ComparablePiece>): PieceComparison? {
        if (compared.isEmpty()) return null

        val metrics = mutableMapOf<ComparisonMetric, MetricComparison>()

        metrics[ComparisonMetric.DISTANCE] = comparison(
            average = compared.map { it.distanceMeters }.average().roundToInt().toDouble(),
            base = base.distanceMeters.toDouble(),
            higherIsBetter = true,
        )
        metrics[ComparisonMetric.TIME] = comparison(
            average = compared.map { it.timeMillis.toDouble() }.average().roundToMillisTenths().toDouble(),
            base = base.timeMillis.toDouble(),
            higherIsBetter = false,
        )

        val basePace = RowingTimeFormat.paceMillis(
            timeMillis = base.timeMillis,
            distanceMeters = base.distanceMeters,
        )?.toDouble()
        val comparedPaces = compared.mapNotNull {
            RowingTimeFormat.paceMillis(
                timeMillis = it.timeMillis,
                distanceMeters = it.distanceMeters,
            )?.toDouble()
        }
        if (comparedPaces.isNotEmpty()) {
            metrics[ComparisonMetric.PACE] = comparison(
                average = comparedPaces.average().roundToMillisTenths().toDouble(),
                base = basePace,
                higherIsBetter = false,
            )
        }

        metrics[ComparisonMetric.STROKE_RATE] = comparison(
            average = compared.map { it.strokeRate }.average().roundToTenths(),
            base = base.strokeRate,
            higherIsBetter = null,
        )

        metrics.putHeartRate(
            metric = ComparisonMetric.AVG_HEART_RATE,
            values = compared.mapNotNull { it.avgHeartRate },
            base = base.avgHeartRate,
        )
        metrics.putHeartRate(
            metric = ComparisonMetric.MAX_HEART_RATE,
            values = compared.mapNotNull { it.maxHeartRate },
            base = base.maxHeartRate,
        )
        metrics.putHeartRate(
            metric = ComparisonMetric.RECOVERY_HEART_RATE,
            values = compared.mapNotNull { it.recoveryHeartRate },
            base = base.recoveryHeartRate,
        )

        metrics.putLactate(
            metric = ComparisonMetric.LACTATE,
            values = compared.mapNotNull { it.lactate },
            base = base.lactate,
        )
        metrics.putPercentOfPb(
            metric = ComparisonMetric.PERCENT_OF_PB,
            values = compared.mapNotNull { it.percentOfPb },
            base = base.percentOfPb,
        )

        return PieceComparison(
            comparedTrainingsCount = compared.size,
            metrics = metrics,
        )
    }

    private fun MutableMap<ComparisonMetric, MetricComparison>.putHeartRate(
        metric: ComparisonMetric,
        values: List<Int>,
        base: Int?,
    ) {
        if (values.isEmpty()) return
        this[metric] = comparison(
            average = values.average().roundToInt().toDouble(),
            base = base?.toDouble(),
            higherIsBetter = false,
        )
    }

    private fun MutableMap<ComparisonMetric, MetricComparison>.putLactate(
        metric: ComparisonMetric,
        values: List<Double>,
        base: Double?,
    ) {
        if (values.isEmpty()) return
        this[metric] = comparison(
            average = values.average().roundToTenths(),
            base = base,
            higherIsBetter = false,
        )
    }

    private fun MutableMap<ComparisonMetric, MetricComparison>.putPercentOfPb(
        metric: ComparisonMetric,
        values: List<Double>,
        base: Double?,
    ) {
        if (values.isEmpty()) return
        this[metric] = comparison(
            average = values.average().roundToTenths(),
            base = base,
            higherIsBetter = true,
        )
    }

    private fun comparison(
        average: Double,
        base: Double?,
        higherIsBetter: Boolean?,
    ): MetricComparison {
        val delta = base?.let { it - average }
        val trend = when {
            delta == null || delta == 0.0 || higherIsBetter == null -> ComparisonTrend.NEUTRAL
            higherIsBetter -> if (delta > 0) ComparisonTrend.BETTER else ComparisonTrend.WORSE
            else -> if (delta < 0) ComparisonTrend.BETTER else ComparisonTrend.WORSE
        }
        return MetricComparison(
            average = average,
            delta = delta,
            trend = trend,
        )
    }

    private fun Double.roundToMillisTenths(): Long =
        (this / MILLIS_PER_TENTH).roundToLong() * MILLIS_PER_TENTH.toLong()

    private fun Double.roundToTenths(): Double =
        (this * TENTHS_IN_ONE).roundToInt() / TENTHS_IN_ONE
}
