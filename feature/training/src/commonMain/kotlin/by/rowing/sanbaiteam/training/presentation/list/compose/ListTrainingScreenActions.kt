package by.rowing.sanbaiteam.training.presentation.list.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.presentation.filter.compose.TrainingFiltersActions

internal interface ListTrainingScreenActions : NavigateBackAware, TrainingFiltersActions {

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