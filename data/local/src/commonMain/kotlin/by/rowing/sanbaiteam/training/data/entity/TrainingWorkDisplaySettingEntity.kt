package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.local.TrainingWorkDisplaySettingDao

/**
 * Выбранный пользователем режим отображения объёма работы для конкретного типа тренировки.
 * Отсутствие строки означает режим по умолчанию.
 */
@AllowDetektPublic
@Entity(tableName = TrainingWorkDisplaySettingDao.TABLE_TRAINING_WORK_DISPLAY_SETTING)
data class TrainingWorkDisplaySettingEntity(
    @PrimaryKey
    val pieceType: TrainingPieceType,
    val mode: TrainingWorkDisplayMode,
)
