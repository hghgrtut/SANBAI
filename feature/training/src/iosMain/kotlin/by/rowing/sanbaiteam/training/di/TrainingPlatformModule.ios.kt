package by.rowing.sanbaiteam.training.di

import by.rowing.sanbaiteam.training.data.trainingimport.IosTrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.IosTrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val trainingPlatformModule: Module = module {
    single<TrainingImportStorage> { IosTrainingImportStorage() }
    single<TrainingFileReader> { IosTrainingFileReader() }
}