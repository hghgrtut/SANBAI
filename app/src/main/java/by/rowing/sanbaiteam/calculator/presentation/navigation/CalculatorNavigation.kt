package by.rowing.sanbaiteam.calculator.presentation.navigation

import by.rowing.sanbaiteam.core.presentation.compose.NavigationDestination
import kotlinx.serialization.Serializable

internal sealed interface CalculatorNavigation {

    @Serializable
    data object Calculator : NavigationDestination<Unit>
}
