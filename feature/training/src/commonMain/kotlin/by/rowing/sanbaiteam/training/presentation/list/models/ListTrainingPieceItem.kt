package by.rowing.sanbaiteam.training.presentation.list.models

internal data class ListTrainingPieceItem(
    val distanceMeters: Int,
    val timeMillis: Long,
    val lactate: Double?,
)