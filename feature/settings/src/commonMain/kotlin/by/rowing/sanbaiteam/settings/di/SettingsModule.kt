package by.rowing.sanbaiteam.settings.di

import by.rowing.sanbaiteam.settings.data.repository.AppThemeSettingsRepository
import by.rowing.sanbaiteam.settings.data.repository.WorkDisplaySettingsRepository
import by.rowing.sanbaiteam.settings.presentation.SettingsViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    single<Settings> { Settings() }

    singleOf(::WorkDisplaySettingsRepository)
    singleOf(::AppThemeSettingsRepository)

    viewModelOf(::SettingsViewModel)
}