package by.rowing.sanbaiteam.athlete.di

import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepositoryImpl
import by.rowing.sanbaiteam.athlete.presentation.add.AddAthleteViewModel
import by.rowing.sanbaiteam.athlete.presentation.detail.AthleteDetailViewModel
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val athleteModule = module {
    singleOf(::AthletesRepositoryImpl) bind AthletesRepository::class

    viewModelOf(::AthletesViewModel)
    viewModelOf(::AddAthleteViewModel)
    viewModelOf(::AthleteDetailViewModel)
}