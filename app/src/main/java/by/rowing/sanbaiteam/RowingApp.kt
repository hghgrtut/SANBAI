package by.rowing.sanbaiteam

import android.app.Application
import by.rowing.sanbaiteam.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RowingApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RowingApp)
            modules(appModule)
        }
    }
}