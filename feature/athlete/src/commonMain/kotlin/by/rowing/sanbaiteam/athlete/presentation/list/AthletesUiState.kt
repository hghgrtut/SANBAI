package by.rowing.sanbaiteam.athlete.presentation.list

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.uikit.component.UiText

internal data class AthletesUiState(
    val items: List<AthleteItemModel>,
    val searchQuery: String,
    val showFilters: Boolean,
    val selectedAthleteIds: Set<Long> = emptySet(),
    val deleteConfirmationRequested: Boolean = false,
    val relatedTrainingsCount: Int = 0,
    val exportNotice: UiText? = null,
    val isExporting: Boolean = false,
    val exportPromptRequested: Boolean = false,
    val isLoading: Boolean = true,
) {

    val isSelectionMode: Boolean get() = selectedAthleteIds.isNotEmpty()
}