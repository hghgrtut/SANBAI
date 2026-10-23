package by.rowing.sanbaiteam.training.data.models

import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import org.jetbrains.compose.resources.StringResource

internal data class TrainingComparisonCandidates(
    val typeResId: StringResource?,
    val items: List<ListTrainingStateItem>,
)
