package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@Composable
@AllowDetektPublic
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    DisposableEffect(enabled, onBack) {
        if (enabled) {
            IosBackGestureHandler.register(onBack)
        }
        onDispose {
            IosBackGestureHandler.unregister()
        }
    }
}
