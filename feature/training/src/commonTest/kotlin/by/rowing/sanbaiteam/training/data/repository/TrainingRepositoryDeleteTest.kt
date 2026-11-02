package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.FakeTrainingDao
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TrainingRepositoryDeleteTest {

    @Test
    fun `delete removes pieces trainings and stored csv files`() = runTest {
        val dao = FakeTrainingDao(
            trainings = listOf(
                training(
                    id = 1L,
                    csvPath = "speedcoach_imports/1_a.csv"
                ),
                training(
                    id = 2L,
                    csvPath = null
                ),
                training(
                    id = 3L,
                    csvPath = "rp3_imports/3_c.csv"
                ),
            )
        )
        val storage = FakeTrainingImportStorage()
        val repository = testTrainingRepository(
            dao = dao,
            importStorage = storage,
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
        val dao = FakeTrainingDao()
        val repository = testTrainingRepository(dao = dao)

        repository.deleteTrainings(emptyList())

        assertTrue(dao.deletedIds.isEmpty())
    }

    private fun training(
        id: Long,
        csvPath: String?,
    ) = TrainingEntity(
        id = id,
        dateMillis = id,
        sourceFileRelativePath = csvPath,
    )
}