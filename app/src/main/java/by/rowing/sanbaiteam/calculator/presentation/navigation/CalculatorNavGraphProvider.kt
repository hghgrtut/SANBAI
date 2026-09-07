package by.rowing.sanbaiteam.calculator.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import by.rowing.sanbaiteam.calculator.presentation.CalculatorViewModel
import by.rowing.sanbaiteam.calculator.presentation.compose.CalculatorScreen
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal object CalculatorNavGraphProvider {

    val root = CalculatorNavigation.Calculator

    fun calculatorNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator,
    ) {
        builder.composable<CalculatorNavigation.Calculator> {
            CalculatorScreen(
                viewModel = koinViewModel<CalculatorViewModel> { parametersOf(navigator) }
            )
        }
    }
}
