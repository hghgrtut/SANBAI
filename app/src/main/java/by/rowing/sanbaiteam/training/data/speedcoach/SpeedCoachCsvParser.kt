package by.rowing.sanbaiteam.training.data.speedcoach

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal object SpeedCoachCsvParser {

    fun parse(csvText: String): SpeedCoachCsvImport {
        val lines = normalizeLines(csvText)

        val serial = extractHeaderValue(lines, "Serial:")
        val sessionName = extractHeaderValue(lines, "Name:")
        val startTimeMillis = extractStartTime(lines)
        val intervals = parseIntervalSummaries(lines)
        if (intervals.isEmpty()) throw IllegalArgumentException("CSV does not contain interval summaries.")

        return SpeedCoachCsvImport(
            rawCsv = csvText,
            deviceSerial = serial?.normalizeSerial(),
            sessionName = sessionName?.takeIf { it.isNotBlank() },
            startTimeMillis = startTimeMillis,
            intervals = intervals
        )
    }

    private fun normalizeLines(csvText: String): List<String> = csvText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')

    private fun parseIntervalSummaries(lines: List<String>): List<SpeedCoachCsvInterval> {
        val startIndex = lines.indexOfFirst { it.trim().startsWith("Interval Summaries:", ignoreCase = true) }
        if (startIndex < 0) return emptyList()
        val headerLine = lines.getOrNull(startIndex + 2)?.takeIf { it.contains("Interval,") } ?: return emptyList()
        val unitLine = lines.getOrNull(startIndex + 3)?.takeIf { it.startsWith("(Interval)") } ?: return emptyList()
        if (unitLine.isBlank()) return emptyList()

        val headers = headerLine.split(',')
        val distanceIndex = headers.indexOf("Total Distance (GPS)")
        val timeIndex = headers.indexOf("Total Elapsed Time")
        val strokeIndex = headers.indexOf("Avg Stroke Rate")
        if (distanceIndex < 0 || timeIndex < 0 || strokeIndex < 0) return emptyList()

        val result = mutableListOf<SpeedCoachCsvInterval>()
        var lineIndex = startIndex + 4
        while (lineIndex < lines.size) {
            val line = lines[lineIndex].trim()
            if (line.isBlank() || line.startsWith("Per-Stroke Data", ignoreCase = true)) break
            val columns = line.split(',')
            if (columns.size > strokeIndex) {
                val distance = columns[distanceIndex].toDoubleOrNull()
                val time = RowingTimeFormat.parseDurationHhMmSsTenths(columns[timeIndex])
                val strokeRate = columns[strokeIndex].toDoubleOrNull()
                if (distance != null && time != null && strokeRate != null) {
                    result += SpeedCoachCsvInterval(
                        distanceMeters = distance.toInt(),
                        timeMillis = time,
                        strokeRate = strokeRate
                    )
                }
            }
            lineIndex++
        }
        return result
    }

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

    private fun extractHeaderValue(lines: List<String>, key: String): String? {
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
}

internal data class SpeedCoachCsvImport(
    val rawCsv: String,
    val deviceSerial: String?,
    val sessionName: String?,
    val startTimeMillis: Long?,
    val intervals: List<SpeedCoachCsvInterval>,
)

internal data class SpeedCoachCsvInterval(
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
)
