package com.checker.uikit3.modifier

import android.view.ViewTreeObserver
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

fun Modifier.debounceClickable(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    debounceDelayMs: Long = DEBOUNCE_DELAY_MILLIS,
    onClick: () -> Unit,
): Modifier = composed {
    return@composed this.debounceClickable(
        interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        indication = LocalIndication.current,
        enabled = enabled,
        debounceDelayMs = debounceDelayMs,
        onClick = onClick
    )
}

fun Modifier.debounceClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    debounceDelayMs: Long = DEBOUNCE_DELAY_MILLIS,
    onClick: () -> Unit,
): Modifier = composed {
    val clickableState = LocalClickableState.current

    return@composed this.clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClick = { clickableState.onClick(click = onClick, debounceDelayMs = debounceDelayMs) }
    )
}

fun Modifier.debounceClickableWithoutEffect(onClick: () -> Unit): Modifier = composed {
    return@composed this.debounceClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/**
 * Если действие по клику != null, то применяет debounceClickable со стандартными настройками.
 * Иначе - элемент не кликабелен.
 */
fun Modifier.clickableIfProvided(
    onClick: (() -> Unit)?,
): Modifier = if (onClick != null) debounceClickable(onClick = onClick) else this

inline fun Modifier.applyIfEnabled(
    enabled: Boolean?,
    builder: Modifier.() -> Modifier,
): Modifier = if (enabled == true) builder() else this

internal fun Modifier.clearFocusOnKeyboardDismiss(
    onKeyboardDismiss: () -> Unit = {},
): Modifier = composed {
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