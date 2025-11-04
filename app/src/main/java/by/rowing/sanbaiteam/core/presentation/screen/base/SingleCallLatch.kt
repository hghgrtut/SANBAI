package by.rowing.sanbaiteam.core.presentation.screen.base

import java.util.concurrent.atomic.AtomicBoolean

class SingleCallLatch {
    private val called = AtomicBoolean()

    fun considerCalling(action: () -> Unit) {
        if (called.getAndSet(true).not()) action()
    }
}
