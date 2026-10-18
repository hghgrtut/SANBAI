package by.rowing.sanbaiteam.training.di

import by.rowing.sanbaiteam.training.data.trainingimport.EmptyTrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.InMemoryTrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupFileAccess
import by.rowing.sanbaiteam.training.data.trainingtransfer.InMemoryBackupFileAccess
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val trainingPlatformModule: Module = module {
    single<TrainingImportStorage> { InMemoryTrainingImportStorage() }
    single<TrainingFileReader> { EmptyTrainingFileReader() }
    single<BackupFileAccess> { InMemoryBackupFileAccess() }
}
