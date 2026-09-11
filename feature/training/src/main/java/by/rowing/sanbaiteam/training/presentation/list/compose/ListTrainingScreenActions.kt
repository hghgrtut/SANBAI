package by.rowing.sanbaiteam.training.presentation.list.compose

internal interface ListTrainingScreenActions {

    fun onBackClick()

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddTrainingClick()

    fun onTrainingClick(trainingId: Long)

    companion object {

        fun empty() = object : ListTrainingScreenActions {
            override fun onBackClick() {}
            override fun onSearchQueryChange(newQuery: String) {}
            override fun onShowFiltersChange(showFilters: Boolean) {}
            override fun onAddTrainingClick() {}
            override fun onTrainingClick(trainingId: Long) {}
        }
    }
}
