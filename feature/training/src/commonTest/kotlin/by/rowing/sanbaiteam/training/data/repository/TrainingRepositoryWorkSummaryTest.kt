package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.FakeTrainingDao
import by.rowing.sanbaiteam.training.data.chart.TrainingChartReader
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.flow.first
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

        assertEquals("3×15 мин", repository.observeTrainingsList().first().single().trainingWork)
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

        assertEquals("2×3:20 + 5:30", repository.observeTrainingsList().first().single().trainingWork)
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

        assertEquals("2×500м + 1000м", repository.observeTrainingsList().first().single().trainingWork)
    }

    private fun repository(
        training: TrainingEntity,
        pieces: List<TrainingAthletePieceEntity>,
    ) = TrainingRepository(
        dao = FakeTrainingDao(
            trainings = listOf(training),
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
}