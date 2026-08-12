package by.rowing.sanbaiteam.athlete.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

@Dao
internal interface AthletePersonalBestDao {

    @Query("SELECT * FROM $TABLE_NAME WHERE athleteId = :athleteId ORDER BY distanceMeters ASC")
    suspend fun getByAthlete(athleteId: Long): List<AthletePersonalBestEntity>

    @Query(
        """
        SELECT * FROM $TABLE_NAME
        WHERE athleteId = :athleteId
          AND boatType = :boatType
          AND distanceMeters = :distanceMeters
        LIMIT 1
        """
    )
    suspend fun getBest(
        athleteId: Long,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): AthletePersonalBestEntity?

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
    suspend fun upsert(best: AthletePersonalBestEntity)

    @Upsert
    suspend fun upsertAll(bests: List<AthletePersonalBestEntity>)

    @Delete
    suspend fun delete(best: AthletePersonalBestEntity)

    @Query("DELETE FROM $TABLE_NAME WHERE athleteId = :athleteId AND boatType = :boatType")
    suspend fun deleteAllForAthleteBoatType(athleteId: Long, boatType: TrainingPieceType)

    companion object {
        const val TABLE_NAME = "athlete_personal_best"
    }
}
