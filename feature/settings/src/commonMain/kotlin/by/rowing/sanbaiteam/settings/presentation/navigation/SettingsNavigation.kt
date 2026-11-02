package by.rowing.sanbaiteam.settings.presentation.navigation

import by.rowing.sanbaiteam.core.presentation.compose.NavigationDestination
import kotlinx.serialization.Serializable

internal sealed interface SettingsNavigation {

    @Serializable
    data object Settings : NavigationDestination<Unit>
}