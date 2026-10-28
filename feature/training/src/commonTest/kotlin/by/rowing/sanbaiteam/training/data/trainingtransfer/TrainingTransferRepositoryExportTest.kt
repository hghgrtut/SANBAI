package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.repository.StubAthletesRepository
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TrainingTransferRepositoryExportTest {

    @Test
    fun `selected export contains only requested trainings`() = runTest {
        val repository = TrainingTransferRepository(
            trainingDao = FakeTrainingDao(
                trainings = listOf(
                    training(
                        trainingId = 1L,
                        dateMillis = 100L,
                        athleteId = 10L
                    ),
                    training(
                        trainingId = 2L,
                        dateMillis = 200L,
                        athleteId = 20L
                    ),
                    training(
                        trainingId = 3L,
                        dateMillis = 300L,
                        athleteId = 10L
                    ),
                ),
            ),
            athletesRepository = StubAthletesRepository(),
            importStorage = FakeTrainingImportStorage(),
        )

        val backup = repository.exportSelectedBackup(trainingIds = setOf(1L, 3L))

        assertEquals(listOf(100L, 300L), backup.trainings.map { it.dateMillis })
        assertEquals(1, backup.athletes.size)
        assertEquals(listOf(0, 0), backup.trainings.map { it.pieces.single().athleteIndex })
    }

    @Test
    fun `export without matching trainings is empty`() = runTest {
        val training = training(
            trainingId = 1L,
            dateMillis = 100L,
            athleteId = 10L
        )
        val repository = TrainingTransferRepository(
            trainingDao = FakeTrainingDao(trainings = listOf(training)),
            athletesRepository = StubAthletesRepository(),
            importStorage = FakeTrainingImportStorage(),
        )

        val backup = repository.exportSelectedBackup(trainingIds = setOf(99L))

        assertEquals(0, backup.trainings.size)
        assertEquals(0, backup.athletes.size)
    }

    private fun training(
        trainingId: Long,
        dateMillis: Long,
        athleteId: Long,
    ) = TrainingWithPiece(
        training = TrainingEntity(
            id = trainingId,
            dateMillis = dateMillis,
        ),
        piece = TrainingAthletePieceEntity(
            trainingId = trainingId,
            athleteId = athleteId,
            order = 0,
            distanceMeters = 1000,
            timeMillis = 200_000L,
            strokeRate = 30.0,
        ),
    )

    private data class TrainingWithPiece(
        val training: TrainingEntity,
        val piece: TrainingAthletePieceEntity,
    )

    private class FakeTrainingDao(
        private val trainings: List<TrainingWithPiece>,
    ) : TrainingDao {

        override suspend fun insertTraining(training: TrainingEntity): Long = 0L

        override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) = Unit

        override suspend fun getAllTrainingEntities(): List<TrainingEntity> =
            trainings.map { it.training }

        override suspend fun getTrainingEntitiesByIds(trainingIds: List<Long>): List<TrainingEntity> =
            trainings
                .map { it.training }
                .filter { it.id in trainingIds }

        override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
            trainings.firstOrNull { it.training.id == trainingId }?.training

        override suspend fun getImportPathsByIds(trainingIds: List<Long>): List<String> = emptyList()

        override suspend fun deletePiecesForTrainings(trainingIds: List<Long>) = Unit

        override suspend fun deleteTrainings(trainingIds: List<Long>) = Unit

        override suspend fun updateImportMetadata(
            trainingId: Long,
            relativePath: String?,
        ) = Unit

        override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
            getPiecesForTrainings(trainingIds = listOf(trainingId))

        override suspend fun getPiecesForTrainings(trainingIds: List<Long>): List<TrainingAthletePieceEntity> =
            trainings.filter { it.training.id in trainingIds }.map { it.piece }

        override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> =
            getPiecesForTraining(trainingId).map { it.athleteId }
    }
}