package by.rowing.sanbaiteam.athlete.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AthleteDao {

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllAthletes(): Flow<List<AthleteEntity>>

    @Insert
    suspend fun addAthlete(athlete: AthleteEntity)

    companion object {

        const val TABLE_NAME = "athlete_entity"
    }
}