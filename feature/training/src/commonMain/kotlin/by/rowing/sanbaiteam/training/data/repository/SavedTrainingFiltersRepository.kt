package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.SavedTrainingFilterEntity
import by.rowing.sanbaiteam.training.data.filter.SavedTrainingFilter
import by.rowing.sanbaiteam.training.data.filter.SavedTrainingFilterSerializer
import by.rowing.sanbaiteam.training.data.local.SavedTrainingFilterDao
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SavedTrainingFiltersRepository(private val dao: SavedTrainingFilterDao) {

    val savedFilters: Flow<List<SavedTrainingFilter>> = dao.getAll().map { entities ->
        entities.mapNotNull { entity -> entity.toModel() }
    }

    suspend fun create(
        name: String,
        filter: TrainingFilter
    ): Long = dao.insert(
        SavedTrainingFilterEntity(
            name = name,
            payload = SavedTrainingFilterSerializer.encode(filter),
            createdAtMillis = nowInMillis(),
        )
    )

    suspend fun overwrite(
        id: Long,
        filter: TrainingFilter
    ) {
        dao.updatePayload(
            id = id,
            payload = SavedTrainingFilterSerializer.encode(filter),
        )
    }

    suspend fun rename(
        id: Long,
        name: String
    ) {
        dao.rename(
            id = id,
            name = name
        )
    }

    suspend fun delete(id: Long) {
        dao.delete(id = id)
    }

    private fun SavedTrainingFilterEntity.toModel(): SavedTrainingFilter? = SavedTrainingFilter(
        id = id,
        name = name,
        filter = SavedTrainingFilterSerializer.decode(payload) ?: return null,
        createdAtMillis = createdAtMillis,
    )
}