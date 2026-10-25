package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord
import by.rowing.sanbaiteam.training.data.models.FitWorkout
import kotlin.math.roundToInt

/** Shared FIT decoding helpers used by every FIT based parser. */
internal object FitWorkoutReader {

    fun decode(bytes: ByteArray): FitWorkout {
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
        )
        return FitWorkout(
            isRowing = fileData.isRowing,
            sport = fileData.sport,
            subSport = fileData.subSport,
            manufacturer = fileData.manufacturer,
            deviceSerial = fileData.deviceSerial,
            startTimeMillis = fileData.startTimeMillis,
            laps = fileData.laps.toList(),
            records = fileData.records.toList()
        )
    }

    const val FIT_FILE_HEADER_ERROR = "File does not contain a FIT header."
    const val FIT_FILE_NOT_ROWING_ERROR = "FIT file does not contain a rowing workout."
}

internal fun ByteArray.hasFitMagic(): Boolean = size > FIT_HEADER_MAGIC_OFFSET + 3 &&
        this[FIT_HEADER_MAGIC_OFFSET] == FIT_MAGIC_DOT &&
        this[FIT_HEADER_MAGIC_OFFSET + 1] == FIT_MAGIC_F &&
        this[FIT_HEADER_MAGIC_OFFSET + 2] == FIT_MAGIC_I &&
        this[FIT_HEADER_MAGIC_OFFSET + 3] == FIT_MAGIC_T

internal fun List<FitRecord>.forLap(
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

internal fun List<Int>.avg(): Int? = takeIf { it.isNotEmpty() }?.average()?.roundToInt()

private const val FIT_MAGIC_DOT = '.'.code.toByte()
private const val FIT_MAGIC_F = 'F'.code.toByte()
private const val FIT_MAGIC_I = 'I'.code.toByte()
private const val FIT_MAGIC_T = 'T'.code.toByte()