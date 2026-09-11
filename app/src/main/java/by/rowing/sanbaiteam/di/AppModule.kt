package by.rowing.sanbaiteam.di

import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.util.AndroidResourceUtils
import by.rowing.sanbaiteam.main.presentation.MainViewModel
import by.rowing.sanbaiteam.main.presentation.navigation.MainNavigator
import com.checker.uikit3.modifier.ClickableState
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module


val appModule = module {
    single { AndroidResourceUtils(androidContext()) }

    singleOf(::ClickableState)

    singleOf(::MainNavigator) bind ComposeNavigator::class

    viewModelOf(::MainViewModel)
}