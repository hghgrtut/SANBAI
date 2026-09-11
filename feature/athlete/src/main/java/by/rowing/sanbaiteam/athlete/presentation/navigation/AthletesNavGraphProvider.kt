package by.rowing.sanbaiteam.athlete.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.add.compose.AddAthleteScreen
import by.rowing.sanbaiteam.athlete.presentation.detail.AthleteDetailViewModel
import by.rowing.sanbaiteam.athlete.presentation.detail.compose.AthleteDetailScreen
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
import by.rowing.sanbaiteam.athlete.presentation.list.compose.AthleteListScreen
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object AthletesNavGraphProvider {

    fun athletesNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator
    ) {
        builder.apply {
            composable<AthletesNavigation.AthletesList> {
                AthleteListScreen(viewModel = koinViewModel<AthletesViewModel> { parametersOf(navigator) })
            }
            composable<AthletesNavigation.AthletesAdd> {
                AddAthleteScreen(viewModel = koinViewModel<AddAthleteViewModel> { parametersOf(navigator) })
            }
            composable<AthletesNavigation.AthleteDetail> { backStackEntry ->
                val athleteId = backStackEntry.toRoute<AthletesNavigation.AthleteDetail>().athleteId
                AthleteDetailScreen(
                    viewModel = koinViewModel<AthleteDetailViewModel> { parametersOf(navigator, athleteId) }
                )
            }
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