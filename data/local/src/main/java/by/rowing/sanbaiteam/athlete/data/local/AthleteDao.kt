package by.rowing.sanbaiteam.athlete.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.coroutines.flow.Flow

@AllowDetektPublic
@Dao
interface AthleteDao {

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllAthletes(): Flow<List<AthleteEntity>>

    @Insert
    suspend fun addAthlete(athlete: AthleteEntity): Long

    @Query("SELECT name FROM $TABLE_NAME WHERE id IN (:athleteIds)")
    suspend fun getAthleteNames(athleteIds: List<Long>): List<String>

    @Query("SELECT * FROM $TABLE_NAME WHERE id IN (:athleteIds)")
    suspend fun getAthletesByIds(athleteIds: List<Long>): List<AthleteEntity>

    @Query("SELECT * FROM $TABLE_NAME WHERE speedCoachSerial = :serial LIMIT 1")
    suspend fun getAthleteBySpeedCoachSerial(serial: String): AthleteEntity?

    @Query("SELECT * FROM $TABLE_NAME WHERE id = :athleteId LIMIT 1")
    suspend fun getById(athleteId: Long): AthleteEntity?

    @Update
    suspend fun updateAthlete(athlete: AthleteEntity)

    companion object {
        const val TABLE_NAME = "athlete_entity"
    }
}
