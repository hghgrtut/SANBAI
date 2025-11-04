package by.rowing.sanbaiteam.core.presentation.screen.base

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel(), LifecycleEventObserver {

    private val viewCreatedLatch = SingleCallLatch()

    @CallSuper
    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event
    ) {
        if (event == Lifecycle.Event.ON_CREATE) {
            viewCreatedLatch.considerCalling { onCreated(source) }
        }
    }

    /** Функция будет вызвана при первом создании вью */
    open fun onCreated(source: LifecycleOwner) {
        // Nothing by default
    }
}