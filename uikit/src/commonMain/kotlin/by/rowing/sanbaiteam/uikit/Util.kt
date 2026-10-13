package by.rowing.sanbaiteam.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.modifier.ClickableState
import by.rowing.sanbaiteam.uikit.modifier.LocalClickableState

@AllowDetektPublic
@Composable
fun ComposePreviewWrapper(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalClickableState provides ClickableState()
    ) {
        content.invoke()
    }
}
