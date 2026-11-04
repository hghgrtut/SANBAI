package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.trainingimport.FitTestLap
import by.rowing.sanbaiteam.training.data.trainingimport.FitTestRecord
import by.rowing.sanbaiteam.training.data.trainingimport.concept2FitFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class TrainingChartReaderConcept2Test {

    private val reader = TrainingChartReader()

    @Test
    fun `continuous workout chart contains records of every lap`() {
        val chart = assertNotNull(
            reader.read(
                sourceFileBytes = rowErgSingleDistance(),
                sourceFileIndex = 0,
                sourceIntervalIndex = 0,
                pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
            )
        )

        assertEquals(420_000L, chart.durationMillis)
        assertEquals(4, chart.strokeRate.size)
        assertEquals(4, chart.heartRate.size)
        assertEquals(4, chart.speed.size)
        assertTrue(chart.strokeRate.last().elapsedMillis > 300_000L)
    }

    @Test
    fun `bike erg chart uses pace per thousand meters`() {
        val chart = assertNotNull(
            reader.read(
                sourceFileBytes = bikeErgJustRide(),
                sourceFileIndex = 0,
                sourceIntervalIndex = 0,
                pieceType = TrainingPieceType.CONCEPT_BIKE_ERG,
            )
        )

        assertEquals(900_000L, chart.durationMillis)
        assertEquals(3, chart.speed.size)
        // 10 м/с = 100 секунд на 1000 м; на 500 м было бы 50 секунд и точка отфильтровалась бы.
        chart.speed.forEach { point -> assertEquals(100.0, point.value, 0.001) }
        assertTrue(chart.speed.last().elapsedMillis > 300_000L)
    }

    @Test
    fun `interval workout chart keeps per lap mapping`() {
        val chart = assertNotNull(
            reader.read(
                sourceFileBytes = intervalWithRest(),
                sourceFileIndex = 0,
                sourceIntervalIndex = 1,
                pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
            )
        )

        assertEquals(300_000L, chart.durationMillis)
        assertEquals(listOf(160.0, 170.0), chart.heartRate.map { it.value })
    }

    private fun rowErgSingleDistance(): ByteArray = concept2FitFile(
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

    private fun bikeErgJustRide(): ByteArray = concept2FitFile(
        laps = listOf(
            FitTestLap(0, 300, 3000.0),
            FitTestLap(300, 300, 3000.0),
            FitTestLap(600, 300, 3000.0),
        ),
        records = listOf(
            FitTestRecord(
                offsetSeconds = 10,
                heartRate = 140,
                cadence = 64,
                speedMetersPerSecond = 10.0,
            ),
            FitTestRecord(
                offsetSeconds = 310,
                heartRate = 142,
                cadence = 65,
                speedMetersPerSecond = 10.0,
            ),
            FitTestRecord(
                offsetSeconds = 610,
                heartRate = 145,
                cadence = 66,
                speedMetersPerSecond = 10.0,
            ),
        ),
        sport = 2,
        subSport = 6,
    )

    private fun intervalWithRest(): ByteArray = concept2FitFile(
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
                offsetSeconds = 430,
                heartRate = 160,
                cadence = 32,
                speedMetersPerSecond = 4.0,
            ),
            FitTestRecord(
                offsetSeconds = 440,
                heartRate = 170,
                cadence = 32,
                speedMetersPerSecond = 4.0,
            ),
        ),
    )
}