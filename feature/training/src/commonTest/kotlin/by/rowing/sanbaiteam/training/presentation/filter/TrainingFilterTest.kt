package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TrainingFilterTest {

    @Test
    fun `empty filter has no active parameters`() {
        val filter = TrainingFilter()

        assertTrue(filter.isEmpty)
        assertEquals(0, filter.activeParametersCount)
        TrainingFilterParameter.entries.forEach { parameter ->
            assertFalse(filter.isActive(parameter))
        }
    }

    @Test
    fun `active parameters are counted`() {
        val filter = TrainingFilter(
            athleteIds = setOf(1L),
            trainingTypes = setOf(TrainingPieceType.SINGLE),
            pieceTimeRange = 60_000L..120_000L,
            pieceDistanceRange = 500..2_000,
            lactateOnly = true,
            dateRange = 0L..1_000L,
        )

        assertFalse(filter.isEmpty)
        assertEquals(TrainingFilterParameter.entries.size, filter.activeParametersCount)
        TrainingFilterParameter.entries.forEach { parameter ->
            assertTrue(filter.isActive(parameter))
        }
    }

    @Test
    fun `each parameter is detected individually`() {
        assertTrue(TrainingFilter(athleteIds = setOf(1L)).isActive(TrainingFilterParameter.ATHLETES))
        assertTrue(
            TrainingFilter(trainingTypes = setOf(TrainingPieceType.DOUBLE))
                .isActive(TrainingFilterParameter.TRAINING_TYPE)
        )
        assertTrue(
            TrainingFilter(pieceTimeRange = 0L..60_000L)
                .isActive(TrainingFilterParameter.PIECE_TIME)
        )
        assertTrue(
            TrainingFilter(pieceDistanceRange = 0..500)
                .isActive(TrainingFilterParameter.PIECE_DISTANCE)
        )
        assertTrue(TrainingFilter(lactateOnly = true).isActive(TrainingFilterParameter.LACTATE))
        assertTrue(TrainingFilter(dateRange = 0L..1_000L).isActive(TrainingFilterParameter.DATE))
    }

    @Test
    fun `without resets only requested parameter`() {
        val filter = TrainingFilter(
            athleteIds = setOf(1L),
            trainingTypes = setOf(TrainingPieceType.SINGLE),
            lactateOnly = true,
        )

        assertEquals(
            TrainingFilter(
                trainingTypes = setOf(TrainingPieceType.SINGLE),
                lactateOnly = true,
            ),
            filter.without(TrainingFilterParameter.ATHLETES)
        )
        assertEquals(
            TrainingFilter(
                athleteIds = setOf(1L),
                lactateOnly = true,
            ),
            filter.without(TrainingFilterParameter.TRAINING_TYPE)
        )
        assertEquals(
            TrainingFilter(
                athleteIds = setOf(1L),
                trainingTypes = setOf(TrainingPieceType.SINGLE),
            ),
            filter.without(TrainingFilterParameter.LACTATE)
        )
    }

    @Test
    fun `apply preset overwrites only preset parameters`() {
        val current = TrainingFilter(
            athleteIds = setOf(1L),
            trainingTypes = setOf(TrainingPieceType.SINGLE),
            lactateOnly = true,
        )
        val preset = TrainingFilter(
            trainingTypes = setOf(TrainingPieceType.DOUBLE),
            pieceDistanceRange = 500..2_000,
        )

        assertEquals(
            TrainingFilter(
                athleteIds = setOf(1L),
                trainingTypes = setOf(TrainingPieceType.DOUBLE),
                pieceDistanceRange = 500..2_000,
                lactateOnly = true,
            ),
            current.applyPreset(preset),
        )
    }

    @Test
    fun `apply empty preset keeps current filter`() {
        val current = TrainingFilter(
            athleteIds = setOf(1L),
            dateRange = 0L..1_000L,
        )

        assertEquals(current, current.applyPreset(TrainingFilter()))
    }

    @Test
    fun `apply full preset replaces all parameters`() {
        val current = TrainingFilter(
            athleteIds = setOf(1L),
        )
        val preset = TrainingFilter(
            athleteIds = setOf(2L),
            trainingTypes = setOf(TrainingPieceType.PAIR),
            pieceTimeRange = 60_000L..120_000L,
            pieceDistanceRange = 500..2_000,
            lactateOnly = true,
            dateRange = 0L..1_000L,
        )

        assertEquals(
            preset,
            current.applyPreset(preset),
        )
    }
}