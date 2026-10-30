package by.rowing.sanbaiteam.training.presentation.list.models

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.filter.FilterableTraining
import org.jetbrains.compose.resources.StringResource

internal data class ListTrainingStateItem(
    override val dateMillis: Long,
    override val athleteIds: List<Long>,
    override val pieces: List<ListTrainingPieceItem>,
    override val trainingType: TrainingPieceType,
    val trainingId: Long,
    val trainingTypeResId: StringResource,
    val trainingDate: String,
    val trainingWork: String,
    val athletes: List<String>,
) : FilterableTraining