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

    @Query("SELECT name FROM $TABLE_NAME WHERE id IN (:athleteIds)")
    suspend fun getAthleteNames(athleteIds: List<Long>): List<String>

    @Query("SELECT * FROM $TABLE_NAME WHERE id IN (:athleteIds)")
    suspend fun getAthletesByIds(athleteIds: List<Long>): List<AthleteEntity>

    @Query("SELECT * FROM $TABLE_NAME WHERE speedCoachSerial = :serial LIMIT 1")
    suspend fun getAthleteBySpeedCoachSerial(serial: String): AthleteEntity?

    companion object {
        const val TABLE_NAME = "athlete_entity"
    }
}
