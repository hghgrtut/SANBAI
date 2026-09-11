package by.rowing.sanbaiteam.core.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer

@Composable
inline fun <reified T> NavHostController.CollectArgResult(
    argName: String,
    initialValue: T,
    crossinline onResult: (T) -> Unit,
) {
    val savedStateHandle: SavedStateHandle? = this.currentBackStackEntry?.savedStateHandle
    val result = savedStateHandle?.getStateFlow(argName, initialValue)?.collectAsState(null)

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isResumed, result?.value) {
        if (isResumed) {
            result?.value?.let { _ ->
                onResult.invoke(result.value as T)
                savedStateHandle[argName] = null
                this@CollectArgResult.currentBackStackEntry?.savedStateHandle?.remove<T>(argName)
            }
        }
    }
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
fun NavigationDestination<*>.getClassName(): String = this::class.serializer().descriptor.serialName