package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Entity(tableName = TrainingDao.TABLE_ATHLETE_WORK)
internal data class TrainingAthleteWork(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingPieceId: Long,
    val seat: Int?,
    val maxPulse: Int?,
    val averagePulse: Int?,
    val restPulse: Int?,
    val lactate: Double?
)