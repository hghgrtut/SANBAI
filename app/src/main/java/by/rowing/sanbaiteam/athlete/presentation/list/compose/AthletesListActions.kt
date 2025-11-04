package by.rowing.sanbaiteam.athlete.presentation.list.compose

internal interface AthletesListActions {

    fun onBackClick()

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddAthleteClick()

    companion object {

        fun empty() = object : AthletesListActions {

            override fun onBackClick() {}
            override fun onSearchQueryChange(newQuery: String) {}
            override fun onShowFiltersChange(showFilters: Boolean) {}
            override fun onAddAthleteClick() {}
        }
    }
}