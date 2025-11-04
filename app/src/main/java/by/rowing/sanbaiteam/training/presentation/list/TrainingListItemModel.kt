package by.rowing.sanbaiteam.training.presentation.list

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal data class TrainingListItemModel(
    val trainingId: Long,
    val trainingType: TrainingPieceType,
    val shortWorkDescription: String,
    val maxPulses: String,
    val goal: String
)