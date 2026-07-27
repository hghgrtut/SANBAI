package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Entity(tableName = TrainingDao.TABLE_TRAINING)
internal data class TrainingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val type: TrainingPieceType = TrainingPieceType.SINGLE,
)
