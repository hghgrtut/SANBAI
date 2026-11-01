package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.training.data.FakeTrainingDao
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.repository.StubAthletesRepository
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TrainingTransferRepositoryExportTest {

    @Test
    fun `selected export contains only requested trainings`() = runTest {
        val repository = TrainingTransferRepository(
            trainingDao = dao(
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
            athletesRepository = StubAthletesRepository(),
            importStorage = FakeTrainingImportStorage(),
        )

        val backup = repository.exportSelectedBackup(trainingIds = setOf(1L, 3L))

        assertEquals(listOf(300L, 100L), backup.trainings.map { it.dateMillis })
        assertEquals(1, backup.athletes.size)
        assertEquals(listOf(0, 0), backup.trainings.map { it.pieces.single().athleteIndex })
    }

    @Test
    fun `export without matching trainings is empty`() = runTest {
        val trainingWithPiece = training(
            trainingId = 1L,
            dateMillis = 100L,
            athleteId = 10L
        )
        val repository = TrainingTransferRepository(
            trainingDao = dao(trainingWithPiece),
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
    ): Pair<TrainingEntity, TrainingAthletePieceEntity> = TrainingEntity(
        id = trainingId,
        dateMillis = dateMillis,
    ) to TrainingAthletePieceEntity(
        trainingId = trainingId,
        athleteId = athleteId,
        order = 0,
        distanceMeters = 1000,
        timeMillis = 200_000L,
        strokeRate = 30.0,
    )

    private fun dao(vararg trainings: Pair<TrainingEntity, TrainingAthletePieceEntity>) = FakeTrainingDao(
        trainings = trainings.map { it.first },
        pieces = trainings.map { it.second },
    )
}