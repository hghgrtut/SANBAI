package by.rowing.sanbaiteam.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.calculator.presentation.navigation.CalculatorNavGraphProvider
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.NavigationEvent
import by.rowing.sanbaiteam.core.presentation.compose.composable
import by.rowing.sanbaiteam.shared.fileimport.ExternalFileImport
import by.rowing.sanbaiteam.shared.main.presentation.MainScreen
import by.rowing.sanbaiteam.shared.main.presentation.MainViewModel
import by.rowing.sanbaiteam.shared.main.presentation.navigation.MainNavigation
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavGraphProvider
import by.rowing.sanbaiteam.uikit.modifier.ClickableState
import by.rowing.sanbaiteam.uikit.modifier.LocalClickableState
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
@AllowDetektPublic
fun App(importUri: String? = null) {
    SANBAITeamTheme {
        val composeNavigator = koinInject<ComposeNavigator>()
        val clickableState = koinInject<ClickableState>()
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            composeNavigator.sharedFlow.collect { navigation ->
                handleNavigationEvent(
                    navigation = navigation,
                    navController = navController
                )
            }
        }
        val externalImportUri by ExternalFileImport.pendingUri.collectAsState()
        val effectiveImportUri = externalImportUri ?: importUri
        LaunchedEffect(effectiveImportUri) {
            val uri = effectiveImportUri ?: return@LaunchedEffect
            navController.navigate(route = TrainingNavGraphProvider.getAddTrainingRoute(importUri = uri))
            ExternalFileImport.consume(uri)
        }

        CompositionLocalProvider(LocalClickableState provides clickableState) {
            NavHost(
                navController = navController,
                startDestination = MainNavigation.Root,
                builder = navigationBuilder(composeNavigator)
            )
        }
    }
}

private val navigationBuilder: (ComposeNavigator) -> NavGraphBuilder.() -> Unit = { composeNavigator ->
    {
        composable<MainNavigation.Root, MainViewModel>(
            viewModelFactory = {
                koinViewModel<MainViewModel> { parametersOf(composeNavigator) }
            }
        ) { viewModel -> MainScreen(viewModel = viewModel) }
        AthletesNavGraphProvider.athletesNavGraph(
            builder = this,
            navigator = composeNavigator
        )
        TrainingNavGraphProvider.trainingNavGraph(
            builder = this,
            navigator = composeNavigator
        )
        CalculatorNavGraphProvider.calculatorNavGraph(
            builder = this,
            navigator = composeNavigator
        )
    }
}

private fun handleNavigationEvent(
    navigation: NavigationEvent,
    navController: NavHostController,
) {
    when (navigation) {
        is NavigationEvent.ClearBackStack -> {
            navController.popBackStack(
                destinationId = navController.graph.id,
                inclusive = true
            )
        }

        is NavigationEvent.NavigateBack -> {
            navigateBack(navigation, navController)
        }

        is NavigationEvent.NavigateForward -> {
            navigateForward(navigation, navController)
        }
    }
}

private fun navigateForward(
    navigation: NavigationEvent.NavigateForward,
    navController: NavHostController,
) {
    if (navigation.removeCurrentScreen) navController.popBackStack()

    navigation.args?.let { args ->
        navController.currentBackStackEntry?.savedStateHandle?.set(
            args.key, args.value
        )
    }

    navController.navigate(
        route = navigation.navTarget,
        navOptions = navigation.navOptions
    )
}

private fun navigateBack(
    navigation: NavigationEvent.NavigateBack,
    navController: NavHostController,
) {
    when (val navTarget = navigation.navTarget) {
        null -> {
            navigation.args?.let { args ->
                navController.previousBackStackEntry?.savedStateHandle?.set(args.key, args.value)
            }
            navController.popBackStack()
        }

        is String -> {
            navController.popBackStack(
                route = navTarget,
                inclusive = false
            )
            navigation.args?.let { args ->
                navController.currentBackStackEntry?.savedStateHandle?.set(args.key, args.value)
            }
        }

        else -> {
            navController.popBackStack(
                route = navTarget,
                inclusive = false
            )
            navigation.args?.let { args ->
                navController.currentBackStackEntry?.savedStateHandle?.set(args.key, args.value)
            }
        }
    }
}