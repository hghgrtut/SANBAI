package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal open class StubAthletesRepository : AthletesRepository {

    override val allAthletes: Flow<List<AthleteItemModel>> = emptyFlow()

    private var weights: List<AthleteWeightEntity> = emptyList()

    override suspend fun getAllAthletesOnce(): List<AthleteEntity> = emptyList()

    override suspend fun addAthlete(athlete: AthleteEntity): Long = 1L

    override suspend fun getAthlete(athleteId: Long): AthleteEntity? = null

    override suspend fun updateAthlete(athlete: AthleteEntity) = Unit

    override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> = emptyList()

    override suspend fun replacePersonalBests(
        athleteId: Long,
        bests: List<AthletePersonalBestEntity>,
    ) = Unit

    override suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity> = emptyMap()

    override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> =
        athleteIds.associateWith { "Гребец $it" }

    override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = null

    override suspend fun getWeights(athleteId: Long): List<AthleteWeightEntity> = weights

    override suspend fun saveWeight(
        athleteId: Long,
        dateMillis: Long,
        weightTenths: Int
    ) {
        weights += AthleteWeightEntity(
            athleteId = athleteId,
            dateMillis = dateMillis,
            weightTenths = weightTenths,
        )
    }

    override suspend fun deleteWeight(
        athleteId: Long,
        dateMillis: Long
    ) {
        weights = weights.filter { it.athleteId != athleteId || it.dateMillis != dateMillis }
    }
}