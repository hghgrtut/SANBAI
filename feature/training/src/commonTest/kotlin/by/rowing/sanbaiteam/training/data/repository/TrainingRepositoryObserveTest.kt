package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.FakeTrainingDao
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TrainingRepositoryObserveTest {

    @Test
    fun `list emits again when trainings change in database`() = runTest {
        val dao = FakeTrainingDao(
            trainings = listOf(training(id = 1L)),
            pieces = listOf(piece()),
        )
        val repository = testTrainingRepository(dao = dao)

        val emitted = repository.observeTrainingsList()
            .onEach { items ->
                if (items.isNotEmpty()) {
                    dao.updateTrainings(emptyList())
                }
            }
            .take(count = 2)
            .toList()

        assertEquals(listOf(1L), emitted.first().map { item -> item.trainingId })
        assertTrue(emitted.last().isEmpty())
    }

    private fun training(id: Long): TrainingEntity = TrainingEntity(
        id = id,
        dateMillis = id,
        type = TrainingPieceType.SINGLE,
    )

    private fun piece(): TrainingAthletePieceEntity = TrainingAthletePieceEntity(
        trainingId = 1L,
        athleteId = 10L,
        order = 0,
        distanceMeters = 500,
        timeMillis = 90_000L,
        strokeRate = 30.0,
    )
}