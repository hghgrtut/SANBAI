package by.rowing.sanbaiteam.athlete.presentation.list.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware

internal interface AthletesListActions : NavigateBackAware {

    fun onSearchQueryChange(newQuery: String)

    fun onShowFiltersChange(showFilters: Boolean)

    fun onAddAthleteClick()

    fun onAthleteClick(athleteId: Long)

    companion object {

        fun empty(): AthletesListActions =
            object : AthletesListActions, NavigateBackAware by NavigateBackAware.empty() {

                override fun onSearchQueryChange(newQuery: String) {}
                override fun onShowFiltersChange(showFilters: Boolean) {}
                override fun onAddAthleteClick() {}
                override fun onAthleteClick(athleteId: Long) {}
            }
    }
}