package by.rowing.sanbaiteam.training.presentation.comparison

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TrainingComparisonSelectionStoreTest {

    @Test
    fun `selection excludes current training and removes duplicates`() {
        val store = TrainingComparisonSelectionStore()

        store.select(
            trainingId = 1L,
            comparedTrainingIds = listOf(3L, 2L, 1L, 2L),
        )

        assertEquals(setOf(2L, 3L), store.idsFor(1L))
    }

    @Test
    fun `selection belongs to the single active training`() {
        val store = TrainingComparisonSelectionStore()

        store.select(trainingId = 1L, comparedTrainingIds = listOf(2L))
        store.select(trainingId = 2L, comparedTrainingIds = listOf(3L))

        assertEquals(emptySet<Long>(), store.idsFor(1L))
        assertEquals(setOf(3L), store.idsFor(2L))
    }

    @Test
    fun `clear ignores selection of another training`() {
        val store = TrainingComparisonSelectionStore()
        store.select(trainingId = 1L, comparedTrainingIds = listOf(2L))

        store.clear(2L)

        assertEquals(setOf(2L), store.idsFor(1L))
    }

    @Test
    fun `empty selection clears the store`() {
        val store = TrainingComparisonSelectionStore()
        store.select(trainingId = 1L, comparedTrainingIds = listOf(2L))

        store.select(trainingId = 1L, comparedTrainingIds = emptyList())

        assertEquals(emptySet<Long>(), store.idsFor(1L))
    }

    private fun TrainingComparisonSelectionStore.idsFor(trainingId: Long): Set<Long> =
        selection.value.comparedTrainingIdsFor(trainingId)
}
