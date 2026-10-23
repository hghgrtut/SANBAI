package by.rowing.sanbaiteam.training.presentation.comparison.models

import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import org.jetbrains.compose.resources.StringResource

internal data class TrainingCompareState(
    val typeResId: StringResource? = null,
    val items: List<ListTrainingStateItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val hasAppliedComparison: Boolean = false,
    val isLoading: Boolean = true,
)
