package by.rowing.sanbaiteam.di

import by.rowing.sanbaiteam.core.data.local.RowingDatabase
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.main.presentation.MainViewModel
import by.rowing.sanbaiteam.main.presentation.navigation.MainNavigator
import com.checker.uikit3.modifier.ClickableState
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val appModule = module {
    // Database
    single { RowingDatabase.getInstance(androidContext()) }
    single { get<RowingDatabase>().athleteDao() }
    single { get<RowingDatabase>().trainingDao() }

    // Repository
    singleOf(::AthletesRepository)

    singleOf(::ClickableState)

    singleOf(::MainNavigator) bind ComposeNavigator::class

    // ViewModels
    viewModelOf(::AthletesViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::AddAthleteViewModel)
}