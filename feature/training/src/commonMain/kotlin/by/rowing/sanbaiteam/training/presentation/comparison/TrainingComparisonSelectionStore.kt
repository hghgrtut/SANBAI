package by.rowing.sanbaiteam.training.presentation.comparison

import by.rowing.sanbaiteam.training.presentation.comparison.models.TrainingComparisonSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит выбранные для сравнения тренировки в рамках текущей сессии.
 * Одновременно активна только одна базовая тренировка — экран детали один.
 */
internal class TrainingComparisonSelectionStore {

    private val _selection = MutableStateFlow<TrainingComparisonSelection?>(null)

    val selection: StateFlow<TrainingComparisonSelection?> = _selection.asStateFlow()

    fun select(
        trainingId: Long,
        comparedTrainingIds: Collection<Long>
    ) {
        _selection.value = comparedTrainingIds
            .filterNot { it == trainingId }
            .toSet()
            .takeIf { it.isNotEmpty() }
            ?.let { comparedTrainingIds ->
                TrainingComparisonSelection(
                    trainingId = trainingId,
                    comparedTrainingIds = comparedTrainingIds
                )
            }
    }

    fun clear() {
        _selection.value = null
    }
}