package by.rowing.sanbaiteam.athlete.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.athlete.data.local.AthletePersonalBestDao
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

internal class AthletesRepositoryImpl(
    private val dao: AthleteDao,
    private val personalBestDao: AthletePersonalBestDao,
) : AthletesRepository {

    override val allAthletes: Flow<List<AthleteItemModel>> = dao.getAllAthletes().map { athleteEntities ->
        athleteEntities.map { athlete ->
            AthleteItemModel(
                id = athlete.id,
                name = athlete.name,
                dateOfBirth = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(athlete.dateOfBirth),
                isMale = athlete.isMale,
                speedCoachSerial = athlete.speedCoachSerial,
            )
        }
    }

    override fun addAthlete(athlete: AthleteEntity) {
        CoroutineScope(Dispatchers.IO).launch { dao.addAthlete(athlete = athlete) }
    }

    override suspend fun getAthlete(athleteId: Long): AthleteEntity? =
        withContext(Dispatchers.IO) { dao.getById(athleteId) }

    override suspend fun updateAthlete(athlete: AthleteEntity) =
        withContext(Dispatchers.IO) { dao.updateAthlete(athlete) }

    override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> =
        withContext(Dispatchers.IO) { personalBestDao.getByAthlete(athleteId) }

    override suspend fun replacePersonalBestsForBoatType(
        athleteId: Long,
        boatType: TrainingPieceType,
        bests: List<AthletePersonalBestEntity>,
    ) = withContext(Dispatchers.IO) {
        personalBestDao.deleteAllForAthleteBoatType(athleteId, boatType)
        if (bests.isNotEmpty()) {
            personalBestDao.upsertAll(
                bests.map {
                    it.copy(
                        id = 0,
                        athleteId = athleteId,
                        boatType = boatType
                    )
                }
            )
        }
    }

    override suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity> = withContext(Dispatchers.IO) {
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

    override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> = withContext(Dispatchers.IO) {
        if (athleteIds.isEmpty()) emptyMap() else dao.getAthletesByIds(athleteIds).associate { it.id to it.name }
    }

    override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = withContext(Dispatchers.IO) {
        dao.getAthleteBySpeedCoachSerial(serial)?.id
    }
}