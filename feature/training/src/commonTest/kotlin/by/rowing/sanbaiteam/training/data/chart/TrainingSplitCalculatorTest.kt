package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint
import by.rowing.sanbaiteam.training.data.models.chart.TrainingPieceChartData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TrainingSplitCalculatorTest {

    @Test
    fun `distance splits are even`() {
        val chart = chart(
            distance = linearDistance(
                durationMillis = 300_000L,
                distance = 1_500.0
            )
        )

        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 1_500,
                totalTimeMillis = 300_000L,
                length = TrainingSplitLength.Distance(meters = 500),
            )
        )

        assertEquals(listOf(500, 500, 500), splits.map { it.distanceMeters })
        assertEquals(listOf(100_000L, 100_000L, 100_000L), splits.map { it.timeMillis })
        assertEquals(listOf(100_000L, 100_000L, 100_000L), splits.map { it.paceMillis })
        assertEquals(listOf(1, 2, 3), splits.map { it.index })
    }

    @Test
    fun `distance splits keep remainder in the last split`() {
        val chart = chart(
            distance = linearDistance(
                durationMillis = 400_000L,
                distance = 2_000.0
            )
        )

        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 2_000,
                totalTimeMillis = 400_000L,
                length = TrainingSplitLength.Distance(meters = 750),
            )
        )

        assertEquals(listOf(750, 750, 500), splits.map { it.distanceMeters })
        assertEquals(listOf(150_000L, 150_000L, 100_000L), splits.map { it.timeMillis })
        assertEquals(2_000, splits.sumOf { it.distanceMeters })
        assertEquals(400_000L, splits.sumOf { it.timeMillis })
    }

    @Test
    fun `time splits keep remainder in the last split`() {
        val chart = chart(
            distance = linearDistance(
                durationMillis = 300_000L,
                distance = 1_000.0
            )
        )

        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 1_000,
                totalTimeMillis = 300_000L,
                length = TrainingSplitLength.Time(millis = 120_000L),
            )
        )

        assertEquals(listOf(120_000L, 120_000L, 60_000L), splits.map { it.timeMillis })
        assertEquals(listOf(400, 400, 200), splits.map { it.distanceMeters })
        assertEquals(1_000, splits.sumOf { it.distanceMeters })
    }

    @Test
    fun `split longer than piece produces single split`() {
        val chart = chart(
            distance = linearDistance(
                durationMillis = 200_000L,
                distance = 1_000.0
            )
        )

        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 1_000,
                totalTimeMillis = 200_000L,
                length = TrainingSplitLength.Distance(meters = 5_000),
            )
        )

        assertEquals(1, splits.size)
        assertEquals(1_000, splits.single().distanceMeters)
        assertEquals(200_000L, splits.single().timeMillis)
    }

    @Test
    fun `missing distance track produces no splits`() {
        val chart = chart(distance = emptyList())

        assertNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 1_000,
                totalTimeMillis = 200_000L,
                length = TrainingSplitLength.Distance(meters = 500),
            )
        )
    }

    @Test
    fun `single point distance track produces no splits`() {
        assertNull(
            TrainingSplitCalculator.calculate(
                chart = chart(
                    distance = listOf(
                        TrainingChartPoint(
                            elapsedMillis = 0L,
                            value = 0.0
                        )
                    )
                ),
                totalDistanceMeters = 1_000,
                totalTimeMillis = 200_000L,
                length = TrainingSplitLength.Distance(meters = 500),
            )
        )
    }

    @Test
    fun `missing stroke rate and heart rate produce empty values`() {
        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart(
                    distance = linearDistance(
                        durationMillis = 200_000L,
                        distance = 1_000.0
                    )
                ),
                totalDistanceMeters = 1_000,
                totalTimeMillis = 200_000L,
                length = TrainingSplitLength.Distance(meters = 500),
            )
        )

        splits.forEach { split ->
            assertNull(split.strokeRate)
            assertNull(split.avgHeartRate)
            assertNull(split.maxHeartRate)
        }
    }

    @Test
    fun `split aggregates use points inside boundaries`() {
        val chart = TrainingPieceChartData(
            durationMillis = 200_000L,
            distance = linearDistance(
                durationMillis = 200_000L,
                distance = 1_000.0
            ),
            strokeRate = listOf(
                TrainingChartPoint(
                    elapsedMillis = 0L,
                    value = 20.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 90_000L,
                    value = 30.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 100_000L,
                    value = 40.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 200_000L,
                    value = 50.0
                ),
            ),
            heartRate = listOf(
                TrainingChartPoint(
                    elapsedMillis = 0L,
                    value = 100.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 90_000L,
                    value = 120.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 100_000L,
                    value = 140.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 200_000L,
                    value = 160.0
                ),
            ),
        )

        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 1_000,
                totalTimeMillis = 200_000L,
                length = TrainingSplitLength.Distance(meters = 500),
            )
        )

        assertEquals(listOf(25.0, 45.0), splits.map { it.strokeRate })
        assertEquals(listOf(110, 150), splits.map { it.avgHeartRate })
        assertEquals(listOf(120, 160), splits.map { it.maxHeartRate })
    }

    @Test
    fun `non monotonic track is normalized and scaled to piece totals`() {
        val chart = chart(
            distance = listOf(
                TrainingChartPoint(
                    elapsedMillis = 0L,
                    value = 0.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 100_000L,
                    value = 600.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 50_000L,
                    value = 300.0
                ),
                TrainingChartPoint(
                    elapsedMillis = 200_000L,
                    value = 700.0
                ),
            )
        )

        val splits = assertNotNull(
            TrainingSplitCalculator.calculate(
                chart = chart,
                totalDistanceMeters = 1_000,
                totalTimeMillis = 200_000L,
                length = TrainingSplitLength.Distance(meters = 500),
            )
        )

        assertEquals(1_000, splits.sumOf { it.distanceMeters })
        assertEquals(200_000L, splits.sumOf { it.timeMillis })
        assertEquals(500, splits.first().distanceMeters)
    }

    private fun chart(distance: List<TrainingChartPoint>): TrainingPieceChartData =
        TrainingPieceChartData(
            durationMillis = distance.lastOrNull()?.elapsedMillis ?: 0L,
            distance = distance,
        )

    private fun linearDistance(
        durationMillis: Long,
        distance: Double,
    ): List<TrainingChartPoint> = listOf(
        TrainingChartPoint(
            elapsedMillis = 0L,
            value = 0.0
        ),
        TrainingChartPoint(
            elapsedMillis = durationMillis / 2,
            value = distance / 2
        ),
        TrainingChartPoint(
            elapsedMillis = durationMillis,
            value = distance
        ),
    )
}