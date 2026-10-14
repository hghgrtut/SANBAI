package by.rowing.sanbaiteam.training.di

import by.rowing.sanbaiteam.training.data.trainingimport.AndroidTrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.AndroidTrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingtransfer.AndroidBackupFileAccess
import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupFileAccess
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val trainingPlatformModule: Module = module {
    single<TrainingImportStorage> { AndroidTrainingImportStorage(androidContext()) }
    single<TrainingFileReader> { AndroidTrainingFileReader(androidContext()) }
    single<BackupFileAccess> { AndroidBackupFileAccess(androidContext()) }
}
