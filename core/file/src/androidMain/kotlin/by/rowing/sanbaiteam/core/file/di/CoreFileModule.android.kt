package by.rowing.sanbaiteam.core.file.di

import by.rowing.sanbaiteam.core.file.AndroidTextFileAccess
import by.rowing.sanbaiteam.core.file.TextFileAccess
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val filePlatformModule: Module = module {
    single<TextFileAccess> { AndroidTextFileAccess(context = androidContext()) }
}