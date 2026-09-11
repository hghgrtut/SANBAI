package by.rowing.sanbaiteam.uikit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.checker.uikit3.modifier.ClickableState
import com.checker.uikit3.modifier.LocalClickableState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComposePreviewWrapper(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalClickableState provides ClickableState(),
        LocalOverscrollConfiguration provides null
    ) {
        content.invoke()
    }
}