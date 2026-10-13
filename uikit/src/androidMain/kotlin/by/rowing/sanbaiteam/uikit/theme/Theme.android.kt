package by.rowing.sanbaiteam.uikit.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
internal actual fun PlatformSystemBars(
    useLightStatusBars: Boolean,
    useLightNavigationBars: Boolean,
) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return
    SideEffect {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useLightStatusBars
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = useLightNavigationBars
    }
}
