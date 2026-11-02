package by.rowing.sanbaiteam.settings.presentation.models

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode

internal data class WorkDisplaySettingItem(
    val pieceType: TrainingPieceType,
    val mode: TrainingWorkDisplayMode,
)