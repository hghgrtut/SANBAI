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
    fun `bounded range respects bounds`() {
        val range = boundedRange(
            min = 60.0,
            max = 400.0,
            bounds = 72.0..172.0,
        )

        assertEquals(72.0, range.min)
        assertEquals(172.0, range.max)
        assertEquals(25.0, range.step)
    }

    @Test
    fun `bounded range keeps narrower data`() {
        val range = boundedRange(
            min = 100.0,
            max = 140.0,
            bounds = 70.0..170.0,
        )

        assertEquals(100.0, range.min)
        assertEquals(140.0, range.max)
        assertEquals(10.0, range.step)
    }

    @Test
    fun `bounded range falls back for collapsed data`() {
        val range = boundedRange(
            min = 100.0,
            max = 100.0,
            bounds = 90.0..110.0,
        )

        assertEquals(90.0, range.min)
        assertEquals(110.0, range.max)
        assertEquals(5.0, range.step)
    }

    @Test
    fun `time tick step produces readable labels`() {
        assertEquals(60_000L, timeTickStep(durationMillis = 240_000L))
        assertEquals(1_000L, timeTickStep(durationMillis = 4_000L))
    }
}