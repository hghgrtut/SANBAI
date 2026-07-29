package by.rowing.sanbaiteam.athlete.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

internal class AthletesRepository(private val dao: AthleteDao) {

    val allAthletes: Flow<List<AthleteItemModel>> = dao.getAllAthletes().map { athleteEntities ->
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

    fun addAthlete(athlete: AthleteEntity) = CoroutineScope(Dispatchers.IO).launch {
        dao.addAthlete(athlete = athlete)
    }

    suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> = withContext(Dispatchers.IO) {
        if (athleteIds.isEmpty()) emptyMap()
        else dao.getAthletesByIds(athleteIds).associate { it.id to it.name }
    }

    suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = withContext(Dispatchers.IO) {
        dao.getAthleteBySpeedCoachSerial(serial)?.id
    }
}