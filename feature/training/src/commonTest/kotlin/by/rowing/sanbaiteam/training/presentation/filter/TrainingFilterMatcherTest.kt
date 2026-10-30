package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingPieceItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TrainingFilterMatcherTest {

    @Test
    fun `empty filter keeps every training`() {
        val trainings = listOf(training(id = 1L), training(id = 2L))

        assertEquals(trainings, trainings.applyTrainingFilter(TrainingFilter()))
        assertTrue(TrainingFilterMatcher.matches(TrainingFilter(), trainings.first()))
    }

    @Test
    fun `athlete filter matches any selected athlete`() {
        val trainings = listOf(
            training(
                id = 1L,
                athleteIds = listOf(1L, 2L)
            ),
            training(
                id = 2L,
                athleteIds = listOf(3L)
            ),
        )

        val result = trainings.applyTrainingFilter(
            TrainingFilter(athleteIds = setOf(2L, 3L))
        )

        assertEquals(listOf(1L, 2L), result.map { it.dateMillis })
    }

    @Test
    fun `training type filter matches any selected type`() {
        val trainings = listOf(
            training(
                id = 1L,
                type = TrainingPieceType.SINGLE
            ),
            training(
                id = 2L,
                type = TrainingPieceType.DOUBLE
            ),
            training(
                id = 3L,
                type = TrainingPieceType.EIGHT
            ),
        )

        val result = trainings.applyTrainingFilter(
            TrainingFilter(trainingTypes = setOf(TrainingPieceType.SINGLE, TrainingPieceType.DOUBLE))
        )

        assertEquals(listOf(1L, 2L), result.map { it.dateMillis })
    }

    @Test
    fun `piece time filter accepts inclusive bounds and any piece`() {
        val trainings = listOf(
            training(
                id = 1L,
                pieces = listOf(piece(timeMillis = 90_000L)),
            ),
            training(
                id = 2L,
                pieces = listOf(
                    piece(timeMillis = 10_000L),
                    piece(timeMillis = 120_000L),
                ),
            ),
            training(
                id = 3L,
                pieces = listOf(piece(timeMillis = 121_000L)),
            ),
        )

        val result = trainings.applyTrainingFilter(
            TrainingFilter(pieceTimeRange = 90_000L..120_000L)
        )

        assertEquals(listOf(1L, 2L), result.map { it.dateMillis })
    }

    @Test
    fun `piece distance filter accepts any piece in range`() {
        val trainings = listOf(
            training(
                id = 1L,
                pieces = listOf(piece(distanceMeters = 500))
            ),
            training(
                id = 2L,
                pieces = listOf(piece(distanceMeters = 2_000))
            ),
            training(
                id = 3L,
                pieces = listOf(piece(distanceMeters = 2_001))
            ),
        )

        val result = trainings.applyTrainingFilter(
            TrainingFilter(pieceDistanceRange = 500..2_000)
        )

        assertEquals(listOf(1L, 2L), result.map { it.dateMillis })
    }

    @Test
    fun `lactate filter keeps trainings with at least one measured piece`() {
        val trainings = listOf(
            training(
                id = 1L,
                pieces = listOf(piece(lactate = null), piece(lactate = 4.2))
            ),
            training(
                id = 2L,
                pieces = listOf(piece(lactate = null))
            ),
        )

        val result = trainings.applyTrainingFilter(TrainingFilter(lactateOnly = true))

        assertEquals(listOf(1L), result.map { it.dateMillis })
    }

    @Test
    fun `date filter accepts inclusive bounds`() {
        val trainings = listOf(
            training(
                id = 1L,
                dateMillis = 1_000L
            ),
            training(
                id = 2L,
                dateMillis = 2_000L
            ),
            training(
                id = 3L,
                dateMillis = 3_000L
            ),
        )

        val result = trainings.applyTrainingFilter(
            TrainingFilter(dateRange = 1_000L..2_000L)
        )

        assertEquals(listOf(1_000L, 2_000L), result.map { it.dateMillis })
    }

    @Test
    fun `different parameters combine with and`() {
        val trainings = listOf(
            training(
                id = 1L,
                athleteIds = listOf(1L),
                type = TrainingPieceType.SINGLE,
                pieces = listOf(piece(distanceMeters = 500)),
            ),
            training(
                id = 2L,
                athleteIds = listOf(2L),
                type = TrainingPieceType.SINGLE,
                pieces = listOf(piece(distanceMeters = 500)),
            ),
            training(
                id = 3L,
                athleteIds = listOf(1L),
                type = TrainingPieceType.DOUBLE,
                pieces = listOf(piece(distanceMeters = 500)),
            ),
        )

        val result = trainings.applyTrainingFilter(
            TrainingFilter(
                athleteIds = setOf(1L),
                trainingTypes = setOf(TrainingPieceType.SINGLE),
            )
        )

        assertEquals(listOf(1L), result.map { it.dateMillis })
    }

    @Test
    fun `training without pieces does not match piece filters`() {
        val training = training(
            id = 1L,
            pieces = emptyList()
        )

        val result = listOf(training).applyTrainingFilter(
            TrainingFilter(pieceDistanceRange = 0..Int.MAX_VALUE)
        )

        assertTrue(result.isEmpty())
    }

    private fun training(
        id: Long,
        type: TrainingPieceType = TrainingPieceType.SINGLE,
        athleteIds: List<Long> = listOf(1L),
        pieces: List<ListTrainingPieceItem> = listOf(piece()),
        dateMillis: Long = id,
    ): TestTraining = TestTraining(
        dateMillis = dateMillis,
        trainingType = type,
        athleteIds = athleteIds,
        pieces = pieces,
    )

    private fun piece(
        distanceMeters: Int = 500,
        timeMillis: Long = 90_000L,
        lactate: Double? = null,
    ): ListTrainingPieceItem = ListTrainingPieceItem(
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        lactate = lactate,
    )

    private data class TestTraining(
        override val dateMillis: Long,
        override val trainingType: TrainingPieceType,
        override val athleteIds: List<Long>,
        override val pieces: List<ListTrainingPieceItem>,
    ) : FilterableTraining
}