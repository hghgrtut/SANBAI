package by.rowing.sanbaiteam.athlete.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.athlete.data.local.AthletePersonalBestDao
import by.rowing.sanbaiteam.athlete.data.local.AthleteWeightDao
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class AthletesRepositoryImpl(
    private val dao: AthleteDao,
    private val personalBestDao: AthletePersonalBestDao,
    private val weightDao: AthleteWeightDao,
) : AthletesRepository {

    override val allAthletes: Flow<List<AthleteItemModel>> = dao.getAllAthletes().map { athleteEntities ->
        athleteEntities.map { athlete ->
            AthleteItemModel(
                id = athlete.id,
                name = athlete.name,
                dateOfBirth = TimeUtils.formatDateWithMonthName(athlete.dateOfBirth),
                isMale = athlete.isMale,
                speedCoachSerial = athlete.speedCoachSerial,
                age = TimeUtils.ageInYearsNow(birthTimeInMillis = athlete.dateOfBirth),
            )
        }
    }

    override suspend fun getAllAthletesOnce(): List<AthleteEntity> = dao.getAllAthletes().first()

    override suspend fun addAthlete(athlete: AthleteEntity): Long = dao.addAthlete(athlete = athlete)

    override suspend fun deleteAthletes(athleteIds: List<Long>) {
        if (athleteIds.isNotEmpty()) {
            dao.deleteAthletes(athleteIds = athleteIds)
        }
    }

    override suspend fun getAthlete(athleteId: Long): AthleteEntity? = dao.getById(athleteId = athleteId)

    override suspend fun updateAthlete(athlete: AthleteEntity) = dao.updateAthlete(athlete = athlete)

    override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> =
        personalBestDao.getByAthlete(athleteId = athleteId)

    override suspend fun upsertPersonalBests(bests: List<AthletePersonalBestEntity>) {
        if (bests.isNotEmpty()) {
            personalBestDao.upsertAll(bests = bests)
        }
    }

    override suspend fun replacePersonalBests(
        athleteId: Long,
        bests: List<AthletePersonalBestEntity>,
    ) {
        personalBestDao.replaceAllForAthlete(
            athleteId = athleteId,
            bests = bests.map {
                it.copy(
                    id = 0,
                    athleteId = athleteId
                )
            },
        )
    }

    override suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity> = withContext(Dispatchers.Default) {
        if (athleteIds.isEmpty()) {
            emptyMap()
        } else {
            personalBestDao.getBestsForAthletes(
                athleteIds = athleteIds,
                boatType = boatType,
                distanceMeters = distanceMeters
            ).associateBy { it.athleteId }
        }
    }

    override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> =
        withContext(Dispatchers.Default) {
            if (athleteIds.isEmpty()) emptyMap() else dao.getAthletesByIds(athleteIds).associate { it.id to it.name }
        }

    override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? =
        dao.getAthleteBySpeedCoachSerial(serial = serial)?.id

    override suspend fun getWeights(athleteId: Long): List<AthleteWeightEntity> =
        weightDao.getByAthlete(athleteId = athleteId)

    override suspend fun upsertWeights(weights: List<AthleteWeightEntity>) {
        if (weights.isNotEmpty()) {
            weightDao.upsertAll(weights = weights)
        }
    }

    override suspend fun saveWeight(
        athleteId: Long,
        dateMillis: Long,
        weightTenths: Int,
    ) {
        weightDao.upsert(
            weight = AthleteWeightEntity(
                athleteId = athleteId,
                dateMillis = TimeUtils.noonOfDay(timeInMillis = dateMillis),
                weightTenths = weightTenths,
            )
        )
    }

    override suspend fun deleteWeight(
        athleteId: Long,
        dateMillis: Long,
    ) {
        weightDao.deleteByDate(
            athleteId = athleteId,
            dateMillis = TimeUtils.noonOfDay(timeInMillis = dateMillis),
        )
    }
}