package by.rowing.sanbaiteam.training.data.speedcoach

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedCoachCsvMergerTest {

    @Test
    fun merge_singleCsv_returnsSameText() {
        val csv = sampleCsv(
            sessionIntervals = 2,
            intervalRows = listOf(
                "1,500.0,00:03:00.0,30.0,90",
                "2,500.0,00:03:00.0,30.0,90",
            ),
            perStrokeRows = listOf("1,00:00:01.0", "2,00:00:01.0"),
        )

        assertEquals(csv, SpeedCoachCsvMerger.merge(listOf(csv)))
    }

    @Test
    fun merge_twoCsvs_renumbersIntervalsAndShiftsPerStroke() {
        val first = sampleCsv(
            sessionIntervals = 2,
            intervalRows = listOf(
                "1,500.0,00:03:00.0,30.0,90",
                "2,500.0,00:03:00.0,30.0,90",
            ),
            perStrokeRows = listOf("1,00:00:01.0", "2,00:00:01.0"),
        )
        val second = sampleCsv(
            sessionIntervals = 1,
            intervalRows = listOf("1,500.0,00:03:00.0,28.0,84"),
            perStrokeRows = listOf("1,00:00:02.0"),
        )

        val merged = SpeedCoachCsvMerger.merge(listOf(first, second))
        val lines = merged.lines()

        val intervalStart = lines.indexOfFirst { it.startsWith("Interval Summaries:") } + 3
        assertEquals("1,500.0,00:03:00.0,30.0,90", lines[intervalStart].trim())
        assertEquals("2,500.0,00:03:00.0,30.0,90", lines[intervalStart + 1].trim())
        assertEquals("3,500.0,00:03:00.0,28.0,84", lines[intervalStart + 2].trim())

        val perStrokeStart = lines.indexOfFirst { it.startsWith("Per-Stroke Data:") } + 3
        assertEquals("1,00:00:01.0", lines[perStrokeStart].trim())
        assertEquals("2,00:00:01.0", lines[perStrokeStart + 1].trim())
        assertEquals("3,00:00:02.0", lines[perStrokeStart + 2].trim())
    }

    @Test
    fun merge_twoCsvs_recalculatesSessionSummary() {
        val first = sampleCsv(
            sessionIntervals = 2,
            intervalRows = listOf(
                "1,500.0,00:03:00.0,30.0,90",
                "2,500.0,00:03:00.0,30.0,90",
            ),
            perStrokeRows = emptyList(),
        )
        val second = sampleCsv(
            sessionIntervals = 1,
            intervalRows = listOf("1,500.0,00:03:00.0,28.0,84"),
            perStrokeRows = emptyList(),
        )

        val merged = SpeedCoachCsvMerger.merge(listOf(first, second))
        val lines = merged.lines()
        val summaryStart = lines.indexOfFirst { it.startsWith("Session Summary:") } + 3
        val summaryColumns = lines[summaryStart].split(',')

        assertEquals("3", summaryColumns[0])
        assertEquals("1500.0", summaryColumns[1])
        assertEquals(
            RowingTimeFormat.formatDurationHhMmSsTenths(
                RowingTimeFormat.parseDurationHhMmSsTenths("00:09:00.0")!!
            ),
            summaryColumns[2]
        )
        assertTrue(summaryColumns[5].toDouble() > 28.0)
        assertEquals("264", summaryColumns[6])
    }

    private fun sampleCsv(
        sessionIntervals: Int,
        intervalRows: List<String>,
        perStrokeRows: List<String>,
    ): String = buildString {
        appendLine("Session Information:,,,,Device Information:")
        appendLine("Name:,Sample,,,Serial:,2693559")
        appendLine()
        appendLine("Session Summary:")
        appendLine("Total Intervals,Total Distance (GPS),Total Elapsed Time,Avg Split (GPS),Avg Speed (GPS),Avg Stroke Rate,Total Strokes,Distance/Stroke (GPS),Start GPS Lat/Lon")
        appendLine("(Intervals),(Meters),(HH:MM:SS.tenths),(/500),(M/S),(SPM),(Strokes),(Meters/Stroke),(Lat/Lon)")
        appendLine("$sessionIntervals,1000.0,00:06:00.0,00:03:00.0,2.78,30.0,180,5.56,53.9/27.5")
        appendLine()
        appendLine("Interval Summaries:")
        appendLine("Interval,Total Distance (GPS),Total Elapsed Time,Avg Stroke Rate,Total Strokes")
        appendLine("(Interval),(Meters),(HH:MM:SS.tenths),(SPM),(Strokes)")
        intervalRows.forEach { appendLine(it) }
        appendLine()
        appendLine("Per-Stroke Data:")
        appendLine("Interval,Elapsed Time")
        appendLine("(Interval),(HH:MM:SS.tenths)")
        perStrokeRows.forEach { appendLine(it) }
    }.trimEnd()
}
