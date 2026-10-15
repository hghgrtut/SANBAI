package by.rowing.sanbaiteam.uikit.component

import platform.Foundation.NSObject
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIPanGestureRecognizer
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIView

object IosBackGestureHandler {

    private var currentOnBack: (() -> Unit)? = null
    private var gestureTarget: PanGestureTarget? = null
    private var isAttached = false

    fun register(onBack: () -> Unit) {
        currentOnBack = onBack
    }

    fun unregister() {
        currentOnBack = null
    }

    fun attachToView(view: UIView) {
        if (isAttached) return
        isAttached = true
        val target = PanGestureTarget()
        gestureTarget = target
        val recognizer = UIScreenEdgePanGestureRecognizer(
            target = target,
            action = NSSelectorFromString("handlePan:"),
        )
        recognizer.edges = UIRectEdgeLeft
        recognizer.cancelsTouchesInView = false
        view.addGestureRecognizer(recognizer)
    }

    private class PanGestureTarget : NSObject() {
        @Override
        fun handlePan(recognizer: UIPanGestureRecognizer) {
            if (recognizer.state == UIGestureRecognizerStateEnded) {
                val view = recognizer.view ?: return
                val translation = recognizer.translationInView(view)
                if (translation.x > 50) {
                    currentOnBack?.invoke()
                }
            }
        }
    }
}
