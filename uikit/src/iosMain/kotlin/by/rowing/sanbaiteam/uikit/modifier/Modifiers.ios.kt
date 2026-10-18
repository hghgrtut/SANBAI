package by.rowing.sanbaiteam.uikit.modifier

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIKeyboardWillHideNotification
import platform.UIKit.UIKeyboardWillShowNotification

internal actual fun Modifier.clearFocusOnKeyboardDismiss(onKeyboardDismiss: () -> Unit): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        val showObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            UIKeyboardWillShowNotification,
            null,
            null,
        ) { _ ->
            keyboardAppearedSinceLastFocused = true
        }
        val hideObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            UIKeyboardWillHideNotification,
            null,
            null,
        ) { _ ->
            if (keyboardAppearedSinceLastFocused) {
                focusManager.clearFocus()
                onKeyboardDismiss()
                keyboardAppearedSinceLastFocused = false
            }
        }
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(showObserver)
            NSNotificationCenter.defaultCenter.removeObserver(hideObserver)
        }
    }
    onFocusEvent {
        if (isFocused != it.isFocused) {
            isFocused = it.isFocused
            if (isFocused) {
                keyboardAppearedSinceLastFocused = false
            }
        }
    }
}