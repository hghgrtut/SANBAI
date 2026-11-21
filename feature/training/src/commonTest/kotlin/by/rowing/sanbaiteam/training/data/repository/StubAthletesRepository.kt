package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal open class StubAthletesRepository(bests: List<AthletePersonalBestEntity> = emptyList()) : AthletesRepository {

    var requestedBoatType: TrainingPieceType? = null
        private set
    var requestedDistanceMeters: Int? = null
        private set

    override val allAthletes: Flow<List<AthleteItemModel>> = emptyFlow()

    private var weights: List<AthleteWeightEntity> = emptyList()
    private var personalBests: MutableList<AthletePersonalBestEntity> = bests.toMutableList()

    override suspend fun getAllAthletesOnce(): List<AthleteEntity> = emptyList()

    override suspend fun addAthlete(athlete: AthleteEntity): Long = 1L

    override suspend fun deleteAthletes(athleteIds: List<Long>) = Unit

    override suspend fun getAthlete(athleteId: Long): AthleteEntity? = null

    override suspend fun updateAthlete(athlete: AthleteEntity) = Unit

    override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> =
        personalBests.filter { it.athleteId == athleteId }

    override suspend fun upsertPersonalBests(bests: List<AthletePersonalBestEntity>) {
        personalBests = bests.toMutableList()
    }

    override suspend fun replacePersonalBests(
        athleteId: Long,
        bests: List<AthletePersonalBestEntity>,
    ) = Unit

    override suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity> {
        requestedBoatType = boatType
        requestedDistanceMeters = distanceMeters
        return personalBests
            .filter {
                it.athleteId in athleteIds &&
                        it.boatType == boatType &&
                        it.distanceMeters == distanceMeters
            }
            .associateBy { it.athleteId }
    }

    override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> =
        athleteIds.associateWith { "Гребец $it" }

    override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = null

    override suspend fun getWeights(athleteId: Long): List<AthleteWeightEntity> = weights

    override suspend fun upsertWeights(weights: List<AthleteWeightEntity>) {
        this.weights = weights
    }

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
