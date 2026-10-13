package by.rowing.sanbaiteam.shared

import androidx.compose.ui.window.ComposeUIViewController
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import platform.UIKit.UIViewController

@AllowDetektPublic
fun mainViewController(): UIViewController = ComposeUIViewController { App() }
