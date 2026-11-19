package by.rowing.sanbaiteam.core.file.di

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import org.koin.core.module.Module
import org.koin.dsl.module

@AllowDetektPublic
val coreFileModule: Module = module {
    includes(filePlatformModule)
}

internal expect val filePlatformModule: Module