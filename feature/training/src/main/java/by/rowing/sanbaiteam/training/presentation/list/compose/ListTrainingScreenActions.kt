package by.rowing.sanbaiteam.training.presentation.list.compose

import android.content.Context
import android.net.Uri

internal interface ListTrainingScreenActions {

    fun onBackClick()

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddTrainingClick()

    fun onTrainingClick(trainingId: Long)

    fun onExportClick()

    fun onExportPromptLaunched()

    fun onExportToUri(
        uri: Uri,
        context: Context
    )

    fun onImportFromUri(
        uri: Uri,
        context: Context
    )

    fun onDismissTransferNotice()

    companion object {

        fun empty() = object : ListTrainingScreenActions {
            override fun onBackClick() {}
            override fun onSearchQueryChange(newQuery: String) {}
            override fun onShowFiltersChange(showFilters: Boolean) {}
            override fun onAddTrainingClick() {}
            override fun onTrainingClick(trainingId: Long) {}
            override fun onExportClick() {}
            override fun onExportPromptLaunched() {}
            override fun onExportToUri(
                uri: Uri,
                context: Context
            ) {
            }

            override fun onImportFromUri(
                uri: Uri,
                context: Context
            ) {
            }

            override fun onDismissTransferNotice() {}
        }
    }
}