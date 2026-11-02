package by.rowing.sanbaiteam.shared.main.presentation

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.calculator.presentation.navigation.CalculatorNavGraphProvider
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.settings.presentation.navigation.SettingsNavGraphProvider
import by.rowing.sanbaiteam.shared.main.presentation.navigation.MainNavigation
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboard
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavGraphProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class MainViewModel(
    private val composeNavigator: ComposeNavigator,
    private val athletesRepository: AthletesRepository,
    private val trainingDashboard: TrainingDashboard,
) : ComposeBaseViewModel<MainUiState>(initialState = MainUiState()), MainScreenActions {

    init {
        viewModelScope.launch {
            combine(
                trainingDashboard.observeDashboard(),
                athletesRepository.allAthletes,
            ) { trainings, athletes -> trainings to athletes }
                .collect { (trainings, athletes) ->
                    changeState {
                        copy(
                            isLoading = false,
                            athletesCount = athletes.size,
                            trainingsCount = trainings.trainingCount,
                            recentTrainings = trainings.recent,
                        )
                    }
                }
        }
    }

    override fun navigateToAthletesScreen() {
        AthletesNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToTrainingsScreen() {
        TrainingNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToCalculatorScreen() {
        CalculatorNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToAddTrainingScreen() {
        TrainingNavGraphProvider.navigateToAdd(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun navigateToSettings() {
        SettingsNavGraphProvider.navigateToRoot(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }

    override fun onTrainingClick(trainingId: Long) {
        TrainingNavGraphProvider.navigateToDetail(
            composeNavigator = composeNavigator,
            trainingId = trainingId,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }
}