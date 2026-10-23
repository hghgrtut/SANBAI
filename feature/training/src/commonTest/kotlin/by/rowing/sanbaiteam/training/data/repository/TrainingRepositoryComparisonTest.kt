package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.comparison.ComparisonMetric
import by.rowing.sanbaiteam.training.data.comparison.ComparisonTrend
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TrainingRepositoryComparisonTest {

    @Test
    fun `candidates contain only same type without current training`() = runTest {
        val repository = repository(
            trainings = listOf(
                training(id = 1, type = TrainingPieceType.SINGLE, date = 3),
                training(id = 2, type = TrainingPieceType.SINGLE, date = 2),
                training(id = 3, type = TrainingPieceType.DOUBLE, date = 1),
                training(id = 4, type = TrainingPieceType.SINGLE, date = 0),
            ),
            pieces = listOf(
                piece(trainingId = 1, order = 0),
                piece(trainingId = 2, order = 0),
                piece(trainingId = 3, order = 0),
            ),
        )

        val candidates = repository.getComparisonCandidates(trainingId = 1)

        assertEquals(listOf(2L), candidates.items.map { it.trainingId })
        assertNotNull(candidates.typeResId)
    }

    @Test
    fun `detail compares pieces with the same order and skips other types`() = runTest {
        val repository = repository(
            trainings = listOf(
                training(id = 1, type = TrainingPieceType.SINGLE),
                training(id = 2, type = TrainingPieceType.SINGLE),
                training(id = 3, type = TrainingPieceType.DOUBLE),
            ),
            pieces = listOf(
                piece(trainingId = 1, order = 0, timeMillis = 200_000L),
                piece(trainingId = 1, order = 1, timeMillis = 210_000L),
                piece(trainingId = 2, order = 0, timeMillis = 220_000L),
                piece(trainingId = 2, order = 1, timeMillis = 230_000L),
                piece(trainingId = 3, order = 0, timeMillis = 999_000L),
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
    fun `missing compared order leaves the piece without comparison`() = runTest {
        val repository = repository(
            trainings = listOf(
                training(id = 1, type = TrainingPieceType.SINGLE),
                training(id = 2, type = TrainingPieceType.SINGLE),
            ),
            pieces = listOf(
                piece(trainingId = 1, order = 0),
                piece(trainingId = 1, order = 1),
                piece(trainingId = 2, order = 0, timeMillis = 220_000L),
            ),
        )

        val state = repository.getTrainingDetail(
            trainingId = 1,
            comparedTrainingIds = listOf(2L),
        )

        assertNotNull(state.pieces[0].comparison)
        assertNull(state.pieces[1].comparison)
    }

    private fun repository(
        trainings: List<TrainingEntity>,
        pieces: List<TrainingAthletePieceEntity>,
    ) = TrainingRepository(
        dao = FakeTrainingDao(trainings = trainings, pieces = pieces),
        athletesRepository = StubAthletesRepository(),
        importStorage = FakeTrainingImportStorage(),
    )

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

    private class FakeTrainingDao(
        private val trainings: List<TrainingEntity>,
        private val pieces: List<TrainingAthletePieceEntity>,
    ) : TrainingDao {

        override suspend fun insertTraining(training: TrainingEntity): Long = 1L

        override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) = Unit

        override suspend fun getAllTrainingEntities(): List<TrainingEntity> = trainings

        override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
            trainings.firstOrNull { it.id == trainingId }

        override suspend fun getCsvPathsByIds(trainingIds: List<Long>): List<String> = emptyList()

        override suspend fun deletePiecesForTrainings(trainingIds: List<Long>) = Unit

        override suspend fun deleteTrainings(trainingIds: List<Long>) = Unit

        override suspend fun updateImportMetadata(
            trainingId: Long,
            relativePath: String?,
            deviceSerial: String?,
            sessionName: String?,
        ) = Unit

        override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
            pieces
                .filter { it.trainingId == trainingId }
                .sortedWith(compareBy({ it.athleteId }, { it.order }))

        override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> =
            pieces
                .filter { it.trainingId == trainingId }
                .map { it.athleteId }
                .distinct()
    }
}
