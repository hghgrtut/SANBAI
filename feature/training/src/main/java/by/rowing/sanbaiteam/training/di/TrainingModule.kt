package by.rowing.sanbaiteam.training.di

import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingTransferRepository
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
    singleOf(::TrainingTransferRepository)
    single { TrainingImportStorage(androidContext()) }

    viewModelOf(::ListTrainingViewModel)
    viewModelOf(::TrainingDetailViewModel)
    viewModel {
        AddTrainingViewModel(
            resourceUtils = get(),
            athleteRepository = get(),
            trainingRepository = get(),
            trainingImportStorage = get(),
            composeNavigator = get(),
            importUri = getOrNull()
        )
    }
}