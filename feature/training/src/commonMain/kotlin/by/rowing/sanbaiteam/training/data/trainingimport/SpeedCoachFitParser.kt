package by.rowing.sanbaiteam.training.data.trainingimport

import kotlin.math.roundToInt

/**
 * Reads SpeedCoach `.fit` files.
 *
 * SpeedCoach writes one `lap` per interval and does not set `intensity`, so every lap is an
 * active piece. Aggregate heart rate and stroke rate are not present in laps and are recalculated
 * from the per-stroke `record` messages.
 *
 * Known SpeedCoach quirks handled here:
 * - `session.total_timer_time` is wrong for multi-lap sessions (equals the last lap), so only
 *   per-lap values are used;
 * - `record.cycle_length` (field 12) is written as a single byte and is unusable, so distance and
 *   stroke rate come from laps and records instead.
 */
internal object SpeedCoachFitParser : TrainingImportParser {

    override val source = TrainingImportSource.SPEED_COACH

    override fun matches(bytes: ByteArray): Boolean {
        if (!bytes.hasFitMagic()) return false
        val workout = runCatching { FitWorkoutReader.decode(bytes) }.getOrNull() ?: return false
        return workout.sport == SPORT_ROWING && workout.manufacturer != MANUFACTURER_CONCEPT2
    }

    override fun parse(
        bytes: ByteArray,
        fileName: String?
    ): ParsedTrainingImport {
        val workout = FitWorkoutReader.decode(bytes).takeIf { it.isRowing }
            ?: throw IllegalArgumentException(FitWorkoutReader.FIT_FILE_NOT_ROWING_ERROR)
        val laps = workout.laps
        require(laps.isNotEmpty()) { "FIT file does not contain any laps." }
        val records = workout.records.sortedBy { it.timestampMillis }
        return ParsedTrainingImport(
            source = source,
            rawFile = bytes,
            fileExtension = FIT_FILE_EXTENSION,
            deviceSerial = workout.deviceSerial,
            startTimeMillis = workout.startTimeMillis,
            intervals = laps.mapIndexed { index, lap ->
                val lapRecords = records.forLap(
                    lap = lap,
                    isLast = index == laps.lastIndex
                )
                val heartRates = lapRecords.mapNotNull { it.heartRate }
                TrainingImportInterval(
                    distanceMeters = lap.distanceMeters.roundToInt(),
                    timeMillis = lap.elapsedMillis,
                    strokeRate = lapRecords.mapNotNull { it.cadence }.avg() ?: 0.0,
                    avgHeartRate = heartRates.avg()?.roundToInt(),
                    maxHeartRate = heartRates.maxOrNull(),
                    recoveryHeartRate = null,
                )
            },
        )
    }
}