package by.rowing.sanbaiteam.athlete.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.add.compose.AddAthleteScreen
import by.rowing.sanbaiteam.athlete.presentation.detail.AthleteDetailViewModel
import by.rowing.sanbaiteam.athlete.presentation.detail.compose.AthleteDetailScreen
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
import by.rowing.sanbaiteam.athlete.presentation.list.compose.AthleteListScreen
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.composable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@AllowDetektPublic
object AthletesNavGraphProvider {

    fun athletesNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator
    ) {
        builder.apply {
            composable<AthletesNavigation.AthletesList, AthletesViewModel>(
                viewModelFactory = {
                    koinViewModel<AthletesViewModel> { parametersOf(navigator) }
                }
            ) { viewModel -> AthleteListScreen(viewModel = viewModel) }
            composable<AthletesNavigation.AthletesAdd, AddAthleteViewModel>(
                viewModelFactory = {
                    koinViewModel<AddAthleteViewModel> { parametersOf(navigator) }
                }
            ) { viewModel -> AddAthleteScreen(viewModel = viewModel) }
            composable<AthletesNavigation.AthleteDetail, AthleteDetailViewModel>(
                viewModelFactory = { route ->
                    koinViewModel<AthleteDetailViewModel> { parametersOf(navigator, route.athleteId) }
                }
            ) { viewModel -> AthleteDetailScreen(viewModel = viewModel) }
        }
    }

    fun navigateToRoot(
        composeNavigator: ComposeNavigator,
        navOptions: NavOptions,
    ) {
        AthletesNavigation.AthletesList.navigateTo(
            composeNavigator = composeNavigator,
            navOptions = navOptions
        )
    }
}