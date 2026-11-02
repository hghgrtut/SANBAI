package by.rowing.sanbaiteam.settings.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.composable
import by.rowing.sanbaiteam.settings.presentation.SettingsViewModel
import by.rowing.sanbaiteam.settings.presentation.compose.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@AllowDetektPublic
object SettingsNavGraphProvider {

    fun settingsNavGraph(
        builder: NavGraphBuilder,
        navigator: ComposeNavigator,
    ) {
        builder.composable<SettingsNavigation.Settings, SettingsViewModel>(
            viewModelFactory = {
                koinViewModel<SettingsViewModel> { parametersOf(navigator) }
            }
        ) { viewModel -> SettingsScreen(viewModel = viewModel) }
    }

    fun navigateToRoot(
        composeNavigator: ComposeNavigator,
        navOptions: NavOptions,
    ) {
        SettingsNavigation.Settings.navigateTo(
            composeNavigator = composeNavigator,
            navOptions = navOptions,
        )
    }
}