package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Entity(tableName = TrainingDao.TABLE_TRAINING_ATHLETE_PIECE)
data class TrainingAthletePieceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingId: Long,
    val athleteId: Long,
    val order: Int,
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
)
