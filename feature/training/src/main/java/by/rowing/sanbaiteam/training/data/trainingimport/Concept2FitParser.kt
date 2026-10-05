package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.core.util.TimeUtils
import com.garmin.fit.DateTime
import com.garmin.fit.FitDecoder
import com.garmin.fit.FitMessages
import com.garmin.fit.Intensity
import com.garmin.fit.LapMesg
import com.garmin.fit.RecordMesg
import com.garmin.fit.SessionMesg
import com.garmin.fit.Sport
import com.garmin.fit.SubSport
import java.io.ByteArrayInputStream
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

    override fun parse(bytes: ByteArray): ParsedTrainingImport {
        if (!bytes.hasFitMagic()) {
            throw IllegalArgumentException("File does not contain a FIT header.")
        }
        val fit = FitDecoder().decode(ByteArrayInputStream(bytes))
        if (!fit.sessionMesgs.firstOrNull().isRowingWorkout()) {
            throw IllegalArgumentException("FIT file does not contain a rowing workout.")
        }

        val laps = fit.lapMesgs.mapNotNull(::readLap)
        val records = fit.recordMesgs.mapNotNull(::readRecord).sortedBy { it.timestampMillis }
        val summaries = laps.mapIndexed { index, lap ->
            val lapRecords = records.forLap(
                lap = lap,
                isLast = index == laps.lastIndex
            )
            val heartRates = lapRecords.mapNotNull { it.heartRate }
            Concept2LapSummary(
                intensity = lap.intensity,
                startTimeMillis = lap.startMillis,
                totalElapsedTimeMillis = lap.elapsedMillis,
                totalDistanceMeters = lap.distanceMeters,
                avgHeartRate = heartRates.avg(),
                maxHeartRate = heartRates.maxOrNull(),
                avgCadence = lapRecords.mapNotNull { it.cadence }.avg(),
            )
        }
        val intervals = laps.mapIndexedNotNull { index, lap ->
            if (lap.intensity != Concept2LapIntensity.ACTIVE) return@mapIndexedNotNull null
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
            startTimeMillis = readStartMillis(fit),
            intervals = intervals,
        )
    }

    override fun merge(csvTexts: List<String>): String = Concept2LapCsv.merge(csvTexts)

    private fun readLap(lap: LapMesg): LapData? {
        val intensity = when (lap.intensity) {
            Intensity.ACTIVE -> Concept2LapIntensity.ACTIVE
            Intensity.REST -> Concept2LapIntensity.REST
            else -> return null
        }
        val elapsedMillis = (lap.totalElapsedTime?.toDouble() ?: 0.0).toLong() * TimeUtils.MILLIS_PER_SECOND
        if (elapsedMillis <= 0L) return null
        val endMillis = lap.timestamp?.toEpochMillis()
        val startMillis = lap.startTime?.toEpochMillis() ?: endMillis?.minus(elapsedMillis) ?: return null
        return LapData(
            intensity = intensity,
            startMillis = startMillis,
            endMillis = endMillis ?: (startMillis + elapsedMillis),
            elapsedMillis = elapsedMillis,
            distanceMeters = lap.totalDistance?.toDouble() ?: 0.0,
        )
    }

    private fun readRecord(record: RecordMesg): RecordData? {
        val timestampMillis = record.timestamp?.toEpochMillis() ?: return null
        return RecordData(
            timestampMillis = timestampMillis,
            heartRate = record.heartRate?.toInt()?.takeIf { it > 0 },
            cadence = record.cadence?.toInt()?.takeIf { it > 0 },
        )
    }

    private fun readStartMillis(fit: FitMessages): Long? =
        (fit.sessionMesgs.firstOrNull()?.timestamp ?: fit.fileIdMesgs.firstOrNull()?.timeCreated)?.toEpochMillis()

    private fun readRecoveryHeartRate(
        restLap: LapData?,
        records: List<RecordData>
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

    private fun List<RecordData>.forLap(
        lap: LapData,
        isLast: Boolean
    ): List<RecordData> = filter { record ->
        val isBeforeEnd = if (isLast) {
            record.timestampMillis <= lap.endMillis
        } else {
            record.timestampMillis < lap.endMillis
        }
        record.timestampMillis >= lap.startMillis && isBeforeEnd
    }

    private fun List<Int>.avg(): Int? = takeIf { it.isNotEmpty() }?.average()?.roundToInt()

    private fun SessionMesg?.isRowingWorkout(): Boolean =
        this == null || subSport == SubSport.INDOOR_ROWING || sport == Sport.ROWING || sport == Sport.FITNESS_EQUIPMENT

    private fun DateTime.toEpochMillis(): Long? =
        timestamp?.takeIf { it != DateTime.INVALID }?.let { it * TimeUtils.MILLIS_PER_SECOND }

    private fun ByteArray.hasFitMagic(): Boolean = size > FIT_HEADER_SIZE &&
            this[FIT_MAGIC_OFFSET] == FIT_MAGIC_DOT &&
            this[FIT_MAGIC_OFFSET + 1] == FIT_MAGIC_F &&
            this[FIT_MAGIC_OFFSET + 2] == FIT_MAGIC_I &&
            this[FIT_MAGIC_OFFSET + 3] == FIT_MAGIC_T

    private data class LapData(
        val intensity: Concept2LapIntensity,
        val startMillis: Long,
        val endMillis: Long,
        val elapsedMillis: Long,
        val distanceMeters: Double,
    )

    private data class RecordData(
        val timestampMillis: Long,
        val heartRate: Int?,
        val cadence: Int?,
    )

    private const val FIT_MAGIC_DOT = '.'.code.toByte()
    private const val FIT_MAGIC_F = 'F'.code.toByte()
    private const val FIT_MAGIC_I = 'I'.code.toByte()
    private const val FIT_MAGIC_T = 'T'.code.toByte()
}