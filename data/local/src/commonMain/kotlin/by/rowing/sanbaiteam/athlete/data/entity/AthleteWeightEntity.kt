package by.rowing.sanbaiteam.athlete.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import by.rowing.sanbaiteam.athlete.data.local.AthleteWeightDao
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
@Entity(
    tableName = AthleteWeightDao.TABLE_NAME,
    primaryKeys = ["athleteId", "dateMillis"],
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["athleteId"]), ]
)
data class AthleteWeightEntity(
    val athleteId: Long,
    val dateMillis: Long,
    val weightTenths: Int,
)