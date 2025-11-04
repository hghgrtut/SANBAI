package by.rowing.sanbaiteam.main.presentation

import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavGraphProvider
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.BaseViewModel
import by.rowing.sanbaiteam.main.presentation.navigation.MainNavigation

internal class MainViewModel(private val composeNavigator: ComposeNavigator) : BaseViewModel(), MainScreenActions {

    override fun navigateToAthletesScreen() {
        AthletesNavGraphProvider.root.navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<MainNavigation.Root>()
        )
    }
}