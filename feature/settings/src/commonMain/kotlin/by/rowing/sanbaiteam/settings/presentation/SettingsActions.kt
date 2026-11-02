package by.rowing.sanbaiteam.settings.presentation

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode

internal interface SettingsActions : NavigateBackAware {

    fun onWorkDisplayModeChanged(
        pieceType: TrainingPieceType,
        mode: TrainingWorkDisplayMode,
    )

    companion object {

        fun empty() = object : SettingsActions {

            override fun onWorkDisplayModeChanged(
                pieceType: TrainingPieceType,
                mode: TrainingWorkDisplayMode,
            ) {
            }

            override fun navigateBack() {}
        }
    }
}