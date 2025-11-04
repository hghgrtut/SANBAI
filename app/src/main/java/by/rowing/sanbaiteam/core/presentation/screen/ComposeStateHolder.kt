package by.rowing.sanbaiteam.core.presentation.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface ComposeStateHolder<S> {

    val state: S

    fun changeState(action: S.() -> S)
}

class ComposeStateHolderImpl<S>(initialState: S) : ComposeStateHolder<S> {

    override var state: S by mutableStateOf(initialState)

    override fun changeState(action: S.() -> S) {
        val newState = action(state)
        if (newState != state) {
            state = newState
        }
    }
}
