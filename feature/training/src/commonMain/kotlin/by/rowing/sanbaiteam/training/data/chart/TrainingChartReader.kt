package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint
import by.rowing.sanbaiteam.training.data.models.chart.TrainingPieceChartData
import by.rowing.sanbaiteam.training.data.trainingimport.FitWorkoutReader
import by.rowing.sanbaiteam.training.data.trainingimport.Rp3CsvParser
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportContainer
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportParsers
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.forLap

/**
 * Builds per-piece chart series from the original imported file.
 *
 * Only rows that were imported with a source mapping (file index + interval index) can be
 * charted. Files that are not recognized by the current parsers (legacy SpeedCoach/Concept2
 * CSV) deliberately produce no chart data.
 */
internal class TrainingChartReader {

    fun read(
        sourceFileBytes: ByteArray,
        sourceFileIndex: Int,
        sourceIntervalIndex: Int,
    ): TrainingPieceChartData? {
        val files = TrainingImportContainer.unpackOrSingle(sourceFileBytes)
        val file = files.getOrNull(sourceFileIndex) ?: return null
        val parser = TrainingImportParsers.detect(file) ?: return null

        return when (parser.source) {
            TrainingImportSource.SPEED_COACH -> readFit(
                bytes = file,
                intervalIndex = sourceIntervalIndex,
                activeOnly = false,
            )

            TrainingImportSource.CONCEPT2_FIT -> readFit(
                bytes = file,
                intervalIndex = sourceIntervalIndex,
                activeOnly = true,
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
    ): TrainingPieceChartData? {
        val workout = runCatching { FitWorkoutReader.decode(bytes) }.getOrNull() ?: return null
        val allLaps = workout.laps
        val lap = (if (activeOnly) allLaps.filter { it.isActive } else allLaps).getOrNull(intervalIndex) ?: return null
        val lapPosition = allLaps.indexOfFirst { it.startMillis == lap.startMillis && it.endMillis == lap.endMillis }
        val records = workout.records
            .sortedBy { it.timestampMillis }
            .forLap(
                lap = lap,
                isLast = lapPosition == allLaps.lastIndex,
            )

        return buildFitData(
            records = records,
            lap = lap,
        )
    }

    private fun buildFitData(
        records: List<FitRecord>,
        lap: FitLap,
    ): TrainingPieceChartData {
        val speed = mutableListOf<TrainingChartPoint>()
        val strokeRate = mutableListOf<TrainingChartPoint>()
        val heartRate = mutableListOf<TrainingChartPoint>()
        var previous: FitRecord? = null

        records.forEach { record ->
            val elapsed = (record.timestampMillis - lap.startMillis)
                .coerceIn(0L, lap.elapsedMillis)
            (record.speedMetersPerSecond?.takeIf { it > 0.0 } ?: calculatedSpeed(
                previous = previous,
                current = record
            ))?.takeIf { it > 0.0 }
                ?.let { PACE_DISTANCE_METERS / it }
                ?.takeIf { it in MIN_PACE_SECONDS..MAX_PACE_SECONDS }
                ?.let {
                    speed.appendPoint(
                        elapsedMillis = elapsed,
                        value = it
                    )
                }

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
        }

        return TrainingPieceChartData(
            durationMillis = lap.elapsedMillis,
            speed = speed,
            strokeRate = strokeRate,
            heartRate = heartRate,
        )
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
        var previous: Rp3CsvParser.Rp3StrokeRow? = null

        group.forEach { stroke ->
            val elapsed = ((stroke.timeSeconds - firstTime) * MILLIS_PER_SECOND)
                .toLong()
                .coerceIn(0L, durationMillis)
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
        private const val MAX_PACE_SECONDS = 500.0
        private const val MILLIS_PER_SECOND = 1000.0
    }
}