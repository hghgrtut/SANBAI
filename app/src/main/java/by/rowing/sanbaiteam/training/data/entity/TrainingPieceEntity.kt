package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Entity(tableName = TrainingDao.TABLE_TRAINING_PIECE)
internal data class TrainingPieceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingId: Long,
    val order: Int,
    val type: TrainingPieceType,
    val duration: Long,
    val length: Long?
)