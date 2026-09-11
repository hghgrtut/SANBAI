package by.rowing.sanbaiteam.data.di

import by.rowing.sanbaiteam.core.data.local.RowingDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { RowingDatabase.getInstance(androidContext()) }
    single { get<RowingDatabase>().athleteDao() }
    single { get<RowingDatabase>().athletePersonalBestDao() }
    single { get<RowingDatabase>().trainingDao() }
}