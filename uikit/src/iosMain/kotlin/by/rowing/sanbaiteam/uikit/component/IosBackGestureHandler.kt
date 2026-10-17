package by.rowing.sanbaiteam.uikit.component

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIPanGestureRecognizer
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
object IosBackGestureHandler {

    private var currentOnBack: (() -> Unit)? = null
    private var gestureTarget: BackGestureTarget? = null
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
        val target = BackGestureTarget { currentOnBack?.invoke() }
        gestureTarget = target
        val recognizer = UIScreenEdgePanGestureRecognizer(
            target = target,
            action = NSSelectorFromString("handlePan:"),
        )
        recognizer.edges = UIRectEdgeLeft
        recognizer.cancelsTouchesInView = false
        view.addGestureRecognizer(recognizer)
    }
}

/**
 * Наследник NSObject должен быть top-level-классом: вложенные объекты,
 * наследующие ObjC-типы, не поддерживаются бэкендом Kotlin/Native.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class BackGestureTarget(
    private val onBackTriggered: () -> Unit,
) : NSObject() {

    @ObjCAction
    fun handlePan(recognizer: UIPanGestureRecognizer) {
        if (recognizer.state == UIGestureRecognizerStateEnded) {
            val view = recognizer.view ?: return
            val translation: CValue<CGPoint> = recognizer.translationInView(view)
            val x = translation.useContents { x }
            if (x > 50.0) {
                onBackTriggered()
            }
        }
    }
}
