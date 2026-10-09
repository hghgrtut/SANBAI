package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

internal object SpeedCoachCsvParser : TrainingImportParser {

    override val source = TrainingImportSource.SPEED_COACH

    private const val INTERVAL_SUMMARIES_MARKER = "Interval Summaries:"

    override fun matches(bytes: ByteArray): Boolean =
        !Concept2FitParser.matches(bytes) &&
                String(bytes, Charsets.UTF_8).contains(INTERVAL_SUMMARIES_MARKER)

    override fun parse(bytes: ByteArray): ParsedTrainingImport = parse(String(bytes, Charsets.UTF_8))

    override fun merge(csvTexts: List<String>): String = SpeedCoachCsvMerger.merge(csvTexts)

    fun parse(csvText: String): ParsedTrainingImport {
        val lines = normalizeLines(csvText)

        val serial = extractHeaderValue(lines, "Serial:")
        val sessionName = extractHeaderValue(lines, "Name:")
        val startTimeMillis = extractStartTime(lines)
        val intervals = parseIntervalSummaries(lines)
        if (intervals.isEmpty()) throw IllegalArgumentException("CSV does not contain interval summaries.")
        val perStrokeMaxHeartRates = parsePerStrokeMaxHeartRates(lines)

        return ParsedTrainingImport(
            source = source,
            csvText = csvText,
            deviceSerial = serial?.normalizeSerial(),
            sessionName = sessionName?.takeIf { it.isNotBlank() },
            startTimeMillis = startTimeMillis,
            intervals = intervals.mapIndexed { index, interval ->
                interval.copy(maxHeartRate = perStrokeMaxHeartRates[index + 1])
            }
        )
    }

    private fun normalizeLines(csvText: String): List<String> = csvText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')

    private fun parseIntervalSummaries(lines: List<String>): List<TrainingImportInterval> {
        val startIndex = lines.indexOfFirst { it.trim().startsWith(INTERVAL_SUMMARIES_MARKER, ignoreCase = true) }
        if (startIndex < 0) return emptyList()
        val headerLine =
            lines.getOrNull(startIndex + 2)?.takeIf { it.contains("Interval,") } ?: return emptyList()
        val unitLine =
            lines.getOrNull(startIndex + 3)?.takeIf { it.startsWith("(Interval)") } ?: return emptyList()
        if (unitLine.isBlank()) return emptyList()

        val headers = headerLine.splitToColumns()
        val distanceIndex = headers.indexOf("Total Distance (GPS)")
        val timeIndex = headers.indexOf("Total Elapsed Time")
        val strokeIndex = headers.indexOf("Avg Stroke Rate")
        val heartRateIndex = headers.indexOf("Avg Heart Rate")
        if (distanceIndex < 0 || timeIndex < 0 || strokeIndex < 0) return emptyList()

        val result = mutableListOf<TrainingImportInterval>()
        var lineIndex = startIndex + 4
        while (lineIndex < lines.size) {
            val line = lines[lineIndex].trim()
            if (line.isBlank() || line.isPerStrokeSectionHeader()) break
            val columns = line.splitToColumns()
            if (columns.size > strokeIndex) {
                val time = RowingTimeFormat.parseDurationHhMmSsTenths(columns[timeIndex])
                val strokeRate = columns[strokeIndex].toDoubleOrNull()
                val avgHeartRate = columns.parseDoubleToInt(columnIndex = heartRateIndex).takeIf { heartRateIndex >= 0 }
                if (time != null && strokeRate != null) {
                    result += TrainingImportInterval(
                        distanceMeters = columns.parseDoubleToInt(columnIndex = distanceIndex) ?: 0,
                        timeMillis = time,
                        strokeRate = strokeRate,
                        avgHeartRate = avgHeartRate
                    )
                }
            }
            lineIndex++
        }
        return result
    }

    private fun parsePerStrokeMaxHeartRates(lines: List<String>): Map<Int, Int> {
        val sectionIndex = lines.indexOfFirst { it.trim().isPerStrokeSectionHeader() }
        if (sectionIndex < 0) return emptyMap()

        var columnHeaderIndex = -1
        for (index in sectionIndex + 1 until lines.size) {
            val line = lines[index].trim()
            if (line.isEmpty() || line.startsWith("(")) continue
            if (line.contains("Interval")) {
                columnHeaderIndex = index
                break
            }
        }
        if (columnHeaderIndex < 0) return emptyMap()

        val headers = lines[columnHeaderIndex].splitToColumns()
        val heartRateIndex = headers.indexOf("Heart Rate")
        if (heartRateIndex < 0) return emptyMap()

        val result = mutableMapOf<Int, Int>()
        var lineIndex = columnHeaderIndex + 1
        if (lines.getOrNull(lineIndex)?.trim()?.startsWith("(") == true) lineIndex++
        while (lineIndex < lines.size) {
            val line = lines[lineIndex].trim()
            if (line.isBlank()) break
            val columns = line.splitToColumns()
            val intervalNumber = columns.getOrNull(0)?.trim()?.toIntOrNull()
            val heartRate = columns.parseDoubleToInt(columnIndex = heartRateIndex)
            if (intervalNumber != null && heartRate != null) {
                result[intervalNumber] = maxOf(result[intervalNumber] ?: 0, heartRate)
            }
            lineIndex++
        }
        return result
    }

    private fun List<String>.parseDoubleToInt(columnIndex: Int): Int? =
        getOrNull(columnIndex)?.trim()?.toDoubleOrNull()?.roundToInt()

    private fun extractStartTime(lines: List<String>): Long? {
        val value = extractHeaderValue(lines, "Start Time:") ?: return null
        val formats = listOf("MM/dd/yyyy hh:mma", "MM/dd/yyyy HH:mm:ss")
        val normalized = value.replace(Regex("\\s+"), " ").trim()
        val compact = normalized.replace(" ", "")
        for (pattern in formats) {
            val parser = SimpleDateFormat(pattern, Locale.US)
            parser.isLenient = false
            val parsed = runCatching {
                parser.parse(if (pattern.contains("a")) compact else normalized)
            }.getOrNull()
            if (parsed != null) return parsed.time
        }
        return null
    }

    private fun extractHeaderValue(
        lines: List<String>,
        key: String
    ): String? {
        for (line in lines) {
            val raw = line.trim()
            val keyIndex = raw.indexOf(key, ignoreCase = true)
            if (keyIndex >= 0) {
                var suffix = raw.substring(keyIndex + key.length)
                var nextCellValue = ""
                while (nextCellValue.isBlank() && suffix.isNotBlank()) {
                    nextCellValue = suffix.substringBefore(',')
                    suffix = suffix.substringAfter(',')
                }
                return nextCellValue.trim()
            }
        }
        return null
    }

    private fun String.normalizeSerial(): String = filter { it.isLetterOrDigit() }

    private fun String.splitToColumns(): List<String> = split(',')

    private fun String.isPerStrokeSectionHeader(): Boolean = startsWith(
        prefix = "Per-Stroke Data",
        ignoreCase = true
    )
}
