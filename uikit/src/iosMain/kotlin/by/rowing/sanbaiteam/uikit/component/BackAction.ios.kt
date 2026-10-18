package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

@Composable
internal actual fun defaultBackAction(): () -> Unit = {
    findNavigationController(viewController = UIApplication.sharedApplication.keyWindow?.rootViewController)
        ?.popViewControllerAnimated(animated = true)
}

private fun findNavigationController(viewController: UIViewController?): UINavigationController? =
    if (viewController == null || viewController is UINavigationController) {
        viewController
    } else {
        findNavigationController(viewController = viewController.presentedViewController)
    }
