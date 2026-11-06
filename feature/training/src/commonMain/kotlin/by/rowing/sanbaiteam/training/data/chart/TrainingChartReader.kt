package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint
import by.rowing.sanbaiteam.training.data.models.chart.TrainingPieceChartData
import by.rowing.sanbaiteam.training.data.models.chart.chartPaceDistanceMeters
import by.rowing.sanbaiteam.training.data.trainingimport.FitWorkoutReader
import by.rowing.sanbaiteam.training.data.trainingimport.Rp3CsvParser
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportContainer
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportParsers
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.concept2PieceRanges
import by.rowing.sanbaiteam.training.data.trainingimport.forLap

/**
 * Builds per-piece chart series from the original imported file.
 *
 * Only rows that were imported with a source mapping (file index + interval index) can be
 * charted. Files that are not recognized by the current parsers (legacy SpeedCoach/Concept2
 * CSV) deliberately produce no chart data.
 *
 * Concept2 pieces follow the same split rule as the parser: a workout without rest laps is a
 * single piece, so chart data is built from all of its active laps.
 */
internal class TrainingChartReader {

    fun read(
        sourceFileBytes: ByteArray,
        sourceFileIndex: Int,
        sourceIntervalIndex: Int,
        pieceType: TrainingPieceType,
    ): TrainingPieceChartData? {
        val files = TrainingImportContainer.unpackOrSingle(sourceFileBytes)
        val file = files.getOrNull(sourceFileIndex) ?: return null
        val parser = TrainingImportParsers.detect(file) ?: return null

        return when (parser.source) {
            TrainingImportSource.SPEED_COACH -> readFit(
                bytes = file,
                intervalIndex = sourceIntervalIndex,
                activeOnly = false,
                pieceType = pieceType,
            )

            TrainingImportSource.CONCEPT2_FIT -> readFit(
                bytes = file,
                intervalIndex = sourceIntervalIndex,
                activeOnly = true,
                pieceType = pieceType,
            )

            TrainingImportSource.RP3_CSV -> readRp3(
                bytes = file,
                intervalIndex = sourceIntervalIndex,
            )
        }
    }

    private fun readFit(
        bytes: ByteArray,
        intervalIndex: Int,
        activeOnly: Boolean,
        pieceType: TrainingPieceType,
    ): TrainingPieceChartData? {
        val workout = runCatching { FitWorkoutReader.decode(bytes) }.getOrNull() ?: return null
        val allLaps = workout.laps
        val lapIndices = if (activeOnly) {
            allLaps.concept2PieceRanges().getOrNull(intervalIndex)?.toList() ?: return null
        } else {
            if (allLaps.getOrNull(intervalIndex) == null) return null
            listOf(intervalIndex)
        }

        return buildFitData(
            records = workout.records.sortedBy { it.timestampMillis },
            laps = allLaps,
            lapIndices = lapIndices,
            pieceType = pieceType,
        )
    }

    private fun buildFitData(
        records: List<FitRecord>,
        laps: List<FitLap>,
        lapIndices: List<Int>,
        pieceType: TrainingPieceType,
    ): TrainingPieceChartData {
        val speed = mutableListOf<TrainingChartPoint>()
        val strokeRate = mutableListOf<TrainingChartPoint>()
        val heartRate = mutableListOf<TrainingChartPoint>()
        val distance = mutableListOf<TrainingChartPoint>()
        val paceDistance = chartPaceDistanceMeters(pieceType).toDouble()
        val paceRange = paceRange(pieceType)
        var previous: FitRecord? = null
        var offsetMillis = 0L
        var offsetDistance = 0.0

        lapIndices.forEach { lapIndex ->
            val lap = laps[lapIndex]
            val lapRecords = records.forLap(
                lap = lap,
                isLast = lapIndex == laps.lastIndex,
            )
            val lapStartDistance = lapRecords.firstNotNullOfOrNull { it.distanceMeters }
            var previousInLap: FitRecord? = null
            var currentLapDistance = 0.0
            lapRecords.forEach { record ->
                val elapsed = offsetMillis + (record.timestampMillis - lap.startMillis)
                    .coerceIn(0L, lap.elapsedMillis)
                val speedMetersPerSecond = record.speedMetersPerSecond?.takeIf { it > 0.0 } ?: calculatedSpeed(
                    previous = previous,
                    current = record
                )

                speedMetersPerSecond?.takeIf { it > 0.0 }
                    ?.let { paceDistance / it }
                    ?.takeIf { it in paceRange }
                    ?.let {
                        speed.appendPoint(
                            elapsedMillis = elapsed,
                            value = it
                        )
                    }

                currentLapDistance = calculateLapDistance(
                    record = record,
                    lapStartDistance = lapStartDistance,
                    previousTimestamp = previousInLap?.timestampMillis,
                    previousDistance = currentLapDistance,
                    speedMetersPerSecond = speedMetersPerSecond,
                )
                distance.appendPoint(
                    elapsedMillis = elapsed,
                    value = offsetDistance + currentLapDistance
                )

                record.cadence?.takeIf { it > 0 }?.let {
                    strokeRate.appendPoint(
                        elapsedMillis = elapsed,
                        value = it.toDouble()
                    )
                }

                record.heartRate?.takeIf { it > 0 }?.let {
                    heartRate.appendPoint(
                        elapsedMillis = elapsed,
                        value = it.toDouble()
                    )
                }

                previous = record
                previousInLap = record
            }
            offsetDistance += maxOf(currentLapDistance, lap.distanceMeters)
            offsetMillis += lap.elapsedMillis
        }

        return TrainingPieceChartData(
            durationMillis = offsetMillis,
            speed = speed,
            strokeRate = strokeRate,
            heartRate = heartRate,
            distance = distance,
        )
    }

    private fun calculateLapDistance(
        record: FitRecord,
        lapStartDistance: Double?,
        previousTimestamp: Long?,
        previousDistance: Double,
        speedMetersPerSecond: Double?,
    ): Double {
        val recordDistance = record.distanceMeters
        if (recordDistance != null && lapStartDistance != null) {
            return (recordDistance - lapStartDistance).coerceAtLeast(0.0)
        }
        val deltaMillis = record.timestampMillis - (previousTimestamp ?: record.timestampMillis)
        if (deltaMillis <= 0L) return previousDistance
        return previousDistance + (speedMetersPerSecond ?: 0.0) * deltaMillis / MILLIS_PER_SECOND
    }

    private fun paceRange(pieceType: TrainingPieceType?): ClosedFloatingPointRange<Double> =
        if (pieceType == TrainingPieceType.CONCEPT_BIKE_ERG) {
            MIN_BIKE_PACE_SECONDS..MAX_PACE_SECONDS
        } else {
            MIN_PACE_SECONDS..MAX_PACE_SECONDS
        }

    private fun calculatedSpeed(
        previous: FitRecord?,
        current: FitRecord,
    ): Double? {
        val previousRecord = previous ?: return null
        val previousDistance = previousRecord.distanceMeters ?: return null
        val currentDistance = current.distanceMeters ?: return null
        val deltaMillis = current.timestampMillis - previousRecord.timestampMillis
        val deltaDistance = currentDistance - previousDistance
        if (deltaMillis <= 0L || deltaDistance <= 0.0) return null
        return deltaDistance / (deltaMillis / MILLIS_PER_SECOND)
    }

    private fun readRp3(
        bytes: ByteArray,
        intervalIndex: Int,
    ): TrainingPieceChartData? {
        val strokes = runCatching { Rp3CsvParser.readStrokes(bytes.decodeToString()) }.getOrNull()
            ?: return null
        val groups = strokes.groupConsecutiveByInterval()
        val group = groups.getOrNull(intervalIndex) ?: return null
        val firstTime = group.first().timeSeconds
        val durationMillis = ((group.last().timeSeconds - firstTime) * MILLIS_PER_SECOND)
            .toLong()
            .coerceAtLeast(0L)

        val speed = mutableListOf<TrainingChartPoint>()
        val strokeRate = mutableListOf<TrainingChartPoint>()
        val heartRate = mutableListOf<TrainingChartPoint>()
        val distance = mutableListOf<TrainingChartPoint>()
        val startDistance = group.first().distanceMeters
        var maxDistance = 0.0
        var previous: Rp3CsvParser.Rp3StrokeRow? = null

        group.forEach { stroke ->
            val elapsed = ((stroke.timeSeconds - firstTime) * MILLIS_PER_SECOND)
                .toLong()
                .coerceIn(0L, durationMillis)
            maxDistance = maxOf(maxDistance, stroke.distanceMeters - startDistance)
            distance.appendPoint(
                elapsedMillis = elapsed,
                value = maxDistance
            )
            val pace = stroke.estimated500mTimeSeconds
                ?.takeIf { it in MIN_PACE_SECONDS..MAX_PACE_SECONDS }
                ?: calculatedPace(
                    previous = previous,
                    current = stroke
                )
            pace
                ?.takeIf { it in MIN_PACE_SECONDS..MAX_PACE_SECONDS }
                ?.let {
                    speed.appendPoint(
                        elapsedMillis = elapsed,
                        value = it
                    )
                }

            stroke.strokeRate?.takeIf { it > 0.0 }?.let {
                strokeRate.appendPoint(
                    elapsedMillis = elapsed,
                    value = it
                )
            }

            stroke.pulse?.takeIf { it > 0 }?.let {
                heartRate.appendPoint(
                    elapsedMillis = elapsed,
                    value = it.toDouble()
                )
            }

            previous = stroke
        }

        return TrainingPieceChartData(
            durationMillis = durationMillis,
            speed = speed,
            strokeRate = strokeRate,
            heartRate = heartRate,
            distance = distance,
        )
    }

    private fun calculatedPace(
        previous: Rp3CsvParser.Rp3StrokeRow?,
        current: Rp3CsvParser.Rp3StrokeRow,
    ): Double? {
        val previousRow = previous ?: return null
        val deltaTime = current.timeSeconds - previousRow.timeSeconds
        val deltaDistance = current.distanceMeters - previousRow.distanceMeters
        return if (deltaTime <= 0.0 || deltaDistance <= 0.0) null else PACE_DISTANCE_METERS * deltaTime / deltaDistance
    }

    private fun List<Rp3CsvParser.Rp3StrokeRow>.groupConsecutiveByInterval(): List<List<Rp3CsvParser.Rp3StrokeRow>> {
        if (isEmpty()) {
            return emptyList()
        }
        val groups = mutableListOf<MutableList<Rp3CsvParser.Rp3StrokeRow>>()
        forEach { stroke ->
            val current = groups.lastOrNull()
            if (current != null && current.first().intervalId == stroke.intervalId) {
                current += stroke
            } else {
                groups += mutableListOf(stroke)
            }
        }
        return groups
    }

    private fun MutableList<TrainingChartPoint>.appendPoint(
        elapsedMillis: Long,
        value: Double,
    ) {
        val last = lastOrNull()
        if (last?.elapsedMillis == elapsedMillis) {
            this[lastIndex] = TrainingChartPoint(
                elapsedMillis = elapsedMillis,
                value = value
            )
        } else {
            add(
                TrainingChartPoint(
                    elapsedMillis = elapsedMillis,
                    value = value
                )
            )
        }
    }

    companion object {

        private const val PACE_DISTANCE_METERS = 500.0
        private const val MIN_PACE_SECONDS = 60.0
        private const val MIN_BIKE_PACE_SECONDS = 40.0
        private const val MAX_PACE_SECONDS = 500.0
        private const val MILLIS_PER_SECOND = 1000.0
    }
}