package by.rowing.sanbaiteam.training.data.trainingimport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class Concept2LapCsvTest {

    @Test
    fun encodeAndDecode_lapArchive_roundTripsEveryField() {
        val laps = listOf(
            lap(Concept2LapIntensity.ACTIVE, 1_000L, 900_000L, 4464.0, avgHeartRate = 174, maxHeartRate = 183),
            lap(Concept2LapIntensity.REST, 901_000L, 360_000L, 427.5, avgHeartRate = 138, maxHeartRate = 141),
            lap(Concept2LapIntensity.ACTIVE, 1_261_000L, 900_000L, 4460.0),
        )

        val csvText = Concept2LapCsv.encode(laps)

        assertTrue(Concept2LapCsv.isArchive(csvText))
        assertEquals(laps, Concept2LapCsv.decode(csvText))
    }

    @Test
    fun decode_archiveWithUnknownIntensity_skipsBrokenRow() {
        val csvText = buildString {
            appendLine(Concept2LapCsv.MARKER)
            appendLine("lap,intensity,startTime,elapsed,distance,avgHr,maxHr,avgCadence")
            appendLine("1,WARMUP,1000,900000,4464.0,174,183,24")
            appendLine("2,ACTIVE,901000,360000,427.5,138,141,20")
        }

        val laps = Concept2LapCsv.decode(csvText)

        assertEquals(1, laps.size)
        assertEquals(Concept2LapIntensity.ACTIVE, laps.single().intensity)
        assertEquals(138, laps.single().avgHeartRate)
        assertEquals(20, laps.single().avgCadence)
    }

    @Test
    fun merge_twoArchives_renumbersLapsAndKeepsValues() {
        val first = Concept2LapCsv.encode(
            listOf(lap(Concept2LapIntensity.ACTIVE, 1_000L, 900_000L, 4464.0, avgHeartRate = 174))
        )
        val second = Concept2LapCsv.encode(
            listOf(lap(Concept2LapIntensity.ACTIVE, 5_000L, 900_000L, 4460.0, avgHeartRate = 182))
        )

        val merged = Concept2LapCsv.decode(Concept2LapCsv.merge(listOf(first, second)))

        assertEquals(listOf(1_000L, 5_000L), merged.map { it.startTimeMillis })
        assertEquals(listOf(174, 182), merged.map { it.avgHeartRate })
    }

    @Test
    fun isArchive_speedCoachCsv_isNotRecognizedAsConcept2Archive() {
        assertFalse(Concept2LapCsv.isArchive("Session Information:,,,Device Information:"))
    }

    private fun lap(
        intensity: Concept2LapIntensity,
        startTimeMillis: Long,
        elapsedMillis: Long,
        distanceMeters: Double,
        avgHeartRate: Int? = null,
        maxHeartRate: Int? = null
    ): Concept2LapSummary = Concept2LapSummary(
        intensity = intensity,
        startTimeMillis = startTimeMillis,
        totalElapsedTimeMillis = elapsedMillis,
        totalDistanceMeters = distanceMeters,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
        avgCadence = 24,
    )
}