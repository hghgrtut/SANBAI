package by.rowing.sanbaiteam.training.data.speedcoach

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import java.util.Locale
import kotlin.math.roundToLong

internal object SpeedCoachCsvMerger {

    fun merge(csvTexts: List<String>): String {
        require(csvTexts.isNotEmpty()) { "At least one CSV is required." }
        if (csvTexts.size == 1) return csvTexts.first()

        val firstLines = normalizeLines(csvTexts.first())
        val intervalSection = findSection(firstLines, "Interval Summaries:")
            ?: return csvTexts.first()
        val perStrokeSection = findSection(firstLines, "Per-Stroke Data:")
        val sessionSection = findSection(firstLines, "Session Summary:")

        val mergedIntervalRows = mutableListOf<String>()
        val mergedPerStrokeRows = mutableListOf<String>()
        val intervalMetrics = mutableListOf<IntervalMetrics>()
        var intervalOffset = 0

        for (csvText in csvTexts) {
            val lines = normalizeLines(csvText)
            val fileIntervalSection = findSection(lines, "Interval Summaries:") ?: continue
            val fileHeaders = lines[fileIntervalSection.columnHeaderIndex].split(',')
            val dataRows = extractDataRows(lines, fileIntervalSection)

            dataRows.forEach { row ->
                val columns = row.split(',').toMutableList()
                intervalMetrics += parseIntervalMetrics(columns, fileHeaders)
                mergedIntervalRows += columns.joinToString(",")
            }

            findSection(lines, "Per-Stroke Data:")?.let { strokeSection ->
                extractDataRows(lines, strokeSection).forEach { row ->
                    val columns = row.split(',').toMutableList()
                    if (columns.isNotEmpty()) {
                        val intervalNumber = columns[0].trim().toIntOrNull()
                        if (intervalNumber != null) {
                            columns[0] = (intervalNumber + intervalOffset).toString()
                        }
                    }
                    mergedPerStrokeRows += columns.joinToString(",")
                }
            }

            intervalOffset += dataRows.size
        }

        val renumberedIntervals = mergedIntervalRows.mapIndexed { index, row ->
            val columns = row.split(',').toMutableList()
            if (columns.isNotEmpty()) {
                columns[0] = (index + 1).toString()
            }
            columns.joinToString(",")
        }

        val sessionSummaryRow = sessionSection?.let { section ->
            val templateHeaders = firstLines[section.columnHeaderIndex].split(',')
            val templateRow = extractDataRows(firstLines, section).firstOrNull()?.split(',')
            buildSessionSummaryRow(
                headers = templateHeaders,
                templateRow = templateRow,
                intervals = intervalMetrics,
            )
        }

        return rebuildCsv(
            firstLines = firstLines,
            sessionSection = sessionSection,
            intervalSection = intervalSection,
            perStrokeSection = perStrokeSection,
            sessionSummaryRow = sessionSummaryRow,
            mergedIntervalRows = renumberedIntervals,
            mergedPerStrokeRows = mergedPerStrokeRows,
        )
    }

    private fun rebuildCsv(
        firstLines: List<String>,
        sessionSection: CsvSection?,
        intervalSection: CsvSection,
        perStrokeSection: CsvSection?,
        sessionSummaryRow: String?,
        mergedIntervalRows: List<String>,
        mergedPerStrokeRows: List<String>,
    ): String {
        val result = mutableListOf<String>()

        if (sessionSection != null && sessionSummaryRow != null) {
            result += firstLines.subList(0, sessionSection.dataStartIndex)
            result += sessionSummaryRow
            result += firstLines.subList(sessionSection.dataEndIndex, intervalSection.headerIndex)
        } else {
            result += firstLines.subList(0, intervalSection.headerIndex)
        }

        result += firstLines[intervalSection.headerIndex]
        result += firstLines[intervalSection.columnHeaderIndex]
        result += firstLines[intervalSection.unitRowIndex]
        result += mergedIntervalRows

        if (perStrokeSection != null) {
            if (result.lastOrNull()?.isNotBlank() == true) {
                result += ""
            }
            result += firstLines[perStrokeSection.headerIndex]
            result += firstLines[perStrokeSection.columnHeaderIndex]
            result += firstLines[perStrokeSection.unitRowIndex]
            result += mergedPerStrokeRows
        }

        return result.joinToString("\n")
    }

    private fun buildSessionSummaryRow(
        headers: List<String>,
        templateRow: List<String>?,
        intervals: List<IntervalMetrics>,
    ): String {
        val cols = (templateRow ?: List(headers.size) { "---" }).toMutableList()
        while (cols.size < headers.size) {
            cols += "---"
        }

        fun setColumn(name: String, value: String) {
            val index = headers.indexOf(name)
            if (index in cols.indices) {
                cols[index] = value
            }
        }

        val totalIntervals = intervals.size
        val totalDistance = intervals.sumOf { it.distanceMeters }
        val totalTimeMillis = intervals.sumOf { it.timeMillis }
        val totalStrokes = intervals.mapNotNull { it.totalStrokes }.takeIf { it.isNotEmpty() }?.sum()

        setColumn("Total Intervals", totalIntervals.toString())
        setColumn("Total Distance (GPS)", String.format(Locale.US, "%.1f", totalDistance))

        if (totalTimeMillis > 0) {
            setColumn("Total Elapsed Time", RowingTimeFormat.formatDurationHhMmSsTenths(totalTimeMillis))
        }

        if (totalDistance > 0 && totalTimeMillis > 0) {
            val paceMillis = (totalTimeMillis.toDouble() * 500 / totalDistance).roundToLong()
            setColumn("Avg Split (GPS)", RowingTimeFormat.formatDurationHhMmSsTenths(paceMillis))
            val timeSeconds = totalTimeMillis / 1000.0
            setColumn("Avg Speed (GPS)", String.format(Locale.US, "%.2f", totalDistance / timeSeconds))
        }

        val avgStrokeRate = if (totalStrokes != null && totalStrokes > 0) {
            intervals.filter { it.totalStrokes != null && it.totalStrokes > 0 }
                .sumOf { it.strokeRate * it.totalStrokes!! } / totalStrokes
        } else {
            intervals.map { it.strokeRate }.average()
        }
        setColumn("Avg Stroke Rate", String.format(Locale.US, "%.1f", avgStrokeRate))

        if (totalStrokes != null && totalStrokes > 0) {
            setColumn("Total Strokes", totalStrokes.toString())
            setColumn("Distance/Stroke (GPS)", String.format(Locale.US, "%.2f", totalDistance / totalStrokes))
        }

        return cols.joinToString(",")
    }

    private fun parseIntervalMetrics(columns: List<String>, headers: List<String>): IntervalMetrics {
        fun value(name: String): String? {
            val index = headers.indexOf(name)
            return columns.getOrNull(index)?.trim()?.takeIf { it.isNotEmpty() }
        }

        return IntervalMetrics(
            distanceMeters = value("Total Distance (GPS)")?.toDoubleOrNull() ?: 0.0,
            timeMillis = value("Total Elapsed Time")?.let(RowingTimeFormat::parseDurationHhMmSsTenths) ?: 0L,
            strokeRate = value("Avg Stroke Rate")?.toDoubleOrNull() ?: 0.0,
            totalStrokes = value("Total Strokes")?.toIntOrNull(),
        )
    }

    private fun normalizeLines(csvText: String): List<String> = csvText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')

    private fun findSection(lines: List<String>, sectionTitle: String): CsvSection? {
        val headerIndex = lines.indexOfFirst { it.trim().startsWith(sectionTitle, ignoreCase = true) }
        if (headerIndex < 0) return null

        val columnHeaderIndex = headerIndex + 1
        val unitRowIndex = headerIndex + 2
        if (unitRowIndex >= lines.size) return null

        val unitLine = lines[unitRowIndex].trim()
        if (!unitLine.startsWith("(")) return null

        var dataEndIndex = unitRowIndex + 1
        while (dataEndIndex < lines.size) {
            val line = lines[dataEndIndex].trim()
            if (line.isBlank() || isSectionHeader(line)) break
            dataEndIndex++
        }

        return CsvSection(
            headerIndex = headerIndex,
            columnHeaderIndex = columnHeaderIndex,
            unitRowIndex = unitRowIndex,
            dataStartIndex = unitRowIndex + 1,
            dataEndIndex = dataEndIndex,
        )
    }

    private fun extractDataRows(lines: List<String>, section: CsvSection): List<String> =
        lines.subList(section.dataStartIndex, section.dataEndIndex)
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }

    private fun isSectionHeader(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.endsWith(':') && (
            trimmed.startsWith("Session Information", ignoreCase = true) ||
                trimmed.startsWith("Session Summary", ignoreCase = true) ||
                trimmed.startsWith("Interval Summaries", ignoreCase = true) ||
                trimmed.startsWith("Per-Stroke Data", ignoreCase = true)
            )
    }

    private data class CsvSection(
        val headerIndex: Int,
        val columnHeaderIndex: Int,
        val unitRowIndex: Int,
        val dataStartIndex: Int,
        val dataEndIndex: Int,
    )

    private data class IntervalMetrics(
        val distanceMeters: Double,
        val timeMillis: Long,
        val strokeRate: Double,
        val totalStrokes: Int?,
    )
}
