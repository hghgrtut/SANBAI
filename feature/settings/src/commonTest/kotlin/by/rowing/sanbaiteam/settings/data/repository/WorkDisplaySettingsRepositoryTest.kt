package by.rowing.sanbaiteam.settings.data.repository

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplaySettingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingWorkDisplaySettingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class WorkDisplaySettingsRepositoryTest {

    @Test
    fun `uses defaults for every training type`() = runTest {
        val repository = WorkDisplaySettingsRepository(dao = FakeDao())

        val modes = repository.modes.first()

        assertEquals(TrainingPieceType.entries.toSet(), modes.keys)
        TrainingPieceType.entries.forEach { type ->
            assertEquals(
                if (type.isWater) TrainingWorkDisplayMode.METERS else TrainingWorkDisplayMode.MINUTES,
                modes.getValue(type),
            )
        }
    }

    @Test
    fun `stores selected mode and keeps other defaults`() = runTest {
        val repository = WorkDisplaySettingsRepository(dao = FakeDao())

        repository.setMode(
            pieceType = TrainingPieceType.SINGLE,
            mode = TrainingWorkDisplayMode.MINUTES,
        )
        repository.setMode(
            pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
            mode = TrainingWorkDisplayMode.METERS,
        )

        val modes = repository.modes.first()
        assertEquals(TrainingWorkDisplayMode.MINUTES, modes.getValue(TrainingPieceType.SINGLE))
        assertEquals(TrainingWorkDisplayMode.METERS, modes.getValue(TrainingPieceType.CONCEPT_ROW_ERG))
        assertEquals(TrainingWorkDisplayMode.METERS, modes.getValue(TrainingPieceType.DOUBLE))
        assertEquals(TrainingWorkDisplayMode.MINUTES, modes.getValue(TrainingPieceType.RP_3))
    }

    @Test
    fun `repeated selection replaces the stored value`() = runTest {
        val repository = WorkDisplaySettingsRepository(dao = FakeDao())

        repository.setMode(
            pieceType = TrainingPieceType.EIGHT,
            mode = TrainingWorkDisplayMode.MINUTES,
        )
        repository.setMode(
            pieceType = TrainingPieceType.EIGHT,
            mode = TrainingWorkDisplayMode.METERS,
        )

        assertEquals(
            TrainingWorkDisplayMode.METERS,
            repository.modes.first().getValue(TrainingPieceType.EIGHT),
        )
    }

    private class FakeDao : TrainingWorkDisplaySettingDao {

        private val flow = MutableStateFlow<List<TrainingWorkDisplaySettingEntity>>(emptyList())

        override fun observeAll(): Flow<List<TrainingWorkDisplaySettingEntity>> = flow.asStateFlow()

        override suspend fun upsert(entity: TrainingWorkDisplaySettingEntity) {
            flow.value = flow.value.filterNot { it.pieceType == entity.pieceType } + entity
        }
    }
}