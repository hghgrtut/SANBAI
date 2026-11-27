package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.models.comparison.ComparisonMetric
import by.rowing.sanbaiteam.training.data.models.comparison.ComparisonTrend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TrainingRepositoryComparisonTest {

    @Test
    fun `candidates contain only same type without current training`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(
                training(
                    id = 1,
                    type = TrainingPieceType.SINGLE,
                    date = 3
                ),
                training(
                    id = 2,
                    type = TrainingPieceType.SINGLE,
                    date = 2
                ),
                training(
                    id = 3,
                    type = TrainingPieceType.DOUBLE,
                    date = 1
                ),
                training(
                    id = 4,
                    type = TrainingPieceType.SINGLE,
                    date = 0
                ),
            ),
            pieces = listOf(
                piece(
                    trainingId = 1,
                    order = 0
                ),
                piece(
                    trainingId = 2,
                    order = 0
                ),
                piece(
                    trainingId = 3,
                    order = 0
                ),
            ),
        )

        val candidates = repository.getComparisonCandidates(trainingId = 1)

        assertEquals(listOf(2L), candidates.map { it.trainingId })
    }

    @Test
    fun `detail compares pieces with the same order and skips other types`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(
                training(
                    id = 1,
                    type = TrainingPieceType.SINGLE
                ),
                training(
                    id = 2,
                    type = TrainingPieceType.SINGLE
                ),
                training(
                    id = 3,
                    type = TrainingPieceType.DOUBLE
                ),
            ),
            pieces = listOf(
                piece(
                    trainingId = 1,
                    order = 0,
                    timeMillis = 200_000L
                ),
                piece(
                    trainingId = 1,
                    order = 1,
                    timeMillis = 210_000L
                ),
                piece(
                    trainingId = 2,
                    order = 0,
                    timeMillis = 220_000L
                ),
                piece(
                    trainingId = 2,
                    order = 1,
                    timeMillis = 230_000L
                ),
                piece(
                    trainingId = 3,
                    order = 0,
                    timeMillis = 999_000L
                ),
            ),
        )

        val state = repository.getTrainingDetail(
            trainingId = 1,
            comparedTrainingIds = listOf(2L, 3L),
        )

        assertEquals(listOf(2L), state.comparedTrainingIds)
        val first = state.pieces[0].comparison
        requireNotNull(first)
        assertEquals(1, first.comparedTrainingsCount)
        val time = first.metrics.getValue(ComparisonMetric.TIME)
        assertEquals(220_000.0, time.average, 0.0001)
        assertEquals(-20_000.0, time.delta!!, 0.0001)
        assertEquals(ComparisonTrend.BETTER, time.trend)
        assertNull(first.metrics[ComparisonMetric.PERCENT_OF_PB])
    }

    @Test
    fun `detail matches pieces by duration when piece count differs`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(
                training(
                    id = 1,
                    type = TrainingPieceType.SINGLE
                ),
                training(
                    id = 2,
                    type = TrainingPieceType.SINGLE
                ),
                training(
                    id = 3,
                    type = TrainingPieceType.SINGLE
                ),
            ),
            pieces = listOf(
                piece(
                    trainingId = 1,
                    order = 0,
                    timeMillis = 900_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 1,
                    order = 1,
                    timeMillis = 900_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 1,
                    order = 2,
                    timeMillis = 900_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 1,
                    order = 3,
                    timeMillis = 420_000L,
                    distanceMeters = 1400
                ),
                piece(
                    trainingId = 2,
                    order = 0,
                    timeMillis = 890_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 2,
                    order = 1,
                    timeMillis = 890_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 2,
                    order = 2,
                    timeMillis = 890_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 2,
                    order = 3,
                    timeMillis = 430_000L,
                    distanceMeters = 1400
                ),
                piece(
                    trainingId = 3,
                    order = 0,
                    timeMillis = 880_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 3,
                    order = 1,
                    timeMillis = 880_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 3,
                    order = 2,
                    timeMillis = 440_000L,
                    distanceMeters = 1400
                ),
            ),
        )

        val state = repository.getTrainingDetail(
            trainingId = 1,
            comparedTrainingIds = listOf(2L, 3L),
        )

        assertEquals(4, state.pieces.size)
        val thirdPieceComparison = requireNotNull(state.pieces[2].comparison)
        assertEquals(1, thirdPieceComparison.comparedTrainingsCount)
        assertEquals(890_000.0, thirdPieceComparison.metrics.getValue(ComparisonMetric.TIME).average, 0.0001)

        val lastPieceComparison = requireNotNull(state.pieces[3].comparison)
        assertEquals(2, lastPieceComparison.comparedTrainingsCount)
        assertEquals(435_000.0, lastPieceComparison.metrics.getValue(ComparisonMetric.TIME).average, 0.0001)
    }

    @Test
    fun `detail falls back to order when durations do not match`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(
                training(
                    id = 1,
                    type = TrainingPieceType.SINGLE
                ),
                training(
                    id = 2,
                    type = TrainingPieceType.SINGLE
                ),
            ),
            pieces = listOf(
                piece(
                    trainingId = 1,
                    order = 0,
                    timeMillis = 900_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 1,
                    order = 1,
                    timeMillis = 420_000L,
                    distanceMeters = 1400
                ),
                piece(
                    trainingId = 2,
                    order = 0,
                    timeMillis = 600_000L,
                    distanceMeters = 2000
                ),
                piece(
                    trainingId = 2,
                    order = 1,
                    timeMillis = 300_000L,
                    distanceMeters = 1000
                ),
                piece(
                    trainingId = 2,
                    order = 2,
                    timeMillis = 720_000L,
                    distanceMeters = 2400
                ),
            ),
        )

        val state = repository.getTrainingDetail(
            trainingId = 1,
            comparedTrainingIds = listOf(2L),
        )

        assertEquals(2, state.pieces.size)
        state.pieces.forEach { piece ->
            assertEquals(1, requireNotNull(piece.comparison).comparedTrainingsCount)
        }
        assertEquals(
            600_000.0,
            state.pieces[0].comparison!!.metrics.getValue(ComparisonMetric.TIME).average,
            0.0001,
        )
        assertEquals(
            300_000.0,
            state.pieces[1].comparison!!.metrics.getValue(ComparisonMetric.TIME).average,
            0.0001,
        )
    }

    @Test
    fun `detail compares by order when piece count is equal`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(
                training(
                    id = 1,
                    type = TrainingPieceType.SINGLE
                ),
                training(
                    id = 2,
                    type = TrainingPieceType.SINGLE
                ),
            ),
            pieces = listOf(
                piece(
                    trainingId = 1,
                    order = 0,
                    timeMillis = 900_000L,
                    distanceMeters = 3000
                ),
                piece(
                    trainingId = 1,
                    order = 1,
                    timeMillis = 420_000L,
                    distanceMeters = 1400
                ),
                piece(
                    trainingId = 2,
                    order = 0,
                    timeMillis = 420_000L,
                    distanceMeters = 1400
                ),
                piece(
                    trainingId = 2,
                    order = 1,
                    timeMillis = 900_000L,
                    distanceMeters = 3000
                ),
            ),
        )

        val state = repository.getTrainingDetail(
            trainingId = 1,
            comparedTrainingIds = listOf(2L),
        )

        state.pieces.forEach { piece ->
            assertEquals(1, requireNotNull(piece.comparison).comparedTrainingsCount)
        }
        assertEquals(
            420_000.0,
            state.pieces[0].comparison!!.metrics.getValue(ComparisonMetric.TIME).average,
            0.0001,
        )
        assertEquals(
            900_000.0,
            state.pieces[1].comparison!!.metrics.getValue(ComparisonMetric.TIME).average,
            0.0001,
        )
    }

    private fun training(
        id: Long,
        type: TrainingPieceType,
        date: Long = id,
    ) = TrainingEntity(
        id = id,
        dateMillis = date,
        type = type,
    )

    private fun piece(
        trainingId: Long,
        order: Int,
        timeMillis: Long = 200_000L,
        distanceMeters: Int = 1000,
    ) = TrainingAthletePieceEntity(
        trainingId = trainingId,
        athleteId = 10L,
        order = order,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        strokeRate = 30.0,
    )
}