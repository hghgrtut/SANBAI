package by.rowing.sanbaiteam.training.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.composable
import by.rowing.sanbaiteam.training.presentation.add.AddTrainingScreen
import by.rowing.sanbaiteam.training.presentation.add.AddTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.chart.TrainingChartViewModel
import by.rowing.sanbaiteam.training.presentation.chart.compose.TrainingChartScreen
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingCompareViewModel
import by.rowing.sanbaiteam.training.presentation.comparison.compose.TrainingCompareScreen
import by.rowing.sanbaiteam.training.presentation.detail.TrainingDetailViewModel
import by.rowing.sanbaiteam.training.presentation.detail.compose.TrainingDetailScreen
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.list.compose.ListTrainingScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@AllowDetektPublic
object TrainingNavGraphProvider {

    fun trainingNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator
    ) {
        builder.composable<TrainingNavigation.TrainingList, ListTrainingViewModel>(
            viewModelFactory = {
                koinViewModel<ListTrainingViewModel> { parametersOf(navigator) }
            }
        ) { viewModel -> ListTrainingScreen(viewModel = viewModel) }
        builder.composable<TrainingNavigation.TrainingAdd, AddTrainingViewModel>(
            viewModelFactory = { route ->
                koinViewModel<AddTrainingViewModel> { parametersOf(navigator, route.importUri) }
            }
        ) { viewModel -> AddTrainingScreen(viewModel = viewModel) }
        builder.composable<TrainingNavigation.TrainingDetail, TrainingDetailViewModel>(
            viewModelFactory = { route ->
                koinViewModel<TrainingDetailViewModel> { parametersOf(navigator, route.trainingId) }
            }
        ) { viewModel -> TrainingDetailScreen(viewModel = viewModel) }
        builder.composable<TrainingNavigation.TrainingCompare, TrainingCompareViewModel>(
            viewModelFactory = { route ->
                koinViewModel<TrainingCompareViewModel> { parametersOf(navigator, route.trainingId) }
            }
        ) { viewModel -> TrainingCompareScreen(viewModel = viewModel) }
        builder.composable<TrainingNavigation.TrainingChart, TrainingChartViewModel>(
            viewModelFactory = { route ->
                koinViewModel<TrainingChartViewModel> {
                    parametersOf(navigator, route.trainingId, route.order, route.metric)
                }
            }
        ) { viewModel -> TrainingChartScreen(viewModel = viewModel) }
    }

    fun navigateToRoot(
        composeNavigator: ComposeNavigator,
        navOptions: NavOptions,
    ) {
        TrainingNavigation.TrainingList.navigateTo(
            composeNavigator = composeNavigator,
            navOptions = navOptions
        )
    }

    fun navigateToDetail(
        composeNavigator: ComposeNavigator,
        trainingId: Long,
        navOptions: NavOptions,
    ) {
        TrainingNavigation.TrainingDetail(trainingId = trainingId).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = navOptions
        )
    }

    fun navigateToAdd(
        composeNavigator: ComposeNavigator,
        navOptions: NavOptions,
    ) {
        TrainingNavigation.TrainingAdd().navigateTo(
            composeNavigator = composeNavigator,
            navOptions = navOptions
        )
    }

    fun getAddTrainingRoute(importUri: String): Any = TrainingNavigation.TrainingAdd(importUri = importUri)
}
