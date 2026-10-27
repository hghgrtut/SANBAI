package by.rowing.sanbaiteam.uikit.component.chart

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MetricChartMathTest {

    @Test
    fun `nice range covers values with even steps`() {
        val range = niceRange(
            min = 101.0,
            max = 189.0
        )

        assertTrue(range.min <= 101.0)
        assertTrue(range.max >= 189.0)
        assertTrue(range.step > 0.0)
    }

    @Test
    fun `time tick step produces readable labels`() {
        assertEquals(60_000L, timeTickStep(durationMillis = 240_000L))
        assertEquals(1_000L, timeTickStep(durationMillis = 4_000L))
    }
}