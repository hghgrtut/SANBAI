package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord
import by.rowing.sanbaiteam.training.data.models.FitWorkout
import kotlin.math.roundToInt

/**
 * Reads Concept2 PM5 logbook `.fit` files (RowErg and BikeErg).
 *
 * Only active laps become training pieces, rest laps provide the recovery heart rate.
 * A workout without any rest lap (just ride, single time/distance) is saved as one
 * continuous piece instead of the automatic PM5 splits.
 * Heart rate and stroke rate metrics are taken from `record` messages.
 */
internal object Concept2FitParser : TrainingImportParser {

    override val source = TrainingImportSource.CONCEPT2_FIT

    private const val RECOVERY_OFFSET_MILLIS = 90_000L

    private const val FIT_FILE_NOT_CONCEPT2_ERROR = "FIT file does not contain a Concept2 workout."

    override fun matches(bytes: ByteArray): Boolean {
        if (!bytes.hasFitMagic()) return false
        val workout = runCatching { FitWorkoutReader.decode(bytes) }.getOrNull() ?: return false
        return workout.concept2Kind() != null
    }

    override fun parse(
        bytes: ByteArray,
        fileName: String?
    ): ParsedTrainingImport {
        val workout = FitWorkoutReader.decode(bytes)
        val trainingType = workout.concept2Kind() ?: throw IllegalArgumentException(FIT_FILE_NOT_CONCEPT2_ERROR)
        val laps = workout.laps
        val records = workout.records.sortedBy { it.timestampMillis }
        val intervals = laps.concept2PieceRanges().map { range ->
            val pieceRecords = range.flatMap { index ->
                records.forLap(
                    lap = laps[index],
                    isLast = index == laps.lastIndex
                )
            }
            val heartRates = pieceRecords.mapNotNull { it.heartRate }
            TrainingImportInterval(
                distanceMeters = range.sumOf { laps[it].distanceMeters }.roundToInt(),
                timeMillis = range.sumOf { laps[it].elapsedMillis },
                strokeRate = pieceRecords.mapNotNull { it.cadence }.avg() ?: 0.0,
                avgHeartRate = heartRates.avg()?.roundToInt(),
                maxHeartRate = heartRates.maxOrNull(),
                recoveryHeartRate = readRecoveryHeartRate(
                    restLap = laps.getOrNull(range.last + 1),
                    records = records
                )
            )
        }

        return ParsedTrainingImport(
            source = source,
            trainingType = trainingType,
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

    private fun FitWorkout.concept2Kind(): TrainingPieceType? = when (sport) {
        SPORT_FITNESS_EQUIPMENT if subSport == SUB_SPORT_INDOOR_ROWING -> TrainingPieceType.CONCEPT_ROW_ERG
        SPORT_CYCLING if subSport == SUB_SPORT_INDOOR_CYCLING && manufacturer == MANUFACTURER_CONCEPT2 ->
            TrainingPieceType.CONCEPT_BIKE_ERG

        else -> null
    }
}