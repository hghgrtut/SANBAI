package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.training.data.repository.FakeSavedTrainingFilterDao
import by.rowing.sanbaiteam.training.data.repository.SavedTrainingFiltersRepository
import by.rowing.sanbaiteam.training.presentation.filter.models.QuickFilterDialog
import by.rowing.sanbaiteam.training.presentation.filter.models.TrainingFiltersState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrainingFiltersControllerTest {

    @Test
    fun `apply quick filter merges preset and highlights chip`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filterId = repository.create(
            name = "Лактат",
            filter = TrainingFilter(lactateOnly = true),
        )
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.updateFilter(filter = TrainingFilter(athleteIds = setOf(3L)))
        controller.applyQuickFilter(filterId = filterId)

        assertEquals(
            TrainingFilter(
                athleteIds = setOf(3L),
                lactateOnly = true,
            ),
            state.filter,
        )
        assertEquals(filterId, state.activeQuickFilterId)
    }

    @Test
    fun `manual filter change resets quick filter highlight`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filterId = repository.create(
            name = "Лактат",
            filter = TrainingFilter(lactateOnly = true),
        )
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.applyQuickFilter(filterId = filterId)
        assertEquals(filterId, state.activeQuickFilterId)

        controller.toggleLactate()

        assertNull(state.activeQuickFilterId)
        assertTrue(state.filter.isEmpty)
    }

    @Test
    fun `save quick filter persists trimmed name and opens no dialog`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.updateFilter(filter = TrainingFilter(pieceDistanceRange = 500..2_000))
        controller.requestSaveQuickFilter()
        assertIs<QuickFilterDialog.Save>(state.quickFilterDialog)

        controller.saveQuickFilter(name = "  Дистанция  ")
        advanceUntilIdle()

        val savedFilter = state.savedFilters.single()
        assertEquals("Дистанция", savedFilter.name)
        assertEquals(TrainingFilter(pieceDistanceRange = 500..2_000), savedFilter.filter)
        assertNull(state.quickFilterDialog)
        assertEquals(savedFilter.id, state.activeQuickFilterId)
    }

    @Test
    fun `save with existing name asks for overwrite and updates payload`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        repository.create(
            name = "Сила",
            filter = TrainingFilter(athleteIds = setOf(9L)),
        )
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.updateFilter(filter = TrainingFilter(lactateOnly = true))
        controller.saveQuickFilter(name = "сила")

        val overwrite = state.quickFilterOverwrite
        assertEquals("сила", overwrite?.name)
        assertEquals(TrainingFilter(lactateOnly = true), overwrite?.filter)
        assertEquals(1, state.savedFilters.size)

        controller.confirmOverwrite()
        advanceUntilIdle()

        val savedFilter = state.savedFilters.single()
        assertEquals("Сила", savedFilter.name)
        assertEquals(TrainingFilter(lactateOnly = true), savedFilter.filter)
        assertNull(state.quickFilterOverwrite)
        assertEquals(savedFilter.id, state.activeQuickFilterId)
    }

    @Test
    fun `rename updates saved filter name`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filterId = repository.create(
            name = "Старое",
            filter = TrainingFilter(lactateOnly = true),
        )
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.openQuickFilterManage(filterId = filterId)
        assertEquals(QuickFilterDialog.Manage(filterId = filterId), state.quickFilterDialog)

        controller.renameQuickFilter(
            filterId = filterId,
            name = " Новое "
        )
        advanceUntilIdle()

        assertEquals("Новое", state.savedFilters.single().name)
        assertNull(state.quickFilterDialog)
    }

    @Test
    fun `rename to taken name is ignored`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        repository.create(
            name = "Первое",
            filter = TrainingFilter(lactateOnly = true)
        )
        val secondId = repository.create(
            name = "Второе",
            filter = TrainingFilter(athleteIds = setOf(1L)),
        )
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.openQuickFilterManage(filterId = secondId)
        controller.renameQuickFilter(
            filterId = secondId,
            name = "первое"
        )
        advanceUntilIdle()

        assertEquals(
            listOf("Первое", "Второе"),
            state.savedFilters.map { savedFilter -> savedFilter.name },
        )
        assertEquals(QuickFilterDialog.Manage(filterId = secondId), state.quickFilterDialog)
    }

    @Test
    fun `delete removes filter and clears its highlight`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filterId = repository.create(
            name = "Удаляемый",
            filter = TrainingFilter(lactateOnly = true),
        )
        repository.create(
            name = "Остаётся",
            filter = TrainingFilter(athleteIds = setOf(2L))
        )
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )
        controller.subscribe()
        advanceUntilIdle()

        controller.applyQuickFilter(filterId = filterId)
        controller.requestDeleteQuickFilter(filterId = filterId)
        assertEquals(filterId, state.quickFilterDeleteId)

        controller.confirmDeleteQuickFilter()
        advanceUntilIdle()

        assertEquals(listOf("Остаётся"), state.savedFilters.map { savedFilter -> savedFilter.name })
        assertNull(state.quickFilterDeleteId)
        assertNull(state.activeQuickFilterId)
    }

    @Test
    fun `save is ignored for empty filter`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        var state = TrainingFiltersState()
        val controller = controller(
            repository = repository,
            read = { state },
            write = { state = it },
        )

        controller.requestSaveQuickFilter()

        assertNull(state.quickFilterDialog)
    }

    private fun TestScope.controller(
        repository: SavedTrainingFiltersRepository,
        read: () -> TrainingFiltersState,
        write: (TrainingFiltersState) -> Unit,
    ): TrainingFiltersController = TrainingFiltersController(
        repository = repository,
        scope = backgroundScope,
        readFilters = read,
        writeFilters = write,
    )
}