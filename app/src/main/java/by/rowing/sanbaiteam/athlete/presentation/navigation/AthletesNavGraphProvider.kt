package by.rowing.sanbaiteam.athlete.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.add.compose.AddAthleteScreen
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
import by.rowing.sanbaiteam.athlete.presentation.list.compose.AthleteListScreen
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal object AthletesNavGraphProvider {

    val root = AthletesNavigation.AthletesList

    fun athletesNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator,
        navHostController: NavHostController
    ) {
        builder.apply {
            composable<AthletesNavigation.AthletesList> {
                AthleteListScreen(viewModel = koinViewModel<AthletesViewModel> { parametersOf(navigator) })
            }
            composable<AthletesNavigation.AthletesAdd> {
                AddAthleteScreen(viewModel = koinViewModel<AddAthleteViewModel> { parametersOf(navigator) })
            }
        }
    }
}