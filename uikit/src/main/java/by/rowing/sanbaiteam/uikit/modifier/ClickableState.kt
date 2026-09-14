package by.rowing.sanbaiteam.uikit.modifier

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal const val DEBOUNCE_DELAY_MILLIS = 300L

@AllowDetektPublic
class ClickableState {

    var enabled: MutableState<Boolean> = mutableStateOf(true)
        private set

    fun onClick(
        debounceDelayMs: Long = DEBOUNCE_DELAY_MILLIS,
        click: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            if (enabled.value) {
                enabled.value = false
                click.invoke()
                delay(debounceDelayMs.milliseconds)
                enabled.value = true
            }
        }
    }
}