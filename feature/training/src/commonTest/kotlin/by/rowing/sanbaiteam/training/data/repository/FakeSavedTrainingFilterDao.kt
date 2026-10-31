package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.SavedTrainingFilterEntity
import by.rowing.sanbaiteam.training.data.local.SavedTrainingFilterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSavedTrainingFilterDao : SavedTrainingFilterDao {

    val entities = MutableStateFlow<List<SavedTrainingFilterEntity>>(emptyList())

    private var nextId = 1L

    override fun getAll(): Flow<List<SavedTrainingFilterEntity>> = entities

    override suspend fun insert(entity: SavedTrainingFilterEntity): Long {
        val id = nextId++
        entities.value += entity.copy(id = id)
        return id
    }

    override suspend fun updatePayload(
        id: Long,
        payload: String
    ) {
        entities.value = entities.value.map { entity ->
            if (entity.id == id) entity.copy(payload = payload) else entity
        }
    }

    override suspend fun rename(
        id: Long,
        name: String
    ) {
        entities.value = entities.value.map { entity ->
            if (entity.id == id) entity.copy(name = name) else entity
        }
    }

    override suspend fun delete(id: Long) {
        entities.value = entities.value.filterNot { entity -> entity.id == id }
    }
}