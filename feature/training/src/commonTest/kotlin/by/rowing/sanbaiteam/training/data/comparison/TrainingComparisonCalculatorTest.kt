package by.rowing.sanbaiteam.training.data.comparison

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TrainingComparisonCalculatorTest {

    @Test
    fun `one compared training uses its values`() {
        val base = piece(
            distanceMeters = 1000,
            timeMillis = 200_000L,
            strokeRate = 30.0,
            avgHeartRate = 150,
            maxHeartRate = 170,
            recoveryHeartRate = 120,
            lactate = 4.0,
            percentOfPb = 90.0,
        )
        val compared = piece(
            distanceMeters = 950,
            timeMillis = 210_000L,
            strokeRate = 31.0,
            avgHeartRate = 148,
            maxHeartRate = 172,
            recoveryHeartRate = 118,
            lactate = 4.2,
            percentOfPb = 88.0,
        )

        val result = TrainingComparisonCalculator.compare(base, listOf(compared))

        requireNotNull(result)
        assertEquals(1, result.comparedTrainingsCount)
        assertMetric(result, ComparisonMetric.DISTANCE, 950.0, 50.0, ComparisonTrend.BETTER)
        assertMetric(result, ComparisonMetric.TIME, 210_000.0, -10_000.0, ComparisonTrend.BETTER)
        assertMetric(result, ComparisonMetric.PACE, 110_500.0, -10_500.0, ComparisonTrend.BETTER)
        assertMetric(result, ComparisonMetric.STROKE_RATE, 31.0, -1.0, ComparisonTrend.NEUTRAL)
        assertMetric(result, ComparisonMetric.AVG_HEART_RATE, 148.0, 2.0, ComparisonTrend.WORSE)
        assertMetric(result, ComparisonMetric.MAX_HEART_RATE, 172.0, -2.0, ComparisonTrend.BETTER)
        assertMetric(result, ComparisonMetric.RECOVERY_HEART_RATE, 118.0, 2.0, ComparisonTrend.WORSE)
        assertMetric(result, ComparisonMetric.LACTATE, 4.2, -0.2, ComparisonTrend.BETTER)
        assertMetric(result, ComparisonMetric.PERCENT_OF_PB, 88.0, 2.0, ComparisonTrend.BETTER)
    }

    @Test
    fun `optional metrics are averaged only over non null values`() {
        val base = piece(
            avgHeartRate = null,
            lactate = 4.0,
            percentOfPb = null,
        )
        val compared = listOf(
            piece(
                avgHeartRate = 140,
                lactate = null,
                percentOfPb = null,
            ),
            piece(
                avgHeartRate = null,
                lactate = 4.4,
                percentOfPb = null,
            ),
        )

        val result = TrainingComparisonCalculator.compare(base, compared)

        requireNotNull(result)
        assertMetric(result, ComparisonMetric.AVG_HEART_RATE, 140.0, null, ComparisonTrend.NEUTRAL)
        assertMetric(result, ComparisonMetric.LACTATE, 4.4, -0.4, ComparisonTrend.BETTER)
        assertFalse(result.metrics.containsKey(ComparisonMetric.MAX_HEART_RATE))
        assertFalse(result.metrics.containsKey(ComparisonMetric.RECOVERY_HEART_RATE))
        assertFalse(result.metrics.containsKey(ComparisonMetric.PERCENT_OF_PB))
    }

    @Test
    fun `returns null when there are no compared pieces`() {
        assertNull(TrainingComparisonCalculator.compare(piece(), emptyList()))
    }

    @Test
    fun `time and stroke rate are rounded`() {
        val base = piece(
            timeMillis = 200_000L,
            strokeRate = 31.0,
        )
        val compared = listOf(
            piece(timeMillis = 200_049L, strokeRate = 31.24),
            piece(timeMillis = 200_149L, strokeRate = 31.26),
        )

        val result = TrainingComparisonCalculator.compare(base, compared)

        requireNotNull(result)
        assertMetric(result, ComparisonMetric.TIME, 200_100.0, -100.0, ComparisonTrend.BETTER)
        assertMetric(result, ComparisonMetric.STROKE_RATE, 31.3, -0.3, ComparisonTrend.NEUTRAL)
    }

    @Test
    fun `equal values have neutral delta`() {
        val base = piece(
            distanceMeters = 500,
            timeMillis = 100_000L,
            strokeRate = 32.0,
            avgHeartRate = 150,
        )

        val result = TrainingComparisonCalculator.compare(base, listOf(base))

        requireNotNull(result)
        assertMetric(result, ComparisonMetric.DISTANCE, 500.0, 0.0, ComparisonTrend.NEUTRAL)
        assertMetric(result, ComparisonMetric.TIME, 100_000.0, 0.0, ComparisonTrend.NEUTRAL)
        assertMetric(result, ComparisonMetric.AVG_HEART_RATE, 150.0, 0.0, ComparisonTrend.NEUTRAL)
        assertTrue(result.metrics.containsKey(ComparisonMetric.PACE))
    }

    private fun assertMetric(
        comparison: PieceComparison,
        metric: ComparisonMetric,
        average: Double,
        delta: Double?,
        trend: ComparisonTrend,
    ) {
        val actual = comparison.metrics.getValue(metric)
        assertEquals(average, actual.average, 0.0001)
        if (delta == null) {
            assertNull(actual.delta)
        } else {
            assertEquals(delta, actual.delta!!, 0.0001)
        }
        assertEquals(trend, actual.trend)
    }

    private fun piece(
        distanceMeters: Int = 1000,
        timeMillis: Long = 200_000L,
        strokeRate: Double = 30.0,
        avgHeartRate: Int? = null,
        maxHeartRate: Int? = null,
        recoveryHeartRate: Int? = null,
        lactate: Double? = null,
        percentOfPb: Double? = null,
    ) = ComparablePiece(
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        strokeRate = strokeRate,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
        recoveryHeartRate = recoveryHeartRate,
        lactate = lactate,
        percentOfPb = percentOfPb,
    )
}
