package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TrainingRepositoryDeleteTest {

    @Test
    fun `delete removes pieces trainings and stored csv files`() = runTest {
        val dao = RecordingTrainingDao(
            trainings = listOf(
                TrainingEntity(
                    id = 1L,
                    dateMillis = 1L,
                    sourceCsvRelativePath = "speedcoach_imports/1_a.csv"
                ),
                TrainingEntity(
                    id = 2L,
                    dateMillis = 2L,
                    sourceCsvRelativePath = null
                ),
                TrainingEntity(
                    id = 3L,
                    dateMillis = 3L,
                    sourceCsvRelativePath = "rp3_imports/3_c.csv"
                ),
            )
        )
        val storage = FakeTrainingImportStorage()
        val repository = TrainingRepository(
            dao = dao,
            athletesRepository = StubAthletesRepository(),
            importStorage = storage,
        )

        repository.deleteTrainings(listOf(1L, 3L))

        assertEquals(listOf(listOf(1L, 3L)), dao.deletedPieceIds)
        assertEquals(listOf(listOf(1L, 3L)), dao.deletedTrainingIds)
        assertEquals(
            listOf("speedcoach_imports/1_a.csv", "rp3_imports/3_c.csv"),
            storage.deletedPaths
        )
    }

    @Test
    fun `delete with empty ids does not touch database`() = runTest {
        val dao = RecordingTrainingDao(trainings = emptyList())
        val repository = TrainingRepository(
            dao = dao,
            athletesRepository = StubAthletesRepository(),
            importStorage = FakeTrainingImportStorage(),
        )

        repository.deleteTrainings(emptyList())

        assertTrue(dao.deletedPieceIds.isEmpty())
        assertTrue(dao.deletedTrainingIds.isEmpty())
    }

    private class RecordingTrainingDao(
        private val trainings: List<TrainingEntity>,
    ) : TrainingDao {

        val deletedPieceIds = mutableListOf<List<Long>>()
        val deletedTrainingIds = mutableListOf<List<Long>>()

        override suspend fun insertTraining(training: TrainingEntity): Long = 1L

        override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) = Unit

        override suspend fun getAllTrainingEntities(): List<TrainingEntity> = trainings

        override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
            trainings.firstOrNull { it.id == trainingId }

        override suspend fun getTrainingEntitiesByIds(trainingIds: List<Long>): List<TrainingEntity> =
            trainings.filter { it.id in trainingIds }

        override suspend fun deletePiecesForTrainings(trainingIds: List<Long>) {
            deletedPieceIds += trainingIds
        }

        override suspend fun deleteTrainings(trainingIds: List<Long>) {
            deletedTrainingIds += trainingIds
        }

        override suspend fun updateImportMetadata(
            trainingId: Long,
            relativePath: String?,
            deviceSerial: String?,
            sessionName: String?,
        ) = Unit

        override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
            emptyList()

        override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> = emptyList()
    }
}
