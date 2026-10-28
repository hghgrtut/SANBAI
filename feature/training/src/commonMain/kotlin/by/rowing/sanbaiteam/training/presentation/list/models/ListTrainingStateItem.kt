package by.rowing.sanbaiteam.training.presentation.list.models

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import org.jetbrains.compose.resources.StringResource

internal data class ListTrainingStateItem(
    val trainingId: Long,
    val trainingType: TrainingPieceType,
    val trainingTypeResId: StringResource,
    val trainingDate: String,
    val trainingWork: String,
    val athletes: List<String>,
)