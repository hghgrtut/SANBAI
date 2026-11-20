package by.rowing.sanbaiteam.athlete.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
@Dao
interface AthleteWeightDao {

    @Query("SELECT * FROM $TABLE_NAME WHERE athleteId = :athleteId ORDER BY dateMillis ASC")
    suspend fun getByAthlete(athleteId: Long): List<AthleteWeightEntity>

    @Upsert
    suspend fun upsert(weight: AthleteWeightEntity)

    @Upsert
    suspend fun upsertAll(weights: List<AthleteWeightEntity>)

    @Query("DELETE FROM $TABLE_NAME WHERE athleteId = :athleteId AND dateMillis = :dateMillis")
    suspend fun deleteByDate(
        athleteId: Long,
        dateMillis: Long,
    )

    companion object {

        internal const val TABLE_NAME = "athlete_weight"
    }
}