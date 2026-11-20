package by.rowing.sanbaiteam.athlete.presentation.list.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware

internal interface AthletesListActions : NavigateBackAware {

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddAthleteClick()

    fun onAthleteClick(athleteId: Long)

    fun onAthleteLongClick(athleteId: Long)

    fun onCancelSelection()

    fun onToggleSelectAllClick(athleteIds: List<Long>)

    fun onDeleteClick()

    fun onDeleteDismiss()

    fun onDeleteConfirm()

    fun onExportSelectedClick()

    fun onExportPromptLaunched()

    fun onExportToHandle(handle: String)

    fun onImportFromHandle(handle: String)

    fun onDismissTransferNotice()

    companion object {

        fun empty(): AthletesListActions =
            object : AthletesListActions, NavigateBackAware by NavigateBackAware.empty() {

                override fun onSearchQueryChange(newQuery: String) {}
                override fun onShowFiltersChange(showFilters: Boolean) {}
                override fun onAddAthleteClick() {}
                override fun onAthleteClick(athleteId: Long) {}
                override fun onAthleteLongClick(athleteId: Long) {}
                override fun onCancelSelection() {}
                override fun onToggleSelectAllClick(athleteIds: List<Long>) {}
                override fun onDeleteClick() {}
                override fun onDeleteDismiss() {}
                override fun onDeleteConfirm() {}
                override fun onExportSelectedClick() {}
                override fun onExportPromptLaunched() {}
                override fun onExportToHandle(handle: String) {}
                override fun onImportFromHandle(handle: String) {}
                override fun onDismissTransferNotice() {}
            }
    }
}