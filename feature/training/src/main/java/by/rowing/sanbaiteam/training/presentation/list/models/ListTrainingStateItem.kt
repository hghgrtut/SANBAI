package by.rowing.sanbaiteam.training.presentation.list.models

import org.jetbrains.compose.resources.StringResource

internal data class ListTrainingStateItem(
    val trainingId: Long,
    val trainingTypeResId: StringResource,
    val trainingDate: String,
    val trainingWork: String,
    val athletes: List<String>,
)
