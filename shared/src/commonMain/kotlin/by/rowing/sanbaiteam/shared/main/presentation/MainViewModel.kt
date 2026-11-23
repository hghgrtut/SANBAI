package by.rowing.sanbaiteam.shared.main.presentation

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightInput
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.calculator.presentation.navigation.CalculatorNavGraphProvider
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
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
                            athletes = athletes,
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

    override fun openWeightDialog() {
        val athleteId = state.athletes.firstOrNull()?.id ?: return
        changeState {
            copy(
                weightDialog = MainWeightDialogState(
                    athleteId = athleteId,
                    dateMillis = TimeUtils.noonOfDay(timeInMillis = nowInMillis()),
                    weightText = "",
                )
            )
        }
    }

    override fun dismissWeightDialog() {
        changeState { copy(weightDialog = null) }
    }

    override fun changeWeightAthlete(athleteId: Long) {
        changeState {
            copy(
                weightDialog = weightDialog?.copy(
                    athleteId = athleteId,
                    error = null,
                )
            )
        }
    }

    override fun changeWeightDate(
        year: Int,
        month: Int,
        day: Int,
    ) {
        TimeUtils.noonMillis(
            year = year,
            month = month,
            day = day,
        )?.let { dateMillis ->
            changeState {
                copy(
                    weightDialog = weightDialog?.copy(
                        dateMillis = dateMillis,
                        error = null,
                    )
                )
            }
        }
    }

    override fun changeWeightValue(text: String) {
        changeState {
            copy(
                weightDialog = weightDialog?.copy(
                    weightText = AthleteWeightInput.sanitize(text = text),
                    error = null,
                )
            )
        }
    }

    override fun saveWeight() {
        val dialog = state.weightDialog ?: return
        val error = AthleteWeightInput.validate(
            text = dialog.weightText,
            dateMillis = dialog.dateMillis,
            nowMillis = nowInMillis(),
        )
        if (error != null) {
            changeState { copy(weightDialog = dialog.copy(error = error)) }
        } else {
            val weightTenths = AthleteWeightInput.parse(text = dialog.weightText) ?: return
            viewModelScope.launch {
                athletesRepository.saveWeight(
                    athleteId = dialog.athleteId,
                    dateMillis = dialog.dateMillis,
                    weightTenths = weightTenths,
                )
                changeState { copy(weightDialog = null) }
            }
        }
    }
}