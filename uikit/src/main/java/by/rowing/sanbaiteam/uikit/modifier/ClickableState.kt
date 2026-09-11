package com.checker.uikit3.modifier

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

const val DEBOUNCE_DELAY_MILLIS = 300L

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