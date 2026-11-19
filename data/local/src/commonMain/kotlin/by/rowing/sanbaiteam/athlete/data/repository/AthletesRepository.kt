package by.rowing.sanbaiteam.athlete.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.flow.Flow

@AllowDetektPublic
interface AthletesRepository {

    val allAthletes: Flow<List<AthleteItemModel>>

    suspend fun getAllAthletesOnce(): List<AthleteEntity>

    suspend fun addAthlete(athlete: AthleteEntity): Long

    suspend fun deleteAthletes(athleteIds: List<Long>)

    suspend fun getAthlete(athleteId: Long): AthleteEntity?

    suspend fun updateAthlete(athlete: AthleteEntity)

    suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity>

    suspend fun upsertPersonalBests(bests: List<AthletePersonalBestEntity>)

    suspend fun replacePersonalBests(
        athleteId: Long,
        bests: List<AthletePersonalBestEntity>,
    )

    suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity>

    suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String>

    suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long?

    suspend fun getWeights(athleteId: Long): List<AthleteWeightEntity>

    suspend fun upsertWeights(weights: List<AthleteWeightEntity>)

    suspend fun saveWeight(
        athleteId: Long,
        dateMillis: Long,
        weightTenths: Int,
    )

    suspend fun deleteWeight(
        athleteId: Long,
        dateMillis: Long,
    )
}