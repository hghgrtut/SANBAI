package by.rowing.sanbaiteam.training.di

import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachImportStorage
import by.rowing.sanbaiteam.training.presentation.add.AddTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.detail.TrainingDetailViewModel
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val trainingModule = module {
    singleOf(::TrainingRepository)
    single { SpeedCoachImportStorage(androidContext()) }

    viewModelOf(::ListTrainingViewModel)
    viewModelOf(::TrainingDetailViewModel)
    viewModel {
        AddTrainingViewModel(
            resourceUtils = get(),
            athleteRepository = get(),
            trainingRepository = get(),
            speedCoachImportStorage = get(),
            composeNavigator = get(),
            importUri = getOrNull()
        )
    }
}