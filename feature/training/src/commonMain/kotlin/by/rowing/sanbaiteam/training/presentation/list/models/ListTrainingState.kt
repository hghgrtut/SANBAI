package by.rowing.sanbaiteam.training.presentation.list.models

import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter
import by.rowing.sanbaiteam.uikit.component.UiText

internal data class ListTrainingState(
    val items: List<ListTrainingStateItem>,
    val filter: TrainingFilter = TrainingFilter(),
    val activeFilterSheet: TrainingFilterParameter? = null,
    val showFilters: Boolean,
    val transferNotice: UiText?,
    val isTransferring: Boolean,
    val exportPromptRequested: Boolean,
    val selectedTrainingIds: Set<Long> = emptySet(),
    val deleteConfirmationRequested: Boolean = false,
    val isLoading: Boolean = true,
    val athleteCatalog: List<AthleteItemModel> = emptyList(),
) {

    val isSelectionMode: Boolean get() = selectedTrainingIds.isNotEmpty()
}