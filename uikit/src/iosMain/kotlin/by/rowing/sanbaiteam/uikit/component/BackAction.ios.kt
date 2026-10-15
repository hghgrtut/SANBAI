package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

@Composable
internal actual fun defaultBackAction(): () -> Unit {
    return {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        findNavigationController(root)?.popViewControllerAnimated(true)
    }
}

private fun findNavigationController(viewController: UIViewController?): UINavigationController? {
    if (viewController == null) return null
    if (viewController is UINavigationController) return viewController
    return findNavigationController(viewController.presentedViewController)
}
