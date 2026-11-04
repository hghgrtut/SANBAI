package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.training.data.FakeTrainingDao
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.repository.StubAthletesRepository
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TrainingTransferRepositoryImportTest {

    @Test
    fun `bike erg backup restores into concept2 storage`() = runTest {
        val storage = FakeTrainingImportStorage()
        val repository = TrainingTransferRepository(
            trainingDao = FakeTrainingDao(),
            athletesRepository = StubAthletesRepository(),
            importStorage = storage,
        )

        repository.importBackup(backup(type = TrainingPieceType.CONCEPT_BIKE_ERG))

        assertEquals(listOf("concept2_imports/1_unknown.fit"), storage.savedFiles.keys.toList())
    }

    @Test
    fun `row erg backup restores into concept2 storage`() = runTest {
        val storage = FakeTrainingImportStorage()
        val repository = TrainingTransferRepository(
            trainingDao = FakeTrainingDao(),
            athletesRepository = StubAthletesRepository(),
            importStorage = storage,
        )

        repository.importBackup(backup(type = TrainingPieceType.CONCEPT_ROW_ERG))

        assertEquals(listOf("concept2_imports/1_unknown.fit"), storage.savedFiles.keys.toList())
    }

    private fun backup(type: TrainingPieceType): TrainingBackup = TrainingBackup(
        athletes = listOf(
            BackupAthlete(
                name = "Тест",
                dateOfBirthMillis = 0L,
                isMale = true,
            )
        ),
        trainings = listOf(
            BackupTraining(
                dateMillis = 1L,
                type = type.name,
                sourceFileBase64 = Base64.encode(byteArrayOf(1, 2, 3)),
                sourceFileExtension = "fit",
                pieces = listOf(
                    BackupPiece(
                        athleteIndex = 0,
                        order = 0,
                        distanceMeters = 1000,
                        timeMillis = 200_000L,
                        strokeRate = 65.0,
                        sourceFileIndex = 0,
                        sourceIntervalIndex = 0,
                    )
                ),
            )
        ),
    )
}