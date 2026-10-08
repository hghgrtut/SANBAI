package by.rowing.sanbaiteam.athlete.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

@AllowDetektPublic
@Dao
interface AthletePersonalBestDao {

    @Query("SELECT * FROM $TABLE_NAME WHERE athleteId = :athleteId ORDER BY distanceMeters ASC")
    suspend fun getByAthlete(athleteId: Long): List<AthletePersonalBestEntity>

    @Query(
        """
        SELECT * FROM $TABLE_NAME
        WHERE athleteId IN (:athleteIds)
          AND boatType = :boatType
          AND distanceMeters = :distanceMeters
        """
    )
    suspend fun getBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): List<AthletePersonalBestEntity>

    @Upsert
    suspend fun upsertAll(bests: List<AthletePersonalBestEntity>)

    @Query("DELETE FROM $TABLE_NAME WHERE athleteId = :athleteId")
    suspend fun deleteAllForAthlete(athleteId: Long)

    @Transaction
    suspend fun replaceAllForAthlete(
        athleteId: Long,
        bests: List<AthletePersonalBestEntity>,
    ) {
        deleteAllForAthlete(athleteId)
        if (bests.isNotEmpty()) {
            upsertAll(bests)
        }
    }

    companion object {
        const val TABLE_NAME = "athlete_personal_best"
    }
}
