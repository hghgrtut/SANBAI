package by.rowing.sanbaiteam.training.presentation.comparison

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class TrainingComparisonSelection(
    val trainingId: Long,
    val comparedTrainingIds: Set<Long>,
)

internal fun TrainingComparisonSelection?.comparedTrainingIdsFor(trainingId: Long): Set<Long> =
    this
        ?.takeIf { it.trainingId == trainingId }
        ?.comparedTrainingIds
        .orEmpty()

/**
 * Хранит выбранные для сравнения тренировки в рамках текущей сессии.
 * Одновременно активна только одна базовая тренировка — экран детали один.
 */
internal class TrainingComparisonSelectionStore {

    private val _selection = MutableStateFlow<TrainingComparisonSelection?>(null)

    val selection: StateFlow<TrainingComparisonSelection?> = _selection.asStateFlow()

    fun select(trainingId: Long, comparedTrainingIds: Collection<Long>) {
        val normalized = comparedTrainingIds
            .filterNot { it == trainingId }
            .toSet()
        _selection.value = normalized
            .takeIf { it.isNotEmpty() }
            ?.let { TrainingComparisonSelection(trainingId, it) }
    }

    fun clear(trainingId: Long) {
        if (_selection.value?.trainingId == trainingId) {
            _selection.value = null
        }
    }
}
