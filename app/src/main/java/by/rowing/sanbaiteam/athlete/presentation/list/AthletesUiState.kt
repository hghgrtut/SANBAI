package by.rowing.sanbaiteam.athlete.presentation.list

import by.rowing.sanbaiteam.athlete.presentation.common.AthleteItemModel

internal data class AthletesUiState(
    val items: List<AthleteItemModel>,
    val searchQuery: String,
    val showFilters: Boolean,
)