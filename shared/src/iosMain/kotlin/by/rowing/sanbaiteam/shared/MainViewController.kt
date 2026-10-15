package by.rowing.sanbaiteam.shared

import androidx.compose.ui.window.ComposeUIViewController
import by.rowing.sanbaiteam.athlete.di.athleteModule
import by.rowing.sanbaiteam.calculator.di.calculatorModule
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.data.di.databaseModule
import by.rowing.sanbaiteam.shared.di.sharedAppModule
import by.rowing.sanbaiteam.training.di.trainingModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

@AllowDetektPublic
fun mainViewController(): UIViewController {
    startKoin {
        modules(
            sharedAppModule,
            athleteModule,
            calculatorModule,
            databaseModule,
            trainingModule,
        )
    }
    return ComposeUIViewController { App() }
}
