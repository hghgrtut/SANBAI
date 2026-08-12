package by.rowing.sanbaiteam.athlete.presentation.list

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.presentation.list.compose.AthletesListActions
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavigation
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class AthletesViewModel(
    private val repository: AthletesRepository,
    private val composeNavigator: ComposeNavigator
) : ComposeBaseViewModel<AthletesUiState>(
    initialState = AthletesUiState(
        items = emptyList(),
        searchQuery = "",
        showFilters = false
    )
), AthletesListActions {

    init {
        viewModelScope.launch {
            repository.allAthletes.collectLatest { changeState { copy(items = it) } }
        }
    }

    override fun onBackClick() {
        composeNavigator.navigateBack()
    }

    override fun onSearchQueryChange(newQuery: String) {
        changeState { copy(searchQuery = newQuery) }
    }

    override fun onShowFiltersChange(showFilters: Boolean) {
        changeState { copy(showFilters = showFilters) }
    }

    override fun onAddAthleteClick() {
        AthletesNavigation.AthletesAdd.navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<AthletesNavigation.AthletesList>()
        )
    }

    override fun onAthleteClick(athleteId: Long) {
        AthletesNavigation.AthleteDetail(athleteId).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<AthletesNavigation.AthletesList>()
        )
    }
}