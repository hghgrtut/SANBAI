package by.rowing.sanbaiteam.athlete.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import java.util.Date

@AllowDetektPublic
@Entity(tableName = AthleteDao.TABLE_NAME)
data class AthleteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateOfBirth: Date,
    val isMale: Boolean,
    val speedCoachSerial: String?,
)