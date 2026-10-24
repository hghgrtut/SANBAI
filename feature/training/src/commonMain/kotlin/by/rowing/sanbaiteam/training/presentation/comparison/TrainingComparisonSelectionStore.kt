package by.rowing.sanbaiteam.training.presentation.comparison

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит выбранные для сравнения тренировки в рамках текущей сессии.
 * Одновременно активна только одна базовая тренировка — экран детали один.
 */
internal class TrainingComparisonSelectionStore {

    private val _selection = MutableStateFlow<Set<Long>?>(null)

    val selection: StateFlow<Set<Long>?> = _selection.asStateFlow()

    fun select(
        trainingId: Long,
        comparedTrainingIds: Collection<Long>
    ) {
        _selection.value = comparedTrainingIds
            .filterNot { it == trainingId }
            .toSet()
            .takeIf { it.isNotEmpty() }
    }

    fun clear() {
        _selection.value = null
    }
}