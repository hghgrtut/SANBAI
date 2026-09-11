package by.rowing.sanbaiteam.athlete.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.flow.Flow

interface AthletesRepository {

    val allAthletes: Flow<List<AthleteItemModel>>

    fun addAthlete(athlete: AthleteEntity)

    suspend fun getAthlete(athleteId: Long): AthleteEntity?

    suspend fun updateAthlete(athlete: AthleteEntity)

    suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity>

    suspend fun replacePersonalBestsForBoatType(
        athleteId: Long,
        boatType: TrainingPieceType,
        bests: List<AthletePersonalBestEntity>,
    )

    suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity>

    suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String>

    suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long?
}