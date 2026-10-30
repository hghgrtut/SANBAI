package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingPieceItem

/**
 * Данные тренировки, необходимые для применения фильтров.
 *
 * Интерфейс отделяет движок фильтрации от UI-модели списка, чтобы логику
 * можно было проверить без ресурсов Compose.
 */
internal interface FilterableTraining {

    val trainingType: TrainingPieceType

    val dateMillis: Long

    val athleteIds: List<Long>

    val pieces: List<ListTrainingPieceItem>
}