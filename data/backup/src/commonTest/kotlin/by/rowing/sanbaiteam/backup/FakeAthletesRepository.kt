package by.rowing.sanbaiteam.backup

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeAthletesRepository(
    initial: List<AthleteEntity> = emptyList(),
    val weights: MutableList<AthleteWeightEntity> = mutableListOf(),
) : AthletesRepository {

    val athletes: MutableList<AthleteEntity> = initial.toMutableList()

    private var nextId: Long = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

    override val allAthletes: Flow<List<AthleteItemModel>> = flowOf(emptyList())

    override suspend fun getAllAthletesOnce(): List<AthleteEntity> = athletes.toList()

    override suspend fun addAthlete(athlete: AthleteEntity): Long {
        val id = nextId++
        athletes += athlete.copy(id = id)
        return id
    }

    override suspend fun deleteAthletes(athleteIds: List<Long>) {
        athletes.removeAll { it.id in athleteIds }
    }

    override suspend fun getAthlete(athleteId: Long): AthleteEntity? = athletes.firstOrNull { it.id == athleteId }

    override suspend fun updateAthlete(athlete: AthleteEntity) {
        val index = athletes.indexOfFirst { it.id == athlete.id }
        if (index >= 0) athletes[index] = athlete
    }

    override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> = emptyList()

    override suspend fun upsertPersonalBests(bests: List<AthletePersonalBestEntity>) = Unit

    override suspend fun replacePersonalBests(athleteId: Long, bests: List<AthletePersonalBestEntity>) = Unit

    override suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity> = emptyMap()

    override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> =
        athletes.filter { it.id in athleteIds }.associate { it.id to it.name }

    override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = null

    override suspend fun getWeights(athleteId: Long): List<AthleteWeightEntity> =
        weights.filter { it.athleteId == athleteId }.sortedBy { it.dateMillis }

    override suspend fun upsertWeights(weights: List<AthleteWeightEntity>) {
        weights.forEach { weight ->
            this.weights.removeAll { it.athleteId == weight.athleteId && it.dateMillis == weight.dateMillis }
            this.weights += weight
        }
    }

    override suspend fun saveWeight(athleteId: Long, dateMillis: Long, weightTenths: Int) = Unit

    override suspend fun deleteWeight(athleteId: Long, dateMillis: Long) = Unit
}