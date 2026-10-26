package by.rowing.sanbaiteam.training.data.trainingimport

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Reads RP3 stroke-by-stroke `.csv` exports.
 *
 * Every row describes a single stroke, `workout_interval_id` groups strokes into pieces and the
 * cumulative `distance` counter restarts at the beginning of each piece. The raw file is stored
 * as-is, so nothing from the original export is lost.
 */
internal object Rp3CsvParser : TrainingImportParser {

    override val source = TrainingImportSource.RP3_CSV

    private const val HEADER_PREFIX = "id,workout_interval_id,"
    private const val BOM = '\uFEFF'

    private const val COLUMN_INTERVAL_ID = "workout_interval_id"
    private const val COLUMN_STROKE_RATE = "stroke_rate"
    private const val COLUMN_TIME = "time"
    private const val COLUMN_DISTANCE = "distance"
    private const val COLUMN_PULSE = "pulse"
    private const val COLUMN_ESTIMATED_500M_TIME = "estimated_500m_time"

    private val FILE_NAME_DATE_REGEX = Regex("^(\\d{12})")

    override fun matches(bytes: ByteArray): Boolean = hasRp3CsvContent(csvText = bytes.decodeToString())

    fun hasRp3CsvContent(csvText: String): Boolean =
        csvText.trimStart(BOM).trimStart().startsWith(HEADER_PREFIX)

    override fun parse(
        bytes: ByteArray,
        fileName: String?
    ): ParsedTrainingImport {
        val csvText = bytes.decodeToString()
        if (!hasRp3CsvContent(csvText)) {
            throwParseError()
        }

        return ParsedTrainingImport(
            source = source,
            rawFile = bytes,
            fileExtension = CSV_FILE_EXTENSION,
            deviceSerial = null,
            startTimeMillis = parseStartTimeMillis(fileName),
            intervals = readStrokes(csvText).toIntervals(),
        )
    }

    internal fun readStrokes(csvText: String): List<Rp3StrokeRow> {
        if (!hasRp3CsvContent(csvText)) {
            throwParseError()
        }
        val lines = csvText.normalizeLines()
        val headerIndex = lines.indexOfFirst { it.trimStart(BOM).trimStart().startsWith(HEADER_PREFIX) }
        val header = lines[headerIndex].splitRp3Row()
        val columns = header.withIndex().associate { (index, name) -> name.trim() to index }

        fun getColumnIndex(columnName: String): Int = columns[columnName]
            ?: throwParseError("Missing RP3 column $columnName.")

        return lines.asSequence()
            .drop(headerIndex + 1)
            .mapNotNull { line ->
                line.parseStroke(
                    intervalIndex = getColumnIndex(columnName = COLUMN_INTERVAL_ID),
                    timeIndex = getColumnIndex(columnName = COLUMN_TIME),
                    distanceIndex = getColumnIndex(columnName = COLUMN_DISTANCE),
                    strokeRateIndex = getColumnIndex(columnName = COLUMN_STROKE_RATE),
                    pulseIndex = getColumnIndex(columnName = COLUMN_PULSE),
                    estimated500mIndex = getColumnIndex(columnName = COLUMN_ESTIMATED_500M_TIME),
                )
            }
            .toList()
            .ifEmpty { throwParseError() }
    }

    private fun List<Rp3StrokeRow>.toIntervals(): List<TrainingImportInterval> {
        val intervals = mutableListOf<TrainingImportInterval>()
        var startIndex = 0
        while (startIndex < size) {
            var endIndex = startIndex + 1
            while (endIndex < size && this[endIndex].intervalId == this[startIndex].intervalId) {
                endIndex++
            }
            val previous = getOrNull(startIndex - 1)
            intervals += subList(startIndex, endIndex).toInterval(
                boundaryDistance = previous?.distanceMeters ?: 0.0,
                boundaryTime = previous?.timeSeconds ?: 0.0
            )
            startIndex = endIndex
        }
        return intervals
    }

    private fun List<Rp3StrokeRow>.toInterval(
        boundaryDistance: Double,
        boundaryTime: Double
    ): TrainingImportInterval {
        val reset = first().distanceMeters < boundaryDistance
        val distanceBoundary = if (reset) 0.0 else boundaryDistance
        val heartRates = mapNotNull { it.pulse }
        val strokeRates = mapNotNull { it.strokeRate }
        return TrainingImportInterval(
            distanceMeters = (last().distanceMeters - distanceBoundary).roundToInt(),
            timeMillis = ((last().timeSeconds - boundaryTime) * MILLIS_PER_SECOND).roundToLong(),
            strokeRate = if (strokeRates.isEmpty()) 0.0 else strokeRates.average(),
            avgHeartRate = heartRates.avg()?.roundToInt(),
            maxHeartRate = heartRates.maxOrNull(),
            recoveryHeartRate = null,
        )
    }

    private fun String.parseStroke(
        intervalIndex: Int,
        timeIndex: Int,
        distanceIndex: Int,
        strokeRateIndex: Int,
        pulseIndex: Int,
        estimated500mIndex: Int,
    ): Rp3StrokeRow? {
        if (isBlank()) return null
        val columns = splitRp3Row()
        if (columns.size <= maxOf(intervalIndex, timeIndex, distanceIndex, strokeRateIndex, pulseIndex)) {
            return null
        }
        val intervalId = columns[intervalIndex].trim()
        val time = columns[timeIndex].trim().toDoubleOrNull() ?: return null
        val distance = columns[distanceIndex].trim().toDoubleOrNull() ?: return null
        return Rp3StrokeRow(
            intervalId = intervalId,
            timeSeconds = time,
            distanceMeters = distance,
            strokeRate = columns[strokeRateIndex].trim().toDoubleOrNull(),
            pulse = columns[pulseIndex].trim().toDoubleOrNull()?.roundToInt()?.takeIf { it > 0 },
            estimated500mTimeSeconds = columns[estimated500mIndex].trim().toDoubleOrNull()?.takeIf { it > 0 },
        )
    }

    private fun parseStartTimeMillis(fileName: String?): Long? {
        val date = FILE_NAME_DATE_REGEX.find(fileName ?: return null)?.value ?: return null
        return runCatching {
            LocalDateTime(
                year = date.substring(0, 4).toInt(),
                month = date.substring(4, 6).toInt(),
                day = date.substring(6, 8).toInt(),
                hour = date.substring(8, 10).toInt(),
                minute = date.substring(10, 12).toInt()
            ).toInstant(TimeZone.UTC).toEpochMilliseconds()
        }.getOrNull()
    }

    private fun throwParseError(errorText: String = "CSV does not contain RP3 stroke data."): Nothing =
        throw IllegalArgumentException(errorText)

    private fun String.normalizeLines(): List<String> = replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')

    internal data class Rp3StrokeRow(
        val intervalId: String,
        val timeSeconds: Double,
        val distanceMeters: Double,
        val strokeRate: Double?,
        val pulse: Int?,
        val estimated500mTimeSeconds: Double? = null,
    )

    private const val MILLIS_PER_SECOND = 1000L
}

private fun String.splitRp3Row(): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var index = 0
    while (index < length) {
        when (val symbol = this[index]) {
            '"' -> {
                if (inQuotes && this.getOrNull(index + 1) == '"') {
                    current.append('"')
                    index++
                } else {
                    inQuotes = !inQuotes
                }
            }

            ',' if !inQuotes -> {
                result += current.toString()
                current.setLength(0)
            }

            else -> current.append(symbol)
        }
        index++
    }
    result += current.toString()
    return result
}