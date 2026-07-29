package by.rowing.sanbaiteam.training.data.speedcoach

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpeedCoachCsvParserTest {

    @Test
    fun parse_extractsSerialAndIntervals() {
        val csv = """
            Session Information:,,,,Device Information:
            Name:,3x1000M/9:00,,,Name:,SpdCoach 2693559
            Start Time:,07/27/2026 08:48:14,,,Model:,SpeedCoach GPS Pro
            Type:,Interval,,,Serial:,2693559
            
            Interval Summaries:
            Interval,Total Distance (GPS),Total Distance (IMP),Total Elapsed Time,Avg Split (GPS),Avg Speed (GPS),Avg Split (IMP),Avg Speed (IMP),Avg Stroke Rate
            (Interval),(Meters),(Meters),(HH:MM:SS.tenths),(/500),(M/S),(/500),(M/S),(SPM)
            1,1000.3,0.0,00:03:24.0,00:01:42.0,4.90,00:00:00.0,0.00,34.0
            2,1000.7,0.0,00:03:27.3,00:01:43.7,4.82,00:00:00.0,0.00,33.0
            3,1000.4,0.0,00:03:27.9,00:01:43.9,4.81,00:00:00.0,0.00,34.0
            
            Per-Stroke Data:
        """.trimIndent()

        val parsed = SpeedCoachCsvParser.parse(csv)

        assertEquals("2693559", parsed.deviceSerial)
        assertEquals("3x1000M/9:00", parsed.sessionName)
        assertEquals(3, parsed.intervals.size)
        assertEquals(1000, parsed.intervals[0].distanceMeters)
        assertEquals(RowingTimeFormat.parseDurationHhMmSsTenths("00:03:27.3"), parsed.intervals[1].timeMillis)
        assertEquals(34.0, parsed.intervals[2].strokeRate, 0.0)
        assertNotNull(parsed.startTimeMillis)

        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = parsed.startTimeMillis!! }
        assertEquals(2026, calendar.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.JULY, calendar.get(java.util.Calendar.MONTH))
        assertEquals(27, calendar.get(java.util.Calendar.DAY_OF_MONTH))
    }
}
