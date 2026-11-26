package by.rowing.sanbaiteam.shared.main.presentation

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboardItem
import by.rowing.sanbaiteam.uikit.component.UiText

internal data class MainUiState(
    val isLoading: Boolean = true,
    val athletes: List<AthleteItemModel> = emptyList(),
    val weightDialog: MainWeightDialogState? = null,
    val syncDialog: MainSyncDialogState? = null,
    val syncNotice: UiText? = null,
    val trainingsCount: Int = 0,
    val recentTrainings: List<TrainingDashboardItem> = emptyList(),
)