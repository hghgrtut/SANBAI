package by.rowing.sanbaiteam.main.presentation.navigation

import by.rowing.sanbaiteam.core.presentation.compose.NavigationDestination
import kotlinx.serialization.Serializable

internal sealed interface MainNavigation {

    @Serializable
    data object Root : NavigationDestination<Unit>
}