package by.rowing.sanbaiteam.uikit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.modifier.ClickableState
import by.rowing.sanbaiteam.uikit.modifier.LocalClickableState

@AllowDetektPublic
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComposePreviewWrapper(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalClickableState provides ClickableState(),
        LocalOverscrollFactory provides null
    ) {
        content.invoke()
    }
}