package by.rowing.sanbaiteam.shared.main.presentation

import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboardItem

internal data class MainUiState(
    val isLoading: Boolean = true,
    val athletesCount: Int = 0,
    val trainingsCount: Int = 0,
    val recentTrainings: List<TrainingDashboardItem> = emptyList(),
)
