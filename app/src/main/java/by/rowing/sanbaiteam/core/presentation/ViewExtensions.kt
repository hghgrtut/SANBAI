package by.rowing.sanbaiteam.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.checker.uikit3.modifier.ClickableState
import com.checker.uikit3.modifier.LocalClickableState

fun ComposeView.runWithCompose(
    strategy: ViewCompositionStrategy = ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
    parentCompositionContext: CompositionContext? = null,
    imageLoader: ImageLoader? = null,
    clickableState: ClickableState,
    useLightStatusBars: Boolean = true,
    useLightNavigationBars: Boolean = true,
    content: @Composable () -> Unit,
) = apply {
    setViewCompositionStrategy(strategy = strategy)

    if (parentCompositionContext != null) {
        setParentCompositionContext(parent = parentCompositionContext)
    }

    val providerValues: List<ProvidedValue<*>> = buildList {
        add(LocalClickableState provides clickableState)
        imageLoader?.let { add(LocalImageLoader provides imageLoader) }
    }

    setContent {
        CompositionLocalProvider(*providerValues.toTypedArray()) {
            SANBAITeamTheme(
                useLightStatusBars = useLightStatusBars,
                useLightNavigationBars = useLightNavigationBars,
            ) {
                content.invoke()
            }
        }
    }
}