package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.local.SavedTrainingFilterDao

/**
 * Сохранённый пользователем быстрый фильтр тренировок.
 *
 * Снимок фильтра хранится JSON-строкой, чтобы слой данных не зависел от
 * presentation-модели фильтра.
 */
@AllowDetektPublic
@Entity(tableName = SavedTrainingFilterDao.TABLE_SAVED_TRAINING_FILTER)
data class SavedTrainingFilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val payload: String,
    val createdAtMillis: Long,
)