package by.rowing.sanbaiteam.training.presentation.list.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware

internal interface ListTrainingScreenActions : NavigateBackAware {

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddTrainingClick()

    fun onTrainingClick(trainingId: Long)

    fun onTrainingLongClick(trainingId: Long)

    fun onCancelSelection()

    fun onDeleteClick()

    fun onDeleteConfirm()

    fun onDeleteDismiss()

    fun onExportSelectedClick()

    fun onToggleSelectAllClick(trainingIds: List<Long>)

    fun onExportPromptLaunched()

    fun onExportToHandle(handle: String)

    fun onImportFromHandle(handle: String)

    fun onDismissTransferNotice()
}