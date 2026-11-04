package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Regression tests for Concept2 PM5 `.fit` files.
 *
 * A workout without rest laps (just ride, single time/distance) is imported as one continuous
 * piece, while an interval workout keeps one piece per active lap.
 */
internal class Concept2FitParserTest {

    @Test
    fun parse_activeLap_becomesSingleInterval() {
        val parsed = Concept2FitParser.parse(intervalFitSample(), fileName = null)

        assertEquals(TrainingImportSource.CONCEPT2_FIT, parsed.source)
        assertEquals(TrainingPieceType.CONCEPT_ROW_ERG, parsed.trainingType)
        assertEquals(1, parsed.intervals.size)

        val interval = parsed.intervals.single()
        assertEquals(4523, interval.distanceMeters)
        assertEquals(900_000L, interval.timeMillis)
        assertEquals(25.0, interval.strokeRate)
        assertEquals(145, interval.avgHeartRate)
        assertEquals(150, interval.maxHeartRate)
        assertEquals(150, interval.recoveryHeartRate)
    }

    @Test
    fun parse_sessionStart_isReportedAsWorkoutStart() {
        val parsed = Concept2FitParser.parse(intervalFitSample(), fileName = null)

        assertEquals((FIT_TEST_START_TS + FIT_EPOCH_OFFSET_SECONDS) * 1_000L, parsed.startTimeMillis)
    }

    @Test
    fun parse_rowErgSingleDistance_isMergedIntoOnePiece() {
        val parsed = Concept2FitParser.parse(rowErgSingleDistanceSample(), fileName = null)

        assertEquals(TrainingPieceType.CONCEPT_ROW_ERG, parsed.trainingType)
        val interval = parsed.intervals.single()
        assertEquals(420_000L, interval.timeMillis)
        assertEquals(2264, interval.distanceMeters)
        assertEquals(27.5, interval.strokeRate)
        assertNull(interval.recoveryHeartRate)
    }

    @Test
    fun parse_bikeErgJustRide_isMergedIntoOnePiece() {
        val parsed = Concept2FitParser.parse(bikeErgJustRideSample(), fileName = null)

        assertEquals(TrainingImportSource.CONCEPT2_FIT, parsed.source)
        assertEquals(TrainingPieceType.CONCEPT_BIKE_ERG, parsed.trainingType)
        val interval = parsed.intervals.single()
        assertEquals(900_000L, interval.timeMillis)
        assertEquals(8060, interval.distanceMeters)
        assertEquals(65.0, interval.strokeRate)
        assertEquals(142, interval.avgHeartRate)
        assertEquals(145, interval.maxHeartRate)
        assertNull(interval.recoveryHeartRate)
    }

    @Test
    fun parse_intervalWorkout_keepsActiveLapsAndRecoveryHeartRate() {
        val parsed = Concept2FitParser.parse(intervalWithRestSample(), fileName = null)

        assertEquals(2, parsed.intervals.size)
        val first = parsed.intervals[0]
        assertEquals(300_000L, first.timeMillis)
        assertEquals(1000, first.distanceMeters)
        assertEquals(145, first.avgHeartRate)
        assertEquals(102, first.recoveryHeartRate)

        val second = parsed.intervals[1]
        assertEquals(300_000L, second.timeMillis)
        assertEquals(165, second.avgHeartRate)
        assertEquals(92, second.recoveryHeartRate)
    }

    @Test
    fun matches_recognisesRowingAndBikeErg() {
        assertTrue(Concept2FitParser.matches(intervalFitSample()))
        assertTrue(Concept2FitParser.matches(rowErgSingleDistanceSample()))
        assertTrue(Concept2FitParser.matches(bikeErgJustRideSample()))
        assertFalse(Concept2FitParser.matches(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun matches_rejectsIndoorCyclingOfOtherManufacturers() {
        val foreignBike = concept2FitFile(
            laps = listOf(
                FitTestLap(
                    startOffsetSeconds = 0,
                    elapsedSeconds = 300,
                    distanceMeters = 2648.0,
                )
            ),
            sport = 2,
            subSport = 6,
            manufacturer = 260,
        )

        assertFalse(Concept2FitParser.matches(foreignBike))
    }

    private fun intervalFitSample(): ByteArray = concept2FitFile(
        laps = listOf(
            FitTestLap(
                startOffsetSeconds = 0,
                elapsedSeconds = 900,
                distanceMeters = 4522.99,
                intensity = 0,
            ),
            FitTestLap(
                startOffsetSeconds = 900,
                elapsedSeconds = 360,
                distanceMeters = 331.39,
                intensity = 1,
            ),
        ),
        records = listOf(
            FitTestRecord(
                offsetSeconds = 10,
                heartRate = 140,
                cadence = 24,
                distanceMeters = 50.0,
                speedMetersPerSecond = 3.6,
            ),
            FitTestRecord(
                offsetSeconds = 20,
                heartRate = 150,
                cadence = 26,
                distanceMeters = 100.0,
                speedMetersPerSecond = 3.7,
            ),
        ),
    )

    private fun rowErgSingleDistanceSample(): ByteArray = concept2FitFile(
        laps = listOf(
            FitTestLap(0, 120, 650.0),
            FitTestLap(120, 120, 600.0),
            FitTestLap(240, 120, 550.0),
            FitTestLap(360, 60, 464.0),
        ),
        records = listOf(
            FitTestRecord(
                offsetSeconds = 10,
                heartRate = 130,
                cadence = 28,
                speedMetersPerSecond = 5.4,
            ),
            FitTestRecord(
                offsetSeconds = 130,
                heartRate = 140,
                cadence = 28,
                speedMetersPerSecond = 5.0,
            ),
            FitTestRecord(
                offsetSeconds = 250,
                heartRate = 150,
                cadence = 27,
                speedMetersPerSecond = 4.6,
            ),
            FitTestRecord(
                offsetSeconds = 370,
                heartRate = 160,
                cadence = 27,
                speedMetersPerSecond = 7.7,
            ),
        ),
    )

    private fun bikeErgJustRideSample(): ByteArray = concept2FitFile(
        laps = listOf(
            FitTestLap(0, 300, 2648.0),
            FitTestLap(300, 300, 2705.0),
            FitTestLap(600, 300, 2707.0),
        ),
        records = listOf(
            FitTestRecord(
                offsetSeconds = 10,
                heartRate = 140,
                cadence = 64,
                speedMetersPerSecond = 8.83,
            ),
            FitTestRecord(
                offsetSeconds = 310,
                heartRate = 142,
                cadence = 65,
                speedMetersPerSecond = 9.02,
            ),
            FitTestRecord(
                offsetSeconds = 610,
                heartRate = 145,
                cadence = 66,
                speedMetersPerSecond = 9.02,
            ),
        ),
        sport = 2,
        subSport = 6,
    )

    private fun intervalWithRestSample(): ByteArray = concept2FitFile(
        laps = listOf(
            FitTestLap(0, 300, 1000.0, intensity = 0),
            FitTestLap(300, 120, 0.0, intensity = 1),
            FitTestLap(420, 300, 1000.0, intensity = 0),
            FitTestLap(720, 120, 0.0, intensity = 1),
        ),
        records = listOf(
            FitTestRecord(
                offsetSeconds = 10,
                heartRate = 140,
                cadence = 30,
                speedMetersPerSecond = 4.0,
            ),
            FitTestRecord(
                offsetSeconds = 20,
                heartRate = 150,
                cadence = 30,
                speedMetersPerSecond = 4.0,
            ),
            FitTestRecord(
                offsetSeconds = 310,
                heartRate = 120,
            ),
            FitTestRecord(
                offsetSeconds = 400,
                heartRate = 100,
            ),
            FitTestRecord(
                offsetSeconds = 430,
                heartRate = 160,
                cadence = 32,
            ),
            FitTestRecord(
                offsetSeconds = 440,
                heartRate = 170,
                cadence = 32,
            ),
            FitTestRecord(
                offsetSeconds = 730,
                heartRate = 110,
            ),
            FitTestRecord(
                offsetSeconds = 820,
                heartRate = 90,
            ),
        ),
    )
}