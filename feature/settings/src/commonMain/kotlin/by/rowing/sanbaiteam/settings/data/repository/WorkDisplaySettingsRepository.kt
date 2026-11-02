package by.rowing.sanbaiteam.settings.data.repository

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplaySettingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingWorkDisplaySettingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Хранит выбранный пользователем режим отображения объёма работы.
 *
 * Отдаёт полную карту по всем типам тренировок: для типов без сохранённого
 * значения подставляется режим по умолчанию.
 */
@AllowDetektPublic
class WorkDisplaySettingsRepository(private val dao: TrainingWorkDisplaySettingDao) {

    val modes: Flow<Map<TrainingPieceType, TrainingWorkDisplayMode>> = dao.observeAll().map { entities ->
        val saved = entities.associate { entity -> entity.pieceType to entity.mode }
        TrainingPieceType.entries.associateWith { type ->
            saved[type] ?: (if (type.isWater) TrainingWorkDisplayMode.METERS else TrainingWorkDisplayMode.MINUTES)
        }
    }

    suspend fun setMode(
        pieceType: TrainingPieceType,
        mode: TrainingWorkDisplayMode,
    ) {
        dao.upsert(
            TrainingWorkDisplaySettingEntity(
                pieceType = pieceType,
                mode = mode,
            )
        )
    }
}