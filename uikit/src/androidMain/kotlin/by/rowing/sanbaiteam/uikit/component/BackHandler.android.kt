package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@Composable
@AllowDetektPublic
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(
        enabled = enabled,
        onBack = onBack
    )
}