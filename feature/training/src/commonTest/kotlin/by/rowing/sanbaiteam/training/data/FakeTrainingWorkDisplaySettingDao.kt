package by.rowing.sanbaiteam.training.data

import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplaySettingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingWorkDisplaySettingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeTrainingWorkDisplaySettingDao(
    settings: List<TrainingWorkDisplaySettingEntity> = emptyList(),
) : TrainingWorkDisplaySettingDao {

    private val settingsFlow = MutableStateFlow(settings)

    override fun observeAll(): Flow<List<TrainingWorkDisplaySettingEntity>> = settingsFlow.asStateFlow()

    override suspend fun upsert(entity: TrainingWorkDisplaySettingEntity) {
        settingsFlow.value = settingsFlow.value.filterNot { it.pieceType == entity.pieceType } + entity
    }
}
