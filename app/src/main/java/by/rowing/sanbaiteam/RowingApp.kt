package by.rowing.sanbaiteam

import android.app.Application
import by.rowing.sanbaiteam.athlete.di.athleteModule
import by.rowing.sanbaiteam.calculator.di.calculatorModule
import by.rowing.sanbaiteam.data.di.databaseModule
import by.rowing.sanbaiteam.di.appModule
import by.rowing.sanbaiteam.training.di.trainingModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RowingApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RowingApp)
            modules(appModule, athleteModule, calculatorModule, databaseModule, trainingModule)
        }
    }
}