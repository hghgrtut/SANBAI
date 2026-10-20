package by.rowing.sanbaiteam.training.presentation.list.compose

internal interface ListTrainingScreenActions {

    fun onBackClick()

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddTrainingClick()

    fun onTrainingClick(trainingId: Long)

    fun onTrainingLongClick(trainingId: Long)

    fun onCancelSelection()

    fun onDeleteClick()

    fun onDeleteConfirm()

    fun onDeleteDismiss()

    fun onExportClick()

    fun onExportPromptLaunched()

    fun onExportToHandle(handle: String)

    fun onImportFromHandle(handle: String)

    fun onDismissTransferNotice()

    companion object {

        fun empty() = object : ListTrainingScreenActions {
            override fun onBackClick() {}
            override fun onSearchQueryChange(newQuery: String) {}
            override fun onShowFiltersChange(showFilters: Boolean) {}
            override fun onAddTrainingClick() {}
            override fun onTrainingClick(trainingId: Long) {}
            override fun onTrainingLongClick(trainingId: Long) {}
            override fun onCancelSelection() {}
            override fun onDeleteClick() {}
            override fun onDeleteConfirm() {}
            override fun onDeleteDismiss() {}
            override fun onExportClick() {}
            override fun onExportPromptLaunched() {}
            override fun onExportToHandle(handle: String) {}
            override fun onImportFromHandle(handle: String) {}
            override fun onDismissTransferNotice() {}
        }
    }
}
