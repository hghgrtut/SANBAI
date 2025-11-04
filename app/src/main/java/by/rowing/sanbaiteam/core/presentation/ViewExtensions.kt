package by.rowing.sanbaiteam.core.presentation

import android.widget.ImageView
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
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
) = this.apply {
    isForceDarkAllowed = false

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

inline fun <reified K : Any, V> LazyListScope.items(
    map: Map<K, V>,
    noinline key: (Map.Entry<K, V>) -> Any = { it.key },
    crossinline itemContent: @Composable (LazyItemScope.(Map.Entry<K, V>) -> Unit),
): Unit = items(
    items = map.entries.toList(),
    key = key,
    itemContent = itemContent
)

inline fun ImageView.load(
    data: Any?,
    imageLoader: ImageLoader,
    builder: ImageRequest.Builder.() -> Unit = {},
): Disposable {
    val request = ImageRequest.Builder(context)
        .data(data)
        .target(this)
        .apply(builder)
        .build()
    return imageLoader.enqueue(request)
}
