package by.rowing.sanbaiteam.core.file.di

import by.rowing.sanbaiteam.core.file.IosTextFileAccess
import by.rowing.sanbaiteam.core.file.TextFileAccess
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val filePlatformModule: Module = module {
    single<TextFileAccess> { IosTextFileAccess() }
}