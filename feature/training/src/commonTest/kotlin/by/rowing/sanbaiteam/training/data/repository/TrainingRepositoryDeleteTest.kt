package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.chart.TrainingChartReader
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
            csvPathsByIds = mapOf(
                1L to "speedcoach_imports/1_a.csv",
                2L to null,
                3L to "rp3_imports/3_c.csv",
            )
        )
        val storage = FakeTrainingImportStorage()
        val repository = TrainingRepository(
            dao = dao,
            athletesRepository = StubAthletesRepository(),
            importStorage = storage,
            chartReader = TrainingChartReader(),
        )

        repository.deleteTrainings(listOf(1L, 3L))

        assertEquals(listOf(listOf(1L, 3L)), dao.deletedIds)
        assertEquals(
            listOf("speedcoach_imports/1_a.csv", "rp3_imports/3_c.csv"),
            storage.deletedPaths
        )
    }

    @Test
    fun `delete with empty ids does not touch database`() = runTest {
        val dao = RecordingTrainingDao(csvPathsByIds = emptyMap())
        val repository = TrainingRepository(
            dao = dao,
            athletesRepository = StubAthletesRepository(),
            importStorage = FakeTrainingImportStorage(),
            chartReader = TrainingChartReader(),
        )

        repository.deleteTrainings(emptyList())

        assertTrue(dao.deletedIds.isEmpty())
    }

    private class RecordingTrainingDao(
        private val csvPathsByIds: Map<Long, String?>,
    ) : TrainingDao {

        val deletedIds = mutableListOf<List<Long>>()

        override suspend fun insertTraining(training: TrainingEntity): Long = 1L

        override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) = Unit

        override suspend fun getAllTrainingEntities(): List<TrainingEntity> =
            csvPathsByIds.map { (id, csvPath) ->
                TrainingEntity(
                    id = id,
                    dateMillis = id,
                    sourceFileRelativePath = csvPath,
                )
            }

        override suspend fun getTrainingEntitiesByIds(trainingIds: List<Long>): List<TrainingEntity> =
            getAllTrainingEntities().filter { it.id in trainingIds }

        override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
            csvPathsByIds[trainingId]?.let { csvPath ->
                TrainingEntity(
                    id = trainingId,
                    dateMillis = trainingId,
                    sourceFileRelativePath = csvPath,
                )
            }

        override suspend fun getImportPathsByIds(trainingIds: List<Long>): List<String> =
            trainingIds.mapNotNull { csvPathsByIds[it] }

        override suspend fun deleteTrainingsWithPieces(trainingIds: List<Long>) {
            if (trainingIds.isEmpty()) return
            deletedIds += trainingIds
        }

        override suspend fun deletePiecesForTrainings(trainingIds: List<Long>) {}

        override suspend fun deleteTrainings(trainingIds: List<Long>) {}

        override suspend fun updateImportMetadata(
            trainingId: Long,
            relativePath: String?,
        ) = Unit

        override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
            emptyList()

        override suspend fun getPiecesForTrainings(trainingIds: List<Long>): List<TrainingAthletePieceEntity> =
            emptyList()

        override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> = emptyList()
    }
}