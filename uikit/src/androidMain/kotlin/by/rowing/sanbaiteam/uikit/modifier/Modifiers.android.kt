package by.rowing.sanbaiteam.uikit.modifier

import android.view.ViewTreeObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat

internal actual fun Modifier.clearFocusOnKeyboardDismiss(onKeyboardDismiss: () -> Unit): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }

    val rootView = LocalView.current.rootView
    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) {
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rootInsets = rootView.rootWindowInsets
            val windowInsetsCompat = WindowInsetsCompat.toWindowInsetsCompat(rootInsets, rootView)
            val imeIsVisible = windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime())
            if (imeIsVisible) {
                keyboardAppearedSinceLastFocused = true
            } else if (keyboardAppearedSinceLastFocused) {
                focusManager.clearFocus()
                onKeyboardDismiss()
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
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