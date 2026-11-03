package by.rowing.sanbaiteam.training.presentation.chart.models

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TrainingChartValueBoundsTest {

    @Test
    fun `water pace bounds are cut by fifty seconds`() {
        val values = List(100) { 120.0 } + listOf(60.0, 400.0)
        val average = values.average()

        val bounds = assertNotNull(
            trainingChartValueBounds(
                metric = TrainingChartMetric.SPEED,
                pieceType = TrainingPieceType.SINGLE,
                points = points(values),
            )
        )

        assertEquals(average - 50.0, bounds.start, 0.001)
        assertEquals(average + 50.0, bounds.endInclusive, 0.001)
    }

    @Test
    fun `erg pace bounds are cut by fifteen seconds`() {
        val values = List(100) { 120.0 } + listOf(60.0, 400.0)
        val average = values.average()

        val bounds = assertNotNull(
            trainingChartValueBounds(
                metric = TrainingChartMetric.SPEED,
                pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
                points = points(values),
            )
        )

        assertEquals(average - 15.0, bounds.start, 0.001)
        assertEquals(average + 15.0, bounds.endInclusive, 0.001)
    }

    @Test
    fun `bike erg pace bounds are cut by thirty seconds`() {
        val values = List(100) { 120.0 } + listOf(60.0, 400.0)
        val average = values.average()

        val bounds = assertNotNull(
            trainingChartValueBounds(
                metric = TrainingChartMetric.SPEED,
                pieceType = TrainingPieceType.CONCEPT_BIKE_ERG,
                points = points(values),
            )
        )

        assertEquals(average - 30.0, bounds.start, 0.001)
        assertEquals(average + 30.0, bounds.endInclusive, 0.001)
    }

    @Test
    fun `water stroke rate bounds are absolute`() {
        val bounds = assertNotNull(
            trainingChartValueBounds(
                metric = TrainingChartMetric.STROKE_RATE,
                pieceType = TrainingPieceType.EIGHT,
                points = points(List(100) { 30.0 } + listOf(5.0, 60.0)),
            )
        )

        assertEquals(9.0, bounds.start, 0.001)
        assertEquals(50.0, bounds.endInclusive, 0.001)
    }

    @Test
    fun `erg stroke rate bounds are relative to average`() {
        val values = List(100) { 30.0 } + listOf(5.0, 60.0)
        val average = values.average()

        val bounds = assertNotNull(
            trainingChartValueBounds(
                metric = TrainingChartMetric.STROKE_RATE,
                pieceType = TrainingPieceType.CONCEPT_BIKE_ERG,
                points = points(values),
            )
        )

        assertEquals(average - 10.0, bounds.start, 0.001)
        assertEquals(average + 10.0, bounds.endInclusive, 0.001)
    }

    @Test
    fun `bounds are not applied when data already fits`() {
        val bounds = trainingChartValueBounds(
            metric = TrainingChartMetric.SPEED,
            pieceType = TrainingPieceType.SINGLE,
            points = points(List(100) { 120.0 } + listOf(100.0, 140.0)),
        )

        assertNull(bounds)
    }

    @Test
    fun `bounds are not applied when average is outside water stroke rate range`() {
        val bounds = trainingChartValueBounds(
            metric = TrainingChartMetric.STROKE_RATE,
            pieceType = TrainingPieceType.SINGLE,
            points = points(listOf(52.0, 55.0, 58.0)),
        )

        assertNull(bounds)
    }

    @Test
    fun `bounds are not applied when water stroke rate average is above the range`() {
        val bounds = trainingChartValueBounds(
            metric = TrainingChartMetric.STROKE_RATE,
            pieceType = TrainingPieceType.SINGLE,
            points = points(List(100) { 55.0 } + 5.0),
        )

        assertNull(bounds)
    }

    @Test
    fun `bounds are not applied to a single point`() {
        val bounds = trainingChartValueBounds(
            metric = TrainingChartMetric.SPEED,
            pieceType = TrainingPieceType.SINGLE,
            points = points(listOf(120.0)),
        )

        assertNull(bounds)
    }

    @Test
    fun `heart rate is not limited`() {
        val bounds = trainingChartValueBounds(
            metric = TrainingChartMetric.HEART_RATE,
            pieceType = TrainingPieceType.SINGLE,
            points = points(List(100) { 120.0 } + listOf(220.0)),
        )

        assertNull(bounds)
    }

    @Test
    fun `empty points produce no bounds`() {
        val bounds = trainingChartValueBounds(
            metric = TrainingChartMetric.SPEED,
            pieceType = TrainingPieceType.SINGLE,
            points = emptyList(),
        )

        assertNull(bounds)
    }

    private fun points(values: List<Double>): List<TrainingChartPoint> =
        values.mapIndexed { index, value ->
            TrainingChartPoint(
                elapsedMillis = index * 1_000L,
                value = value,
            )
        }
}