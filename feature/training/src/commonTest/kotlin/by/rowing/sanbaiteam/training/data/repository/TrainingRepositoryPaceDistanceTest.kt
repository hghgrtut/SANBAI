package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TrainingRepositoryPaceDistanceTest {

    @Test
    fun `bike trainings show pace per 1000 meters`() = runTest {
        listOf(TrainingPieceType.CONCEPT_BIKE_ERG, TrainingPieceType.WATT_BIKE).forEach { type ->
            val repository = testTrainingRepository(
                trainings = listOf(training(type)),
                pieces = listOf(piece()),
            )

            val piece = repository.getTrainingDetail(TRAINING_ID).pieces.single()

            assertEquals(1000, piece.paceDistanceMeters)
            assertEquals("3:20.0 /1000м", piece.paceFormatted)
        }
    }

    @Test
    fun `single keeps pace per 500 meters`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(training(TrainingPieceType.SINGLE)),
            pieces = listOf(piece()),
        )

        val piece = repository.getTrainingDetail(TRAINING_ID).pieces.single()

        assertEquals(500, piece.paceDistanceMeters)
        assertEquals("1:40.0 /500м", piece.paceFormatted)
    }

    private fun training(type: TrainingPieceType) = TrainingEntity(
        id = TRAINING_ID,
        dateMillis = 1L,
        type = type,
    )

    private fun piece() = TrainingAthletePieceEntity(
        trainingId = TRAINING_ID,
        athleteId = 10L,
        order = 0,
        distanceMeters = 1000,
        timeMillis = 200_000L,
        strokeRate = 20.0,
    )

    companion object {

        private const val TRAINING_ID = 1L
    }
}