package by.rowing.sanbaiteam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.calculator.presentation.navigation.CalculatorNavGraphProvider
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.NavigationEvent
import by.rowing.sanbaiteam.main.presentation.MainScreen
import by.rowing.sanbaiteam.main.presentation.MainViewModel
import by.rowing.sanbaiteam.main.presentation.navigation.MainNavigation
import by.rowing.sanbaiteam.training.presentation.navigation.TrainingNavGraphProvider
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import com.checker.uikit3.modifier.ClickableState
import com.checker.uikit3.modifier.LocalClickableState
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {

    private val clickableState by inject<ClickableState>()
    private val composeNavigator by inject<ComposeNavigator>()
    private var pendingImportUri by mutableStateOf<String?>(null)


    private val mainActivityContent: @Composable () -> Unit = {
        CompositionLocalProvider(LocalClickableState provides clickableState) {
            SANBAITeamTheme {
                val navController = rememberNavController()
                LaunchedEffect(null) {
                    composeNavigator.sharedFlow.collect { navigation ->
                        handleNavigationEvent(navigation = navigation, navController = navController)
                    }
                }
                LaunchedEffect(pendingImportUri) {
                    val importUri = pendingImportUri ?: return@LaunchedEffect
                    navController.navigate(route = TrainingNavGraphProvider.getAddTrainingRoute(importUri = importUri))
                    pendingImportUri = null
                }
                NavHost(
                    navController = navController,
                    startDestination = MainNavigation.Root,
                    builder = navigationBuilder
                )
            }
        }
    }

    private val navigationBuilder: NavGraphBuilder.() -> Unit = {
        composable<MainNavigation.Root> {
            MainScreen(viewModel = koinViewModel<MainViewModel> { parametersOf(composeNavigator) })
        }
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingImportUri = extractImportUri(intent)
        enableEdgeToEdge()
        setContentView(
            ComposeView(this).apply {
                setViewCompositionStrategy(strategy = ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent(mainActivityContent)
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingImportUri = extractImportUri(intent)
    }

    private fun extractImportUri(intent: Intent?): String? = when (intent?.action) {
        Intent.ACTION_VIEW -> intent.data?.toString()
        Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.toString()
        else -> null
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

        // здесь если навигация по строке - нужно явно кастовать к строке, хоть метод popBackStack один и тот же, параметры разные
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