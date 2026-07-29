package by.rowing.sanbaiteam.training.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.training.presentation.add.AddTrainingScreen
import by.rowing.sanbaiteam.training.presentation.add.AddTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.detail.TrainingDetailViewModel
import by.rowing.sanbaiteam.training.presentation.detail.compose.TrainingDetailScreen
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.list.compose.ListTrainingScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal object TrainingNavGraphProvider {

    val root = TrainingNavigation.TrainingList

    fun trainingNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator
    ) {
        builder.composable<TrainingNavigation.TrainingList> {
            ListTrainingScreen(viewModel = koinViewModel<ListTrainingViewModel> { parametersOf(navigator) })
        }
        builder.composable<TrainingNavigation.TrainingAdd> { backStackEntry ->
            val route = backStackEntry.toRoute<TrainingNavigation.TrainingAdd>()
            AddTrainingScreen(
                viewModel = koinViewModel<AddTrainingViewModel> {
                    parametersOf(navigator, route.importUri)
                }
            )
        }
        builder.composable<TrainingNavigation.TrainingDetail> { backStackEntry ->
            val trainingId = backStackEntry.toRoute<TrainingNavigation.TrainingDetail>().trainingId
            TrainingDetailScreen(
                viewModel = koinViewModel<TrainingDetailViewModel> { parametersOf(navigator, trainingId) }
            )
        }
    }
}
