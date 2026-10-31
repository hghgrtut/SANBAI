package by.rowing.sanbaiteam.training.presentation.comparison.models

import by.rowing.sanbaiteam.training.presentation.filter.models.TrainingFiltersState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import org.jetbrains.compose.resources.StringResource

internal data class TrainingCompareState(
    val typeResId: StringResource? = null,
    val items: List<ListTrainingStateItem> = emptyList(),
    val filteredItems: List<ListTrainingStateItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val hasAppliedComparison: Boolean = false,
    val isLoading: Boolean = true,
    val filters: TrainingFiltersState = TrainingFiltersState(),
) {

    /** Выбранные кандидаты, которые видны с учётом текущего фильтра. */
    val visibleSelectedIds: Set<Long>
        get() {
            if (selectedIds.isEmpty()) return emptySet()
            val visibleIds = filteredItems.mapTo(mutableSetOf()) { item -> item.trainingId }
            return selectedIds.intersect(visibleIds)
        }
}