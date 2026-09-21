package by.rowing.sanbaiteam.training.presentation.list.models

internal data class ListTrainingState(
    val items: List<ListTrainingStateItem>,
    val searchQuery: String,
    val showFilters: Boolean,
    val transferNotice: String?,
    val isTransferring: Boolean,
    val exportPromptRequested: Boolean,
)