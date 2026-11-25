package by.rowing.sanbaiteam.backup

import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteWeightDao

internal class FakeAthleteWeightDao(
    val weights: MutableList<AthleteWeightEntity> = mutableListOf(),
) : AthleteWeightDao {

    override suspend fun getByAthlete(athleteId: Long): List<AthleteWeightEntity> =
        weights.filter { it.athleteId == athleteId }.sortedBy { it.dateMillis }

    override suspend fun getByDateRange(fromMillis: Long, toMillis: Long): List<AthleteWeightEntity> =
        weights.filter { it.dateMillis in fromMillis..toMillis }
            .sortedWith(compareBy({ it.athleteId }, { it.dateMillis }))

    override suspend fun upsert(weight: AthleteWeightEntity) {
        upsertAll(listOf(weight))
    }

    override suspend fun upsertAll(weights: List<AthleteWeightEntity>) {
        weights.forEach { weight ->
            this.weights.removeAll { it.athleteId == weight.athleteId && it.dateMillis == weight.dateMillis }
            this.weights += weight
        }
    }

    override suspend fun deleteByDate(athleteId: Long, dateMillis: Long) {
        weights.removeAll { it.athleteId == athleteId && it.dateMillis == dateMillis }
    }
}
