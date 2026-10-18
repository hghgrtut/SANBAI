package by.rowing.sanbaiteam.training.data.trainingimport

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("MaxLineLength")
internal class Rp3CsvParserTest {

    @Test
    fun parse_twoIntervals_buildsPiecesAndResetsDistance() {
        val import = Rp3CsvParser.parse(rp3Csv().encodeToByteArray(), null)

        assertEquals(TrainingImportSource.RP3_CSV, import.source)
        assertNull(import.deviceSerial)
        assertNull(import.sessionName)
        assertEquals(2, import.intervals.size)

        val first = import.intervals[0]
        assertEquals(22, first.distanceMeters)
        assertEquals(8_100L, first.timeMillis)
        assertEquals(21.0, first.strokeRate, DELTA)
        assertEquals(100, first.avgHeartRate)
        assertEquals(101, first.maxHeartRate)
        assertNull(first.recoveryHeartRate)

        val second = import.intervals[1]
        assertEquals(15, second.distanceMeters)
        assertEquals(95_400L, second.timeMillis)
        assertEquals(18.5, second.strokeRate, DELTA)
        assertEquals(121, second.avgHeartRate)
        assertEquals(122, second.maxHeartRate)
        assertNull(second.recoveryHeartRate)
    }

    @Test
    fun parse_keepsRawCsvTextUntouched() {
        val csv = rp3Csv()

        assertEquals(csv, Rp3CsvParser.parse(csv.encodeToByteArray(), null).csvText)
    }

    @Test
    fun parse_withDateInFileName_parsesUtcStartTime() {
        val import = Rp3CsvParser.parse(
            bytes = rp3Csv().encodeToByteArray(),
            fileName = "202609240115-rp3-row.csv"
        )

        val expected = LocalDateTime(2026, 9, 24, 1, 15)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
        assertEquals(expected, import.startTimeMillis)
    }

    @Test
    fun parse_withoutDateInFileName_hasNullStartTime() {
        assertNull(Rp3CsvParser.parse(rp3Csv().encodeToByteArray(), "rp3-row.csv").startTimeMillis)
        assertNull(Rp3CsvParser.parse(rp3Csv().encodeToByteArray(), null).startTimeMillis)
    }

    @Test
    fun matches_rp3Content_returnsTrue() {
        assertTrue(Rp3CsvParser.matches(rp3Csv().encodeToByteArray()))
    }

    @Test
    fun matches_otherCsvContent_returnsFalse() {
        assertFalse(Rp3CsvParser.matches("Session Information:,,,Device Information:".encodeToByteArray()))
        assertFalse(Rp3CsvParser.matches(ByteArray(0)))
    }

    @Test
    fun byCsvContent_rp3Archive_returnsRp3Source() {
        assertEquals(TrainingImportSource.RP3_CSV, TrainingImportSource.byCsvContent(rp3Csv()))
    }

    @Test
    fun parse_unsupportedContent_throws() {
        assertFailsWith<IllegalArgumentException> {
            Rp3CsvParser.parse("some,other,csv".encodeToByteArray(), null)
        }
    }

    @Test
    fun merge_singleCsv_returnsSameText() {
        val csv = mergeSampleCsv(rows = 2)

        assertEquals(csv, Rp3CsvParser.merge(listOf(csv)))
    }

    @Test
    fun merge_twoCsvs_keepsSingleHeaderAndRenumbersIntervals() {
        val first = mergeSampleCsv(rows = 2)
        val second = mergeSampleCsv(rows = 1)

        val merged = Rp3CsvParser.merge(listOf(first, second))
        val lines = merged.lines()

        assertEquals(HEADER, lines[0])
        assertEquals(4, lines.size)

        assertEquals("1", lines[1].splitRp3Row()[1])
        assertEquals("1", lines[2].splitRp3Row()[1])
        assertEquals("2", lines[3].splitRp3Row()[1])
        assertEquals("1,2,3", lines[1].splitRp3Row()[22])
    }

    private fun mergeSampleCsv(rows: Int): String = buildString {
        appendLine(HEADER)
        repeat(rows) { index ->
            val strokeNumber = index + 1
            appendLine(
                row(
                    intervalId = 555,
                    strokeNumber = strokeNumber,
                    strokeRate = 20.0,
                    time = strokeNumber.toDouble(),
                    distance = strokeNumber * 10.0,
                    pulse = 100
                )
            )
        }
    }.trimEnd()

    private fun rp3Csv(): String = buildString {
        appendLine(HEADER)
        appendLine(
            row(
                intervalId = 12624352,
                strokeNumber = 1,
                strokeRate = 20.0,
                time = 1.069,
                distance = 2.44,
                pulse = 99
            )
        )
        appendLine(
            row(
                intervalId = 12624352,
                strokeNumber = 2,
                strokeRate = 21.0,
                time = 4.601,
                distance = 12.21,
                pulse = 0,
                curve = "5,6,7,8"
            )
        )
        appendLine(
            row(
                intervalId = 12624352,
                strokeNumber = 3,
                strokeRate = 22.0,
                time = 8.100,
                distance = 22.0,
                pulse = 101
            )
        )
        appendLine(
            row(
                intervalId = 12624353,
                strokeNumber = 1,
                strokeRate = 18.0,
                time = 100.0,
                distance = 5.0,
                pulse = 120
            )
        )
        appendLine(
            row(
                intervalId = 12624353,
                strokeNumber = 2,
                strokeRate = 19.0,
                time = 103.5,
                distance = 15.0,
                pulse = 122
            )
        )
    }.trimEnd()

    private fun row(
        intervalId: Long,
        strokeNumber: Int,
        strokeRate: Double,
        time: Double,
        distance: Double,
        pulse: Int,
        curve: String = "1,2,3"
    ): String = listOf(
        "", intervalId.toString(), "", strokeNumber.toString(), "100.0", "100.0",
        strokeRate.toString(), time.toString(), "130.0", distance.toString(), "250.0",
        "200.0", "500.0", "500.0", pulse.toString(), "0", "500.0", "50.0", "0.4",
        "1.0", "2.0", "0", "\"$curve\"", strokeNumber.toString(), "100.0"
    ).joinToString(",")

    private companion object {
        const val DELTA = 0.0001
        val HEADER = listOf(
            "id", "workout_interval_id", "ref", "stroke_number", "power", "avg_power", "stroke_rate",
            "time", "stroke_length", "distance", "distance_per_stroke", "estimated_500m_time",
            "energy_per_stroke", "energy_sum", "pulse", "work_per_pulse", "peak_force", "peak_force_pos",
            "rel_peak_force_pos", "drive_time", "recover_time", "k", "curve_data",
            "stroke_number_in_interval", "avg_calculated_power"
        ).joinToString(",")
    }
}
