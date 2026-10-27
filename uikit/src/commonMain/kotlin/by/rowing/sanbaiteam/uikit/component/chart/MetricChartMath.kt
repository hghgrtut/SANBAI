package by.rowing.sanbaiteam.uikit.component.chart

import by.rowing.sanbaiteam.uikit.component.chart.models.ChartRange
import by.rowing.sanbaiteam.uikit.component.chart.models.MetricChartPoint
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

private const val MAX_POINTS_PER_SEGMENT = 500

internal fun niceRange(
    min: Double,
    max: Double,
    targetTicks: Int = 4,
): ChartRange = when {
    !min.isFinite() || !max.isFinite() -> ChartRange(
        min = 0.0,
        max = 1.0,
        step = 0.25
    )

    min == max -> {
        val padding = if (min == 0.0) 1.0 else abs(min) * 0.1
        niceRange(
            min = min - padding,
            max = max + padding,
            targetTicks = targetTicks
        )
    }

    else -> {
        val step = niceStep(rawStep = (max - min) / targetTicks.coerceAtLeast(1))
        ChartRange(
            min = floor(min / step) * step,
            max = ceil(max / step) * step,
            step = step
        )
    }
}

internal fun timeTickStep(durationMillis: Long): Long {
    if (durationMillis <= 0L) return 1_000L
    val rawStep = durationMillis.toDouble() / 4 // 4 - количество отметок времени на графике
    val candidates = longArrayOf(
        1_000L,
        2_000L,
        5_000L,
        10_000L,
        15_000L,
        30_000L,
        60_000L,
        120_000L,
        300_000L,
        600_000L,
        900_000L,
        1_800_000L,
    )
    return candidates.firstOrNull { it >= rawStep } ?: candidates.last()
}

internal fun buildContinuousSegments(
    points: List<MetricChartPoint>,
    durationMillis: Long,
): List<List<MetricChartPoint>> {
    if (points.isEmpty()) {
        return emptyList()
    }
    val gapMillis = gapThresholdMillis(
        points = points,
        durationMillis = durationMillis
    )
    val segments = mutableListOf<MutableList<MetricChartPoint>>()
    var current = mutableListOf(points.first())
    points.asSequence().drop(1).forEach { point ->
        val previous = current.last()
        if (point.elapsedMillis - previous.elapsedMillis > gapMillis) {
            segments += current
            current = mutableListOf(point)
        } else {
            current += point
        }
    }
    segments += current
    return segments.map { segment -> downsample(points = segment) }
}

private fun gapThresholdMillis(
    points: List<MetricChartPoint>,
    durationMillis: Long
): Long {
    val median = medianIntervalMillis(points)
    val fallback = max(durationMillis / points.size.coerceAtLeast(1), 1_000L)
    val interval = if (median > 0L) median else fallback
    return max(interval * 3L, 3_000L)
}

private fun niceStep(rawStep: Double): Double {
    if (!rawStep.isFinite() || rawStep <= 0.0) {
        return 1.0
    }
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val fraction = rawStep / magnitude
    val niceFraction = when {
        fraction <= 1.0 -> 1.0
        fraction <= 2.0 -> 2.0
        fraction <= 2.5 -> 2.5
        fraction <= 5.0 -> 5.0
        else -> 10.0
    }
    return niceFraction * magnitude
}

private fun medianIntervalMillis(points: List<MetricChartPoint>): Long {
    if (points.size < 2) return 0L
    val deltas = points.zipWithNext { first, second -> second.elapsedMillis - first.elapsedMillis }
        .filter { it > 0L }
        .sorted()
    if (deltas.isEmpty()) return 0L
    return deltas[deltas.size / 2]
}

private fun downsample(points: List<MetricChartPoint>): List<MetricChartPoint> {
    if (points.size <= MAX_POINTS_PER_SEGMENT) {
        return points
    }
    val bucketCount = MAX_POINTS_PER_SEGMENT / 2
    val bucketSize = ceil(points.size.toDouble() / bucketCount).toInt().coerceAtLeast(1)
    val result = ArrayList<MetricChartPoint>(MAX_POINTS_PER_SEGMENT)
    points.chunked(bucketSize).forEach { bucket ->
        if (bucket.size <= 2) {
            result += bucket
        } else {
            val min = bucket.minBy { it.value }
            val max = bucket.maxBy { it.value }
            if (min.elapsedMillis <= max.elapsedMillis) {
                result += min
                if (max !== min) {
                    result += max
                }
            } else {
                result += max
                if (max !== min) {
                    result += min
                }
            }
        }
    }
    return result
}