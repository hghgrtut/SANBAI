package by.rowing.sanbaiteam.training.presentation.list.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter

internal interface ListTrainingScreenActions : NavigateBackAware {

    fun onFilterChange(filter: TrainingFilter)

    fun onFilterReset(parameter: TrainingFilterParameter)

    fun onResetFilters()

    fun onFilterSheetOpen(parameter: TrainingFilterParameter)

    fun onFilterSheetDismiss()

    fun onLactateFilterToggle()

    fun onQuickFilterApply(filterId: Long)

    fun onQuickFilterSaveClick()

    fun onQuickFilterSave(name: String)

    fun onQuickFilterOverwriteConfirm()

    fun onQuickFilterOverwriteDismiss()

    fun onQuickFilterManageClick(filterId: Long)

    fun onQuickFilterRename(filterId: Long, name: String)

    fun onQuickFilterDeleteClick(filterId: Long)

    fun onQuickFilterDeleteConfirm()

    fun onQuickFilterDeleteDismiss()

    fun onQuickFilterDialogDismiss()

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