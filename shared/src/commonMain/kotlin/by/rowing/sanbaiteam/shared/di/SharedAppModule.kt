package by.rowing.sanbaiteam.shared.di

import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.shared.main.presentation.MainViewModel
import by.rowing.sanbaiteam.shared.main.presentation.navigation.MainNavigator
import by.rowing.sanbaiteam.uikit.modifier.ClickableState
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedAppModule = module {
    singleOf(::ClickableState)

    singleOf(::MainNavigator) bind ComposeNavigator::class

    viewModelOf(::MainViewModel)
}
