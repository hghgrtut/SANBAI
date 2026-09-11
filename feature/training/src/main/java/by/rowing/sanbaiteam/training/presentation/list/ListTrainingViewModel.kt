package by.rowing.sanbaiteam.training.presentation.list

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.list.compose.ListTrainingScreenActions
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavigation
import kotlinx.coroutines.launch

internal class ListTrainingViewModel(
    private val repository: TrainingRepository,
    private val composeNavigator: ComposeNavigator
) : ComposeBaseViewModel<ListTrainingState>(
    initialState = ListTrainingState(
        items = emptyList(),
        searchQuery = "",
        showFilters = false
    )
), ListTrainingScreenActions {

    init {
        reloadTrainings()
    }

    fun reloadTrainings() {
        viewModelScope.launch {
            val trainings = repository.getTrainingsList()
            changeState { copy(items = trainings) }
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

    override fun onAddTrainingClick() {
        TrainingNavigation.TrainingAdd().navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingList>()
        )
    }

    override fun onTrainingClick(trainingId: Long) {
        TrainingNavigation.TrainingDetail(trainingId).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<TrainingNavigation.TrainingList>()
        )
    }
}
