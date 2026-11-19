package by.rowing.sanbaiteam.athlete.data.transfer

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
    initialBests: MutableList<AthletePersonalBestEntity> = mutableListOf(),
) : AthletesRepository {

    val athletes: MutableList<AthleteEntity> = initial.toMutableList()
    val bests: MutableList<AthletePersonalBestEntity> = initialBests
    val weights: MutableList<AthleteWeightEntity> = mutableListOf()

    private var nextId: Long = (initial.maxOfOrNull { it.id } ?: 0) + 1
    private var nextBestId: Long = (initialBests.maxOfOrNull { it.id } ?: 0) + 1

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

    override suspend fun getAthlete(athleteId: Long): AthleteEntity? =
        athletes.firstOrNull { it.id == athleteId }

    override suspend fun updateAthlete(athlete: AthleteEntity) {
        val index = athletes.indexOfFirst { existing -> existing.id == athlete.id }
        if (index >= 0) {
            athletes[index] = athlete
        }
    }

    override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> =
        bests.filter { it.athleteId == athleteId }

    override suspend fun upsertPersonalBests(bests: List<AthletePersonalBestEntity>) {
        bests.forEach { best ->
            val id = if (best.id == 0L) nextBestId++ else best.id
            this.bests.removeAll { existing ->
                existing.athleteId == best.athleteId &&
                        existing.boatType == best.boatType &&
                        existing.distanceMeters == best.distanceMeters
            }
            this.bests += best.copy(id = id)
        }
    }

    override suspend fun replacePersonalBests(
        athleteId: Long,
        bests: List<AthletePersonalBestEntity>,
    ) = Unit

    override suspend fun getPersonalBestsForAthletes(
        athleteIds: List<Long>,
        boatType: TrainingPieceType,
        distanceMeters: Int,
    ): Map<Long, AthletePersonalBestEntity> = emptyMap()

    override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> = emptyMap()

    override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = null

    override suspend fun getWeights(athleteId: Long): List<AthleteWeightEntity> =
        weights.filter { it.athleteId == athleteId }

    override suspend fun upsertWeights(weights: List<AthleteWeightEntity>) {
        weights.forEach { weight ->
            this.weights.removeAll { existing ->
                existing.athleteId == weight.athleteId && existing.dateMillis == weight.dateMillis
            }
            this.weights += weight
        }
    }

    override suspend fun saveWeight(
        athleteId: Long,
        dateMillis: Long,
        weightTenths: Int,
    ) = Unit

    override suspend fun deleteWeight(
        athleteId: Long,
        dateMillis: Long,
    ) = Unit
}