package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.SavedTrainingFilterEntity
import by.rowing.sanbaiteam.training.data.filter.SavedTrainingFilterSerializer
import by.rowing.sanbaiteam.training.data.local.SavedTrainingFilterDao
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SavedTrainingFiltersRepositoryTest {

    @Test
    fun `create maps saved entity to model`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filter = TrainingFilter(athleteIds = setOf(1L))

        val filterId = repository.create(name = "Тест", filter = filter)
        val savedFilters = repository.savedFilters.first()

        assertEquals(1, savedFilters.size)
        assertEquals(filterId, savedFilters.first().id)
        assertEquals("Тест", savedFilters.first().name)
        assertEquals(filter, savedFilters.first().filter)
    }

    @Test
    fun `overwrite replaces payload but keeps name`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filterId = repository.create(name = "Тест", filter = TrainingFilter(lactateOnly = true))

        repository.overwrite(id = filterId, filter = TrainingFilter(athleteIds = setOf(7L)))
        val savedFilter = repository.savedFilters.first().single()

        assertEquals("Тест", savedFilter.name)
        assertEquals(TrainingFilter(athleteIds = setOf(7L)), savedFilter.filter)
    }

    @Test
    fun `rename and delete update stored filters`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filterId = repository.create(name = "Старое", filter = TrainingFilter())

        repository.rename(id = filterId, name = "Новое")
        assertEquals("Новое", repository.savedFilters.first().single().name)

        repository.delete(id = filterId)
        assertTrue(repository.savedFilters.first().isEmpty())
    }

    @Test
    fun `entity with broken payload is skipped`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        dao.insert(
            SavedTrainingFilterEntity(
                name = "Битый",
                payload = "not a json",
                createdAtMillis = 0L,
            )
        )
        val repository = SavedTrainingFiltersRepository(dao = dao)

        assertTrue(repository.savedFilters.first().isEmpty())
    }

    @Test
    fun `saved filter stores decoded snapshot`() = runTest {
        val dao = FakeSavedTrainingFilterDao()
        val repository = SavedTrainingFiltersRepository(dao = dao)
        val filter = TrainingFilter(pieceDistanceRange = 500..2_000)

        val filterId = repository.create(name = "Дистанция", filter = filter)
        val entity = dao.entities.value.single { it.id == filterId }

        assertEquals(
            filter,
            SavedTrainingFilterSerializer.decode(entity.payload),
        )
    }
}

private class FakeSavedTrainingFilterDao : SavedTrainingFilterDao {

    val entities = MutableStateFlow<List<SavedTrainingFilterEntity>>(emptyList())

    private var nextId = 1L

    override fun getAll(): Flow<List<SavedTrainingFilterEntity>> = entities

    override suspend fun insert(entity: SavedTrainingFilterEntity): Long {
        val id = nextId++
        entities.value = entities.value + entity.copy(id = id)
        return id
    }

    override suspend fun updatePayload(id: Long, payload: String) {
        entities.value = entities.value.map { entity ->
            if (entity.id == id) entity.copy(payload = payload) else entity
        }
    }

    override suspend fun rename(id: Long, name: String) {
        entities.value = entities.value.map { entity ->
            if (entity.id == id) entity.copy(name = name) else entity
        }
    }

    override suspend fun delete(id: Long) {
        entities.value = entities.value.filterNot { entity -> entity.id == id }
    }
}
