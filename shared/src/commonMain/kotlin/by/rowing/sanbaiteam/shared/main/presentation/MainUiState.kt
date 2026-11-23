package by.rowing.sanbaiteam.shared.main.presentation

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboardItem

internal data class MainUiState(
    val isLoading: Boolean = true,
    val athletes: List<AthleteItemModel> = emptyList(),
    val weightDialog: MainWeightDialogState? = null,
    val trainingsCount: Int = 0,
    val recentTrainings: List<TrainingDashboardItem> = emptyList(),
)
