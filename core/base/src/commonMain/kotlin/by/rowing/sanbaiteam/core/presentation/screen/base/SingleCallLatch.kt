package by.rowing.sanbaiteam.core.presentation.screen.base

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class SingleCallLatch {
    private val called = AtomicBoolean(false)

    fun considerCalling(action: () -> Unit) {
        if (!called.exchange(true)) action()
    }
}
