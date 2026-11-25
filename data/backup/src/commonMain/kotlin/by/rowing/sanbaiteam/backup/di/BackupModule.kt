package by.rowing.sanbaiteam.backup.di

import by.rowing.sanbaiteam.backup.data.repository.BackupRepository
import by.rowing.sanbaiteam.backup.data.serializer.BackupSerializer
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import org.koin.dsl.module

@AllowDetektPublic
val backupModule = module {
    single { BackupSerializer() }
    single {
        BackupRepository(
            trainingDao = get(),
            athletesRepository = get(),
            athleteWeightDao = get(),
            importStorage = get(),
            backupSerializer = get(),
        )
    }
}