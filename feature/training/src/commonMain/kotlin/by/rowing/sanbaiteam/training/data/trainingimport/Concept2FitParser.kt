package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord
import kotlin.math.roundToInt

/**
 * Reads Concept2 RowErg logbook `.fit` files written by PM5 monitors.
 *
 * Only active laps become training pieces, rest laps provide the recovery heart rate.
 * Heart rate and stroke rate metrics are taken from `record` messages.
 */
internal object Concept2FitParser : TrainingImportParser {

    override val source = TrainingImportSource.CONCEPT2_FIT

    private const val RECOVERY_OFFSET_MILLIS = 90_000L

    override fun matches(bytes: ByteArray): Boolean {
        if (!bytes.hasFitMagic()) return false
        val workout = runCatching { FitWorkoutReader.decode(bytes) }.getOrNull() ?: return false
        return workout.sport == SPORT_FITNESS_EQUIPMENT && workout.subSport == SUB_SPORT_INDOOR_ROWING
    }

    override fun parse(
        bytes: ByteArray,
        fileName: String?
    ): ParsedTrainingImport {
        val workout = FitWorkoutReader.decode(bytes).takeIf { it.isRowing }
            ?: throw IllegalArgumentException(FitWorkoutReader.FIT_FILE_NOT_ROWING_ERROR)
        val laps = workout.laps
        val records = workout.records.sortedBy { it.timestampMillis }
        val summaries = laps.mapIndexed { index, lap ->
            val lapRecords = records.forLap(
                lap = lap,
                isLast = index == laps.lastIndex
            )
            LapSummary(
                heartRates = lapRecords.mapNotNull { it.heartRate },
                cadences = lapRecords.mapNotNull { it.cadence },
            )
        }
        val intervals = laps.mapIndexedNotNull { index, lap ->
            if (!lap.isActive) return@mapIndexedNotNull null
            val summary = summaries[index]
            TrainingImportInterval(
                distanceMeters = lap.distanceMeters.roundToInt(),
                timeMillis = lap.elapsedMillis,
                strokeRate = summary.cadences.avg() ?: 0.0,
                avgHeartRate = summary.heartRates.avg()?.roundToInt(),
                maxHeartRate = summary.heartRates.maxOrNull(),
                recoveryHeartRate = readRecoveryHeartRate(
                    restLap = laps.getOrNull(index + 1),
                    records = records
                )
            )
        }

        return ParsedTrainingImport(
            source = source,
            rawFile = bytes,
            fileExtension = FIT_FILE_EXTENSION,
            deviceSerial = workout.deviceSerial,
            startTimeMillis = workout.startTimeMillis,
            intervals = intervals,
        )
    }

    private fun readRecoveryHeartRate(
        restLap: FitLap?,
        records: List<FitRecord>
    ): Int? {
        val targetMillis = (restLap?.startMillis ?: return null) + RECOVERY_OFFSET_MILLIS
        val heartRates = records.filter { it.heartRate != null }
        val before = heartRates.lastOrNull { it.timestampMillis <= targetMillis }
        val after = heartRates.firstOrNull { it.timestampMillis > targetMillis }
        val beforeHeartRate = before?.heartRate ?: return after?.heartRate
        val afterHeartRate = after?.heartRate ?: return beforeHeartRate
        val heartRateDetectionDeltaMillis = (after.timestampMillis - before.timestampMillis)
        val ratio = (targetMillis - before.timestampMillis).toDouble() / heartRateDetectionDeltaMillis
        return (beforeHeartRate + (afterHeartRate - beforeHeartRate) * ratio).roundToInt()
    }

    private data class LapSummary(
        val heartRates: List<Int>,
        val cadences: List<Int>,
    )
}