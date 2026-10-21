package by.rowing.sanbaiteam.training.data.dashboard

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import org.jetbrains.compose.resources.StringResource

private const val RECENT_TRAININGS_LIMIT = 4

@AllowDetektPublic
data class TrainingDashboardItem(
    val trainingId: Long,
    val typeResId: StringResource,
    val date: String,
    val work: String,
    val athletes: List<String>,
)

@AllowDetektPublic
data class TrainingDashboardData(
    val trainingCount: Int,
    val recent: List<TrainingDashboardItem>,
)

/**
 * Публичный фасад над [TrainingRepository] для главного экрана.
 * Отдаёт только те данные, которые уже используются списком тренировок.
 */
@AllowDetektPublic
interface TrainingDashboard {

    suspend fun load(): TrainingDashboardData
}

internal class TrainingDashboardImpl(
    private val repository: TrainingRepository,
) : TrainingDashboard {

    override suspend fun load(): TrainingDashboardData {
        val trainings = repository.getTrainingsList()
        return TrainingDashboardData(
            trainingCount = trainings.size,
            recent = trainings
                .take(n = RECENT_TRAININGS_LIMIT)
                .map { training ->
                    TrainingDashboardItem(
                        trainingId = training.trainingId,
                        typeResId = training.trainingTypeResId,
                        date = training.trainingDate,
                        work = training.trainingWork,
                        athletes = training.athletes,
                    )
                },
        )
    }
}
