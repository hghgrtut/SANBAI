package by.rowing.sanbaiteam.athlete.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import java.util.Date

@Entity(tableName = AthleteDao.TABLE_NAME)
internal data class AthleteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateOfBirth: Date,
    val isMale: Boolean
)