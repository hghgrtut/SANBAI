package by.rowing.sanbaiteam.athlete.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
            )
        }
    }

    fun addAthlete(athlete: AthleteEntity) = CoroutineScope(Dispatchers.IO).launch {
        dao.addAthlete(athlete = athlete)
    }
}