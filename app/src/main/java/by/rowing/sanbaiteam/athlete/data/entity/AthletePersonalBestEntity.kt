package by.rowing.sanbaiteam.athlete.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.athlete.data.local.AthletePersonalBestDao
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

@Entity(
    tableName = AthletePersonalBestDao.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["athleteId", "boatType", "distanceMeters"], unique = true),
        Index(value = ["athleteId"]),
    ]
)
internal data class AthletePersonalBestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val athleteId: Long,
    val boatType: TrainingPieceType,
    val distanceMeters: Int,
    val timeMillis: Long,
)
