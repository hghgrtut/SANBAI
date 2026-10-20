package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord
import by.rowing.sanbaiteam.training.data.models.FitWorkout
import kotlin.math.roundToInt

/**
 * Reads Concept2 RowErg logbook `.fit` files written by PM5 monitors.
 *
 * Only active laps become training pieces, rest laps provide the recovery heart rate.
 * Heart rate and stroke rate metrics are taken from `record` messages.
 */
internal object Concept2FitParser : TrainingImportParser {

    override val source = TrainingImportSource.CONCEPT2_FIT

    private const val FIT_HEADER_SIZE = 12
    private const val FIT_MAGIC_OFFSET = 8
    private const val RECOVERY_OFFSET_MILLIS = 90_000L

    override fun matches(bytes: ByteArray): Boolean = bytes.hasFitMagic()

    override fun parse(
        bytes: ByteArray,
        fileName: String?
    ): ParsedTrainingImport {
        val workout = decodeFitWorkout(bytes)
        val laps = workout.laps
        val records = workout.records.sortedBy { it.timestampMillis }
        val summaries = laps.mapIndexed { index, lap ->
            val lapRecords = records.forLap(
                lap = lap,
                isLast = index == laps.lastIndex
            )
            val heartRates = lapRecords.mapNotNull { it.heartRate }
            Concept2LapSummary(
                intensity = if (lap.isActive) Concept2LapIntensity.ACTIVE else Concept2LapIntensity.REST,
                startTimeMillis = lap.startMillis,
                totalElapsedTimeMillis = lap.elapsedMillis,
                totalDistanceMeters = lap.distanceMeters,
                avgHeartRate = heartRates.avg(),
                maxHeartRate = heartRates.maxOrNull(),
                avgCadence = lapRecords.mapNotNull { it.cadence }.avg(),
            )
        }
        val intervals = laps.mapIndexedNotNull { index, lap ->
            if (!lap.isActive) return@mapIndexedNotNull null
            val summary = summaries[index]
            TrainingImportInterval(
                distanceMeters = summary.totalDistanceMeters.roundToInt(),
                timeMillis = summary.totalElapsedTimeMillis,
                strokeRate = summary.avgCadence?.toDouble() ?: 0.0,
                avgHeartRate = summary.avgHeartRate,
                maxHeartRate = summary.maxHeartRate,
                recoveryHeartRate = readRecoveryHeartRate(
                    restLap = laps.getOrNull(index + 1),
                    records = records
                )
            )
        }

        return ParsedTrainingImport(
            source = source,
            csvText = Concept2LapCsv.encode(summaries),
            deviceSerial = null,
            sessionName = null,
            startTimeMillis = workout.startTimeMillis,
            intervals = intervals,
        )
    }

    override fun merge(csvTexts: List<String>): String = Concept2LapCsv.merge(csvTexts)

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

    private fun decodeFitWorkout(bytes: ByteArray): FitWorkout {
        if (bytes.size < FIT_HEADER_MIN_SIZE || !bytes.hasFitMagic()) {
            throw IllegalArgumentException(FIT_FILE_HEADER_ERROR)
        }
        val reader = ByteReader(bytes)
        val header = FileHeader.read(reader).takeIf { it.isValid }
            ?: throw IllegalArgumentException(FIT_FILE_HEADER_ERROR)
        val dataStart = reader.position
        val dataEnd = dataStart + header.dataSize.toInt()
        val fileData = readDataSection(
            bytes = bytes,
            start = dataStart,
            end = dataEnd
        ).takeIf { it.isRowing } ?: throw IllegalArgumentException(FIT_FILE_NOT_ROWING_ERROR)
        return FitWorkout(
            isRowing = fileData.isRowing,
            startTimeMillis = fileData.startTimeMillis,
            laps = fileData.laps.toList(),
            records = fileData.records.toList()
        )
    }

    private fun List<FitRecord>.forLap(
        lap: FitLap,
        isLast: Boolean
    ): List<FitRecord> = filter { record ->
        val isBeforeEnd = if (isLast) {
            record.timestampMillis <= lap.endMillis
        } else {
            record.timestampMillis < lap.endMillis
        }
        record.timestampMillis >= lap.startMillis && isBeforeEnd
    }

    private fun List<Int>.avg(): Int? = takeIf { it.isNotEmpty() }?.average()?.roundToInt()

    private fun ByteArray.hasFitMagic(): Boolean = size > FIT_HEADER_SIZE &&
            this[FIT_MAGIC_OFFSET] == FIT_MAGIC_DOT &&
            this[FIT_MAGIC_OFFSET + 1] == FIT_MAGIC_F &&
            this[FIT_MAGIC_OFFSET + 2] == FIT_MAGIC_I &&
            this[FIT_MAGIC_OFFSET + 3] == FIT_MAGIC_T

    private const val FIT_MAGIC_DOT = '.'.code.toByte()
    private const val FIT_MAGIC_F = 'F'.code.toByte()
    private const val FIT_MAGIC_I = 'I'.code.toByte()
    private const val FIT_MAGIC_T = 'T'.code.toByte()

    private const val FIT_FILE_HEADER_ERROR = "File does not contain a FIT header."
    private const val FIT_FILE_NOT_ROWING_ERROR = "FIT file does not contain a rowing workout."
}
