package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.chart.TrainingChartReader
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TrainingRepositoryWorkSummaryTest {

    @Test
    fun `erg training shows time summary`() = runTest {
        val repository = repository(
            training = training(type = TrainingPieceType.CONCEPT_ROW_ERG),
            pieces = listOf(
                piece(
                    order = 0,
                    distanceMeters = 4000,
                    timeMillis = 900_000L
                ),
                piece(
                    order = 1,
                    distanceMeters = 4000,
                    timeMillis = 900_000L
                ),
                piece(
                    order = 2,
                    distanceMeters = 4000,
                    timeMillis = 900_000L
                ),
            ),
        )

        assertEquals("3×15 мин", repository.getTrainingsList().single().trainingWork)
    }

    @Test
    fun `erg training groups equal times and keeps plus separator`() = runTest {
        val repository = repository(
            training = training(type = TrainingPieceType.RP_3),
            pieces = listOf(
                piece(
                    order = 0,
                    distanceMeters = 1000,
                    timeMillis = 200_000L
                ),
                piece(
                    order = 1,
                    distanceMeters = 1000,
                    timeMillis = 200_000L
                ),
                piece(
                    order = 2,
                    distanceMeters = 1000,
                    timeMillis = 330_000L
                ),
            ),
        )

        assertEquals("2×3:20 + 5:30", repository.getTrainingsList().single().trainingWork)
    }

    @Test
    fun `boat training keeps distance summary`() = runTest {
        val repository = repository(
            training = training(type = TrainingPieceType.SINGLE),
            pieces = listOf(
                piece(
                    order = 0,
                    distanceMeters = 500,
                    timeMillis = 100_000L
                ),
                piece(
                    order = 1,
                    distanceMeters = 500,
                    timeMillis = 101_000L
                ),
                piece(
                    order = 2,
                    distanceMeters = 1000,
                    timeMillis = 210_000L
                ),
            ),
        )

        assertEquals("2×500м + 1000м", repository.getTrainingsList().single().trainingWork)
    }

    private fun repository(
        training: TrainingEntity,
        pieces: List<TrainingAthletePieceEntity>,
    ) = TrainingRepository(
        dao = FakeTrainingDao(
            training = training,
            pieces = pieces
        ),
        athletesRepository = StubAthletesRepository(),
        importStorage = FakeTrainingImportStorage(),
        chartReader = TrainingChartReader(),
    )

    private fun training(type: TrainingPieceType) = TrainingEntity(
        id = 1L,
        dateMillis = 0L,
        type = type,
    )

    private fun piece(
        order: Int,
        distanceMeters: Int,
        timeMillis: Long,
    ) = TrainingAthletePieceEntity(
        trainingId = 1L,
        athleteId = 10L,
        order = order,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        strokeRate = 30.0,
    )

    private class FakeTrainingDao(
        private val training: TrainingEntity,
        private val pieces: List<TrainingAthletePieceEntity>,
    ) : TrainingDao {

        override suspend fun insertTraining(training: TrainingEntity): Long = 0L

        override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) = Unit

        override suspend fun getAllTrainingEntities(): List<TrainingEntity> = listOf(training)

        override suspend fun getTrainingEntitiesByIds(trainingIds: List<Long>): List<TrainingEntity> =
            listOf(training).filter { it.id in trainingIds }

        override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
            training.takeIf { it.id == trainingId }

        override suspend fun getImportPathsByIds(trainingIds: List<Long>): List<String> = emptyList()

        override suspend fun deletePiecesForTrainings(trainingIds: List<Long>) = Unit

        override suspend fun deleteTrainings(trainingIds: List<Long>) = Unit

        override suspend fun updateImportMetadata(
            trainingId: Long,
            relativePath: String?,
        ) = Unit

        override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
            pieces.takeIf { trainingId == training.id }.orEmpty()

        override suspend fun getPiecesForTrainings(trainingIds: List<Long>): List<TrainingAthletePieceEntity> =
            pieces.takeIf { training.id in trainingIds }.orEmpty()

        override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> =
            getPiecesForTraining(trainingId).map { it.athleteId }
    }
}