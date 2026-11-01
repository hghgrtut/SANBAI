package by.rowing.sanbaiteam.training.presentation.filter.compose

import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter

/** Действия панели фильтров, общие для списка тренировок и выбора для сравнения. */
internal interface TrainingFiltersActions {

    fun onFilterChange(filter: TrainingFilter)

    fun onFilterReset(parameter: TrainingFilterParameter)

    fun onResetFilters()

    fun onFilterSheetOpen(parameter: TrainingFilterParameter)

    fun onFilterSheetDismiss()

    fun onLactateFilterToggle()

    fun onShowFiltersChange(showFilters: Boolean)

    fun onQuickFilterApply(filterId: Long)

    fun onQuickFilterSaveClick()

    fun onQuickFilterSave(name: String)

    fun onQuickFilterOverwriteConfirm()

    fun onQuickFilterOverwriteDismiss()

    fun onQuickFilterManageClick(filterId: Long)

    fun onQuickFilterRename(
        filterId: Long,
        name: String
    )

    fun onQuickFilterDeleteClick(filterId: Long)

    fun onQuickFilterDeleteConfirm()

    fun onQuickFilterDeleteDismiss()

    fun onQuickFilterDialogDismiss()
}