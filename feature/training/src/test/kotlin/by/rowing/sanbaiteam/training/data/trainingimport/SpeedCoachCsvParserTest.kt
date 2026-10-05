package by.rowing.sanbaiteam.training.data.trainingimport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@Suppress("MaxLineLength")
internal class SpeedCoachCsvParserTest {

    @Test
    fun parse_withHeartRateColumns_parsesAvgAndMax() {
        val import = SpeedCoachCsvParser.parse(csvWithHeartRate())

        assertEquals(2, import.intervals.size)
        assertEquals(158, import.intervals[0].avgHeartRate)
        assertEquals(152, import.intervals[1].avgHeartRate)
        assertEquals(162, import.intervals[0].maxHeartRate)
        assertEquals(151, import.intervals[1].maxHeartRate)
    }

    @Test
    fun parse_withoutHeartRateColumns_parsesIntervalsWithNullHeartRates() {
        val import = SpeedCoachCsvParser.parse(csvWithoutHeartRate())

        assertEquals(1, import.intervals.size)
        assertEquals(500, import.intervals[0].distanceMeters)
        assertNull(import.intervals[0].avgHeartRate)
        assertNull(import.intervals[0].maxHeartRate)
    }

    @Test
    fun parse_fullSpeedCoachCsv_parsesAllFourIntervalsWithHeartRate() {
        val import = SpeedCoachCsvParser.parse(realSpeedCoachCsv())

        assertEquals(4, import.intervals.size)

        assertEquals("2693559", import.deviceSerial)

        assertEquals(2857, import.intervals[0].distanceMeters)
        assertEquals(2983, import.intervals[1].distanceMeters)
        assertEquals(3739, import.intervals[2].distanceMeters)
        assertEquals(3419, import.intervals[3].distanceMeters)

        assertEquals(155, import.intervals[0].avgHeartRate)
        assertEquals(155, import.intervals[1].avgHeartRate)
        assertEquals(153, import.intervals[2].avgHeartRate)
        assertEquals(155, import.intervals[3].avgHeartRate)

        assertEquals(161, import.intervals[0].maxHeartRate)
        assertEquals(160, import.intervals[1].maxHeartRate)
        assertEquals(160, import.intervals[2].maxHeartRate)
        assertEquals(164, import.intervals[3].maxHeartRate)

        import.intervals.forEach { interval ->
            assertNotNull(
                "avgHeartRate should not be null for ${interval.distanceMeters}m interval",
                interval.avgHeartRate
            )
            assertNotNull(
                "maxHeartRate should not be null for ${interval.distanceMeters}m interval",
                interval.maxHeartRate
            )
        }
    }

    private fun csvWithHeartRate(): String = buildString {
        appendLine("Session Information:,,,Device Information:")
        appendLine("Name:,Sample,,,Serial:,2693559")
        appendLine()
        appendLine("Session Summary:")
        appendLine("Total Intervals,Total Distance (GPS),Total Elapsed Time,Avg Stroke Rate")
        appendLine("(Intervals),(Meters),(HH:MM:SS.tenths),(SPM)")
        appendLine("2,1000.0,00:06:00.0,30.0")
        appendLine()
        appendLine("Interval Summaries:")
        appendLine()
        appendLine("Interval,Total Distance (GPS),Total Elapsed Time,Avg Stroke Rate,Avg Heart Rate")
        appendLine("(Interval),(Meters),(HH:MM:SS.tenths),(SPM),(BPM)")
        appendLine("1,500.0,00:03:00.0,30.0,158.4")
        appendLine("2,500.0,00:03:00.0,28.0,152.0")
        appendLine()
        appendLine("Per-Stroke Data:")
        appendLine()
        appendLine("Interval,Elapsed Time,Heart Rate")
        appendLine("(Interval),(HH:MM:SS.tenths),(BPM)")
        appendLine("1,00:00:01.0,155")
        appendLine("1,00:00:02.0,162")
        appendLine("1,00:00:03.0,159")
        appendLine("2,00:00:01.0,148")
        appendLine("2,00:00:02.0,151")
    }.trimEnd()

    private fun csvWithoutHeartRate(): String = buildString {
        appendLine("Interval Summaries:")
        appendLine()
        appendLine("Interval,Total Distance (GPS),Total Elapsed Time,Avg Stroke Rate")
        appendLine("(Interval),(Meters),(HH:MM:SS.tenths),(SPM)")
        appendLine("1,500.0,00:03:00.0,30.0")
        appendLine()
        appendLine("Per-Stroke Data:")
        appendLine()
        appendLine("Interval,Elapsed Time")
        appendLine("(Interval),(HH:MM:SS.tenths)")
        appendLine("1,00:00:01.0")
    }.trimEnd()

    private fun realSpeedCoachCsv(): String = buildString {
        appendLine("Session Information:,,,Device Information:")
        appendLine("Name:,Захаров Игорь Викторович,,,Start Time:,07/29/2026 08:53AM")
        appendLine("Serial:,2693559,,,Model No.,SpeedCoach GPS PL")
        appendLine("Firmware Version:,3.8,,,Software Version,2.8.10")
        appendLine()
        appendLine("Session Summary:")
        appendLine("Total Intervals,Total Distance (GPS),Total Elapsed Time,Avg Stroke Rate")
        appendLine("(Intervals),(Meters),(HH:MM:SS.tenths),(SPM)")
        appendLine("4,12997.8,01:00:00.0,22.0")
        appendLine()
        appendLine("Interval Summaries:")
        appendLine()
        appendLine("Interval,Total Distance (GPS),Total Distance (IMP),Total Elapsed Time,Avg Split (GPS),Avg Speed (GPS),Avg Split (IMP),Avg Speed (IMP),Avg Stroke Rate,Total Strokes,Distance/Stroke (GPS),Distance/Stroke (IMP),Avg Heart Rate,Avg Power,Avg Catch,Avg Slip,Avg Finish,Avg Wash,Avg Force Avg,Avg Work,Avg Force Max,Avg Max Force Angle,Start GPS Lat.,Start GPS Lon.")
        appendLine("(Interval),(Meters),(Meters),(HH:MM:SS.tenths),(MM:SS),(M/S),(MM:SS),(M/S),(SPM),(Strokes),(M),(M),(BPM),(Watts),(Degrees),(Degrees),(Degrees),(Degrees),(Newtons),(Joules),(Newtons),(Degrees),(Lat),(Lon)")
        appendLine("1,2857.4,0.0,00:15:00.0,00:02:37.7,3.17,00:00:00.0,0.00,21.0,319,9.0,0.0,155.0,130.7,35.0,23.7,52.9,1.7,2.63,0.14,667.1,86.3,52.7314850,27.4005067")
        appendLine("2,2982.7,0.0,00:15:00.0,00:02:32.2,3.28,00:00:00.0,0.00,22.0,329,9.1,0.0,155.0,145.6,34.5,24.1,53.7,1.6,2.72,0.13,702.4,88.2,52.7331517,27.3982117")
        appendLine("3,3738.8,0.0,00:15:00.0,00:02:03.0,4.06,00:00:00.0,0.00,22.0,333,11.2,0.0,153.0,152.3,36.2,22.8,54.1,1.5,2.81,0.15,718.9,89.1,52.7332400,27.3983467")
        appendLine("4,3419.0,0.0,00:15:00.0,00:02:12.6,3.73,00:00:00.0,0.00,22.0,334,10.2,0.0,155.0,138.9,35.8,23.2,53.4,1.6,2.58,0.14,689.2,87.5,52.7331867,27.3983300")
        appendLine()
        appendLine("Per-Stroke Data:")
        appendLine()
        appendLine("Interval,Distance (GPS),Distance (IMP),Elapsed Time,Split (GPS),Speed (GPS),Split (IMP),Speed (IMP),Stroke Rate,Total Strokes,Distance/Stroke (GPS),Distance/Stroke (IMP),Heart Rate,Power,Catch,Slip,Finish,Wash,Force Avg,Work,Force Max,Max Force Angle,GPS Lat.,GPS Lon.")
        appendLine("(Interval),(Meters),(Meters),(HH:MM:SS.tenths),(MM:SS),(M/S),(MM:SS),(M/S),(SPM),(Strokes),(M),(M),(BPM),(Watts),(Degrees),(Degrees),(Degrees),(Degrees),(Newtons),(Joules),(Newtons),(Degrees),(Lat),(Lon)")
        // Interval 1 strokes — real data with Heart Rate column at index 12
        appendLine("1,8.6,0.0,00:00:00.3,00:02:20.6,3.55,00:00:00.0,0.00,37.0,1,8.6,0.0,121,0.0,30.0,36.5,55.9,4.8,0.00,0.00,439.6,82.5,52.7314850,27.4005067")
        appendLine("1,15.9,0.0,00:00:01.5,00:02:26.5,3.40,00:00:00.0,0.00,37.0,2,7.7,0.0,126,0.0,31.3,34.8,64.6,0.6,0.00,0.00,420.0,82.8,52.7314850,27.4005067")
        appendLine("1,24.0,0.0,00:00:02.9,00:02:25.2,3.44,00:00:00.0,0.00,35.0,3,8.1,0.0,130,0.0,27.2,41.0,60.2,2.4,0.00,0.00,510.0,84.6,52.7314850,27.4005067")
        appendLine("1,866.6,0.0,00:04:33.1,00:02:36.6,3.19,00:00:00.0,0.00,22.0,104,8.3,0.0,155,0.0,45.9,14.9,50.5,0.7,4.43,0.16,758.0,91.4,52.7324750,27.3992017")
        appendLine("1,1615.7,0.0,00:08:43.0,00:02:39.7,3.13,00:00:00.0,0.00,21.0,192,8.4,0.0,161,0.0,45.7,12.9,49.2,1.0,4.98,0.23,850.0,92.3,52.7331500,27.3982417")
        appendLine("1,2853.7,0.0,00:14:58.9,00:02:37.6,3.17,00:00:00.0,0.00,21.0,319,9.0,0.0,159,0.0,43.2,12.3,50.8,1.1,4.39,0.16,742.0,89.5,52.7332400,27.3983467")
        // Interval 2 strokes
        appendLine("2,8.3,0.0,00:00:00.3,00:02:27.3,3.38,00:00:00.0,0.00,32.0,1,8.3,0.0,142,0.0,28.2,32.4,55.2,0.2,0.00,0.00,430.0,82.8,52.7331517,27.3982117")
        appendLine("2,17.8,0.0,00:00:01.8,00:02:40.3,3.12,00:00:00.0,0.00,34.0,2,9.5,0.0,143,0.0,28.8,35.2,64.6,1.3,0.00,0.00,494.0,85.0,52.7331517,27.3982117")
        appendLine("2,1627.9,0.0,00:08:38.9,00:02:35.5,3.21,00:00:00.0,0.00,22.0,185,8.8,0.0,160,0.0,44.2,14.3,52.4,0.7,4.75,0.20,812.0,91.4,52.7331517,27.3982117")
        appendLine("2,2978.3,0.0,00:14:58.8,00:02:32.0,3.28,00:00:00.0,0.00,22.0,329,9.1,0.0,148,0.0,39.9,14.7,50.3,1.4,4.48,0.15,749.0,88.5,52.7332400,27.3983467")
        // Interval 3 strokes
        appendLine("3,9.0,0.0,00:00:00.4,00:02:38.6,3.14,00:00:00.0,0.00,33.0,1,9.0,0.0,148,0.0,27.0,39.5,63.9,0.6,0.00,0.00,524.0,86.6,52.7332400,27.3983467")
        appendLine("3,19.9,0.0,00:00:02.2,00:02:37.9,3.15,00:00:00.0,0.00,34.0,2,10.9,0.0,148,0.0,27.9,33.5,62.9,1.3,0.00,0.00,544.0,85.0,52.7332400,27.3983467")
        appendLine("3,1841.6,0.0,00:09:23.6,00:02:01.9,4.10,00:00:00.0,0.00,22.0,172,10.7,0.0,160,0.0,35.0,18.4,52.1,1.0,4.10,0.19,752.0,90.4,52.7331867,27.3983300")
        appendLine("3,3734.7,0.0,00:14:58.8,00:02:02.9,4.07,00:00:00.0,0.00,22.0,333,11.2,0.0,145,0.0,40.4,13.2,49.7,1.6,4.07,0.16,683.0,86.4,52.7331867,27.3983300")
        // Interval 4 strokes
        appendLine("4,8.4,0.0,00:00:00.2,00:02:22.1,3.51,00:00:00.0,0.00,32.0,1,8.4,0.0,146,0.0,22.0,54.4,49.1,3.4,0.00,0.00,430.0,81.5,52.7331867,27.3983300")
        appendLine("4,18.8,0.0,00:00:01.7,00:02:38.8,3.14,00:00:00.0,0.00,34.0,2,10.4,0.0,148,0.0,25.1,38.9,65.3,1.6,0.00,0.00,556.0,86.6,52.7331867,27.3983300")
        appendLine("4,1588.0,0.0,00:08:04.6,00:02:11.8,3.75,00:00:00.0,0.00,22.0,169,9.4,0.0,164,0.0,39.9,19.5,50.5,0.9,4.17,0.17,752.0,90.3,52.7331867,27.3983300")
        appendLine("4,3413.9,0.0,00:14:58.7,00:02:12.5,3.73,00:00:00.0,0.00,22.0,334,10.2,0.0,155,0.0,37.9,18.8,52.7,1.1,4.26,0.15,681.0,86.6,52.7331867,27.3983300")
    }.trimEnd()
}