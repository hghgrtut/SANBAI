package by.rowing.sanbaiteam.settings.di

import by.rowing.sanbaiteam.settings.data.repository.WorkDisplaySettingsRepository
import by.rowing.sanbaiteam.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    singleOf(::WorkDisplaySettingsRepository)

    viewModelOf(::SettingsViewModel)
}