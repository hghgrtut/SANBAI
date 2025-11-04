package by.rowing.sanbaiteam.core.presentation.compose

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No LocalNavController provided")
}