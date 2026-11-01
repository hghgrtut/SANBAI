package by.rowing.sanbaiteam.data.di

import by.rowing.sanbaiteam.core.data.local.RowingDatabase
import org.koin.core.module.Module

expect val databaseModule: Module

internal fun Module.databaseDaos() {
    single { get<RowingDatabase>().athleteDao() }
    single { get<RowingDatabase>().athletePersonalBestDao() }
    single { get<RowingDatabase>().trainingDao() }
    single { get<RowingDatabase>().savedTrainingFilterDao() }
}