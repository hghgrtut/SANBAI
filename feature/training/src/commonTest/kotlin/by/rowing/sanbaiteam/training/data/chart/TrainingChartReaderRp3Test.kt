package by.rowing.sanbaiteam.training.data.chart

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class TrainingChartReaderRp3Test {

    @Test
    fun `rp3 csv interval produces pace stroke rate and heart rate series`() {
        val reader = TrainingChartReader()

        val data = assertNotNull(
            reader.read(
                sourceFileBytes = RP3_CSV.encodeToByteArray(),
                sourceFileIndex = 0,
                sourceIntervalIndex = 0,
            )
        )

        assertEquals(4_000L, data.durationMillis)
        assertEquals(3, data.speed.size)
        assertEquals(3, data.strokeRate.size)
        assertEquals(2, data.heartRate.size)
        assertEquals(120.0, data.speed.first().value)
        assertEquals(0L, data.speed.first().elapsedMillis)
        assertEquals(4_000L, data.speed.last().elapsedMillis)
        assertEquals(3, data.availableMetrics.size)
    }

    @Test
    fun `rp3 csv second interval is sliced independently`() {
        val reader = TrainingChartReader()

        val data = assertNotNull(
            reader.read(
                sourceFileBytes = RP3_CSV.encodeToByteArray(),
                sourceFileIndex = 0,
                sourceIntervalIndex = 1,
            )
        )

        assertEquals(1_000L, data.durationMillis)
        assertEquals(2, data.speed.size)
        assertEquals(0L, data.speed.first().elapsedMillis)
    }

    @Test
    fun `legacy csv without rp3 header has no chart`() {
        val reader = TrainingChartReader()

        assertEquals(
            null,
            reader.read(
                sourceFileBytes = "time,distance\n0,10\n".encodeToByteArray(),
                sourceFileIndex = 0,
                sourceIntervalIndex = 0,
            )
        )
    }

    private companion object {
        val RP3_CSV = """
            id,workout_interval_id,stroke_number,stroke_rate,time,distance,estimated_500m_time,pulse
            ,1,1,20.0,1.0,10.0,120.0,100
            ,1,2,21.0,3.0,25.0,115.0,105
            ,1,3,22.0,5.0,40.0,110.0,0
            ,2,1,24.0,10.0,10.0,105.0,110
            ,2,2,25.0,11.0,22.0,104.0,112
        """.trimIndent()
    }
}
