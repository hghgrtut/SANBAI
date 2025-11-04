package com.checker.uikit3.modifier

import android.view.ViewTreeObserver
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import by.rowing.sanbaiteam.uikit.theme.ColorPalette

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

fun Modifier.fadeBorders(
    color: Color,
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
): Modifier = drawWithContent {
    drawContent()

    val borders = mutableListOf<Pair<Offset, Offset>>()
    start?.let { start ->
        borders.add(Offset.Zero to Offset(start.toPx(), 0f))
    }
    top?.let { top ->
        borders.add(Offset.Zero to Offset(0f, top.toPx()))
    }
    end?.let { end ->
        borders.add(Offset(size.width, 0f) to Offset(size.width - end.toPx(), 0f))
    }
    bottom?.let { bottom ->
        borders.add(Offset(0f, size.height) to Offset(0f, size.height - bottom.toPx()))
    }

    borders.forEach { border ->
        val brush = Brush.linearGradient(
            colors = listOf(color, ColorPalette.Transparent),
            start = border.first,
            end = border.second
        )
        drawRect(
            brush = brush,
            blendMode = BlendMode.SrcOver
        )
    }
}

fun Modifier.rectRipple(
    interactionSource: InteractionSource,
    cornerRadius: Dp = 16.dp,
    extraPadding: Dp = 8.dp,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()

    val progress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(
            durationMillis = RECT_RIPPLE_DURATION_MILLIS,
            easing = FastOutSlowInEasing
        ),
        label = "rectRippleProgress"
    )

    val density = LocalDensity.current
    val cornerPx = with(density) { cornerRadius.toPx() }
    val extraPx = with(density) { extraPadding.toPx() }

    drawWithContent {
        drawContent()

        if (progress > 0f) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(
                    x = -extraPx,
                    y = -extraPx
                ),
                size = Size(
                    width = size.width + (extraPx * 2),
                    height = size.height + (extraPx * 2)
                ),
                cornerRadius = CornerRadius(
                    x = cornerPx,
                    y = cornerPx
                ),
                alpha = progress
            )
        }
    }
}

private const val RECT_RIPPLE_DURATION_MILLIS = 200