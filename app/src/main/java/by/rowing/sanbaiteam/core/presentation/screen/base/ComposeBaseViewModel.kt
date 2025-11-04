package by.rowing.sanbaiteam.core.presentation.screen.base

import by.rowing.sanbaiteam.core.presentation.screen.ComposeStateHolder
import by.rowing.sanbaiteam.core.presentation.screen.ComposeStateHolderImpl

abstract class ComposeBaseViewModel<T>(initialState: T) : BaseViewModel(),
    ComposeStateHolder<T> by ComposeStateHolderImpl(initialState) {

    protected var isResumed: Boolean = false

    open fun pause() {
        isResumed = false
    }

    open fun resume() {
        isResumed = true
    }
}