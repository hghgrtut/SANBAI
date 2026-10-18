package by.rowing.sanbaiteam.training.presentation.list.models

import by.rowing.sanbaiteam.uikit.component.UiText

internal data class ListTrainingState(
    val items: List<ListTrainingStateItem>,
    val searchQuery: String,
    val showFilters: Boolean,
    val transferNotice: UiText?,
    val isTransferring: Boolean,
    val exportPromptRequested: Boolean,
)
