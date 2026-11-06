package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.roundToTenths
import by.rowing.sanbaiteam.training.data.chart.models.TrainingPieceSplit
import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint
import by.rowing.sanbaiteam.training.data.models.chart.TrainingPieceChartData
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Делит отрезок на сплиты по накопленной дистанции из [TrainingPieceChartData.distance].
 *
 * Все сплиты, кроме последнего, имеют ровно заданную длину; последний забирает остаток.
 * Итоговые дистанция и время берутся из БД, а дистанционный трек масштабируется под них,
 * чтобы сумма сплитов совпадала с отрезком.
 */
internal object TrainingSplitCalculator {

    fun calculate(
        chart: TrainingPieceChartData,
        totalDistanceMeters: Int,
        totalTimeMillis: Long,
        length: TrainingSplitLength,
    ): List<TrainingPieceSplit>? {
        if (totalDistanceMeters <= 0 || totalTimeMillis <= 0) return null
        val boundaries = buildBoundaries(
            points = chart.distance,
            totalDistanceMeters = totalDistanceMeters,
            totalTimeMillis = totalTimeMillis,
            length = length,
        )
        return boundaries?.indices?.toList()?.dropLast(1)?.map { index ->
            buildSplit(
                chart = chart,
                index = index + 1,
                start = boundaries[index],
                end = boundaries[index + 1],
                isLast = index == boundaries.lastIndex - 1,
            )
        }
    }

    private fun buildTrack(
        points: List<TrainingChartPoint>,
        totalDistanceMeters: Int,
        totalTimeMillis: Long,
    ): List<TrackPoint>? {
        if (points.size < 2) return null
        val cleaned = mutableListOf<TrackPoint>()
        var maxDistance = 0.0
        points.sortedBy { it.elapsedMillis }.forEach { point ->
            val time = point.elapsedMillis.coerceIn(0L, totalTimeMillis)
            maxDistance = maxOf(maxDistance, point.value.coerceAtLeast(0.0))
            if (cleaned.lastOrNull()?.timeMillis == time) {
                cleaned[cleaned.lastIndex] = TrackPoint(
                    timeMillis = time,
                    distanceMeters = maxDistance
                )
            } else {
                cleaned += TrackPoint(
                    timeMillis = time,
                    distanceMeters = maxDistance
                )
            }
        }
        if (cleaned.size < 2) return null

        if (cleaned.first().timeMillis > 0L) {
            cleaned.add(
                0,
                TrackPoint(
                    timeMillis = 0L,
                    distanceMeters = 0.0
                )
            )
        }
        val lastDistance = cleaned.last().distanceMeters
        if (lastDistance <= 0.0) return null

        val scale = totalDistanceMeters / lastDistance
        val scaled = cleaned.map { point ->
            TrackPoint(
                timeMillis = point.timeMillis,
                distanceMeters = point.distanceMeters * scale,
            )
        }
        return if (scaled.last().timeMillis >= totalTimeMillis) {
            scaled
        } else {
            scaled + TrackPoint(
                timeMillis = totalTimeMillis,
                distanceMeters = totalDistanceMeters.toDouble(),
            )
        }
    }

    private fun buildBoundaries(
        points: List<TrainingChartPoint>,
        totalDistanceMeters: Int,
        totalTimeMillis: Long,
        length: TrainingSplitLength,
    ): List<SplitBoundary>? = buildTrack(
        points = points,
        totalDistanceMeters = totalDistanceMeters,
        totalTimeMillis = totalTimeMillis,
    )?.let { track ->
        when (length) {
            is TrainingSplitLength.Distance -> distanceBoundaries(
                track = track,
                totalDistanceMeters = totalDistanceMeters,
                totalTimeMillis = totalTimeMillis,
                stepMeters = length.meters,
            )

            is TrainingSplitLength.Time -> timeBoundaries(
                track = track,
                totalDistanceMeters = totalDistanceMeters,
                totalTimeMillis = totalTimeMillis,
                stepMillis = length.millis,
            )
        }
    }

    private fun distanceBoundaries(
        track: List<TrackPoint>,
        totalDistanceMeters: Int,
        totalTimeMillis: Long,
        stepMeters: Int,
    ): List<SplitBoundary>? {
        if (stepMeters <= 0) return null
        val boundaries = mutableListOf<SplitBoundary>()
        var distance = 0L
        while (distance < totalDistanceMeters) {
            boundaries += SplitBoundary(
                timeMillis = timeAtDistance(
                    track = track,
                    distance = distance.toDouble()
                ).roundToLong(),
                distanceMeters = distance.toInt(),
            )
            distance += stepMeters
        }
        boundaries += SplitBoundary(
            timeMillis = totalTimeMillis,
            distanceMeters = totalDistanceMeters,
        )
        return boundaries
    }

    private fun timeBoundaries(
        track: List<TrackPoint>,
        totalDistanceMeters: Int,
        totalTimeMillis: Long,
        stepMillis: Long,
    ): List<SplitBoundary>? {
        if (stepMillis <= 0L) return null
        val boundaries = mutableListOf<SplitBoundary>()
        var timeMillis = 0L
        while (timeMillis < totalTimeMillis) {
            boundaries += SplitBoundary(
                timeMillis = timeMillis,
                distanceMeters = distanceAtTime(
                    track = track,
                    timeMillis = timeMillis.toDouble(),
                ).roundToInt(),
            )
            timeMillis += stepMillis
        }
        boundaries += SplitBoundary(
            timeMillis = totalTimeMillis,
            distanceMeters = totalDistanceMeters,
        )
        return boundaries
    }

    private fun buildSplit(
        chart: TrainingPieceChartData,
        index: Int,
        start: SplitBoundary,
        end: SplitBoundary,
        isLast: Boolean,
    ): TrainingPieceSplit {
        val timeMillis = (end.timeMillis - start.timeMillis).coerceAtLeast(0L)
        val distanceMeters = (end.distanceMeters - start.distanceMeters).coerceAtLeast(0)
        val strokeRates = valuesInRange(
            points = chart.strokeRate,
            startMillis = start.timeMillis,
            endMillis = end.timeMillis,
            includeEnd = isLast,
        )
        val heartRates = valuesInRange(
            points = chart.heartRate,
            startMillis = start.timeMillis,
            endMillis = end.timeMillis,
            includeEnd = isLast,
        )
        return TrainingPieceSplit(
            index = index,
            distanceMeters = distanceMeters,
            timeMillis = timeMillis,
            paceMillis = RowingTimeFormat.paceMillis(
                timeMillis = timeMillis,
                distanceMeters = distanceMeters,
            ),
            strokeRate = strokeRates.takeIf { it.isNotEmpty() }?.average()?.roundToTenths(),
            avgHeartRate = heartRates.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
            maxHeartRate = heartRates.maxOrNull()?.roundToInt(),
        )
    }

    private fun valuesInRange(
        points: List<TrainingChartPoint>,
        startMillis: Long,
        endMillis: Long,
        includeEnd: Boolean,
    ): List<Double> = points
        .filter { point ->
            point.elapsedMillis >= startMillis &&
                    if (includeEnd) point.elapsedMillis <= endMillis else point.elapsedMillis < endMillis
        }
        .map { it.value }

    private fun timeAtDistance(
        track: List<TrackPoint>,
        distance: Double,
    ): Double {
        if (distance <= track.first().distanceMeters) return track.first().timeMillis.toDouble()
        if (distance >= track.last().distanceMeters) return track.last().timeMillis.toDouble()
        var low = 0
        var high = track.lastIndex
        while (low < high) {
            val middle = (low + high) / 2
            if (track[middle].distanceMeters < distance) {
                low = middle + 1
            } else {
                high = middle
            }
        }
        return interpolateTime(
            previous = track[(low - 1).coerceAtLeast(0)],
            current = track[low],
            distance = distance,
        )
    }

    private fun distanceAtTime(
        track: List<TrackPoint>,
        timeMillis: Double,
    ): Double {
        when {
            timeMillis <= track.first().timeMillis -> return track.first().distanceMeters
            timeMillis >= track.last().timeMillis -> return track.last().distanceMeters
        }
        var low = 0
        var high = track.lastIndex
        while (low < high) {
            val middle = (low + high) / 2
            if (track[middle].timeMillis < timeMillis) {
                low = middle + 1
            } else {
                high = middle
            }
        }
        return interpolateDistance(
            previous = track[(low - 1).coerceAtLeast(0)],
            current = track[low],
            timeMillis = timeMillis,
        )
    }

    private fun interpolateTime(
        previous: TrackPoint,
        current: TrackPoint,
        distance: Double,
    ): Double {
        val span = current.distanceMeters - previous.distanceMeters
        if (span <= 0.0) {
            return current.timeMillis.toDouble()
        }
        val fraction = (distance - previous.distanceMeters) / span
        return previous.timeMillis + fraction * (current.timeMillis - previous.timeMillis)
    }

    private fun interpolateDistance(
        previous: TrackPoint,
        current: TrackPoint,
        timeMillis: Double,
    ): Double {
        val span = current.timeMillis - previous.timeMillis
        if (span <= 0L) {
            return current.distanceMeters
        }
        val fraction = (timeMillis - previous.timeMillis) / span
        return previous.distanceMeters + fraction * (current.distanceMeters - previous.distanceMeters)
    }

    private data class TrackPoint(
        val timeMillis: Long,
        val distanceMeters: Double,
    )

    private data class SplitBoundary(
        val timeMillis: Long,
        val distanceMeters: Int,
    )
}