package by.rowing.sanbaiteam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.NavigationEvent
import by.rowing.sanbaiteam.core.presentation.runWithCompose
import by.rowing.sanbaiteam.main.presentation.MainScreen
import by.rowing.sanbaiteam.main.presentation.MainViewModel
import by.rowing.sanbaiteam.main.presentation.navigation.MainNavigation
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavGraphProvider
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import com.checker.uikit3.modifier.ClickableState
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {

    private val clickableState by inject<ClickableState>()
    private val composeNavigator by inject<ComposeNavigator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(
            ComposeView(this).apply {
                runWithCompose(clickableState = clickableState) {
                    SANBAITeamTheme {
                        val navController = rememberNavController()
                        LaunchedEffect(null) {
                            composeNavigator.sharedFlow.collect { navigation ->
                                handleNavigationEvent(navigation, navController, composeNavigator)
                            }
                        }
                        NavHost(
                            navController = navController,
                            startDestination = MainNavigation.Root
                        ) {
                            composable<MainNavigation.Root> {
                                MainScreen(
                                    viewModel = koinViewModel<MainViewModel> { parametersOf(composeNavigator) }
                                )
                            }
                            AthletesNavGraphProvider.athletesNavGraph(
                                builder = this,
                                navigator = composeNavigator
                            )
                            TrainingNavGraphProvider.trainingNavGraph(
                                builder = this,
                                navigator = composeNavigator
                            )
                        }
                    }
                }
            }
        )
    }
}

private suspend fun handleNavigationEvent(
    navigation: NavigationEvent,
    navController: NavHostController,
    //lastNavBottomSheet: MutableState<NavigationEvent?>,
    composeNavigator: ComposeNavigator,
) {
    when (navigation) {
        is NavigationEvent.ClearBackStack -> {
            navController.popBackStack(
                destinationId = navController.graph.id,
                inclusive = true
            )
        }

        is NavigationEvent.NavigateBack -> {
            navigateBack(navigation, navController, composeNavigator)
        }

        is NavigationEvent.NavigateForward -> {
            navigateForward(navigation, navController)
        }
    }
}


private fun navigateForward(
    navigation: NavigationEvent.NavigateForward,
//    lastNavBottomSheet: MutableState<NavigationEvent?>,
    navController: NavHostController,
) {
//    if (navigation.shouldRememberBottomSheet) lastNavBottomSheet.value = navigation
    if (navigation.removeCurrentScreen) navController.popBackStack()

    navigation.args?.let { args ->
//        if (navController.currentBackStackEntry?.destination is BottomSheetNavigator.Destination) {
//            navController.popBackStack()
//        }
        navController.currentBackStackEntry?.savedStateHandle?.set(
            args.key, args.value
        )
    }

    val stringNavTarget = navigation.navTarget as? String
    if (stringNavTarget != null) {
        // здесь если навигация по строке - нужно явно кастовать к строке, хоть метод navigate один и тот же, параметры разные
        navController.navigate(
            route = stringNavTarget,
            navOptions = navigation.navOptions
        )
    } else {
        navController.navigate(
            route = navigation.navTarget,
            navOptions = navigation.navOptions
        )
    }
}

private suspend fun navigateBack(
    navigation: NavigationEvent.NavigateBack,
    navController: NavHostController,
   // lastNavBottomSheet: MutableState<NavigationEvent?>,
    composeNavigator: ComposeNavigator,
) {
    when (val navTarget = navigation.navTarget) {
        null -> {
            navigation.args?.let { args ->
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    args.key, args.value
                )
            }
            navController.popBackStack()
        }

        // здесь если навигация по строке - нужно явно кастовать к строке, хоть метод popBackStack один и тот же, параметры разные
        is String -> {
            navController.popBackStack(
                route = navTarget,
                inclusive = false
            )
            navigation.args?.let { args ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    args.key, args.value
                )
            }
        }

        else -> {
            navController.popBackStack(
                route = navTarget,
                inclusive = false
            )
            navigation.args?.let { args ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    args.key, args.value
                )
            }
        }
    }
//    restoreBottomSheetDialog(lastNavBottomSheet, navigation, composeNavigator, navController)
}