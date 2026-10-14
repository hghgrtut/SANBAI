package by.rowing.sanbaiteam.data.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import by.rowing.sanbaiteam.core.data.local.RowingDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databaseModule: Module = module {
    single {
        val context = androidContext()
        val dbFile = context.getDatabasePath(RowingDatabase.DATABASE_NAME)
        Room.databaseBuilder<RowingDatabase>(
            context = context,
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(*RowingDatabase.MIGRATIONS)
            .build()
    }
    databaseDaos()
}
