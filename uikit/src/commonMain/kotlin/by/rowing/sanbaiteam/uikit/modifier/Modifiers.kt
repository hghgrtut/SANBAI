package by.rowing.sanbaiteam.uikit.modifier

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

internal fun Modifier.debounceClickable(
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

internal fun Modifier.debounceClickable(
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
        onClick = {
            clickableState.onClick(
                click = onClick,
                debounceDelayMs = debounceDelayMs
            )
        }
    )
}

internal fun Modifier.debounceClickableWithoutEffect(onClick: () -> Unit): Modifier = composed {
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
internal fun Modifier.clickableIfProvided(
    onClick: (() -> Unit)?,
): Modifier = if (onClick != null) debounceClickable(onClick = onClick) else this

internal inline fun Modifier.applyIfEnabled(
    enabled: Boolean?,
    builder: Modifier.() -> Modifier,
): Modifier = if (enabled == true) builder() else this

/**
 * Сбрасывает фокус при скрытии экранной клавиатуры.
 * Платформенная реализация: на Android отслеживает видимость IME, на iOS — no-op.
 */
internal expect fun Modifier.clearFocusOnKeyboardDismiss(
    onKeyboardDismiss: () -> Unit = {},
): Modifier
