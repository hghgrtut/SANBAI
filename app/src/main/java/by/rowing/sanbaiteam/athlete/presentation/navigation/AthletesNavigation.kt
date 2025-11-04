package by.rowing.sanbaiteam.athlete.presentation.navigation

import by.rowing.sanbaiteam.core.presentation.compose.NavigationDestination
import kotlinx.serialization.Serializable

internal sealed interface AthletesNavigation {

    @Serializable
    data object AthletesList : NavigationDestination<Unit>

    @Serializable
    data object AthletesAdd : NavigationDestination<Unit>
}