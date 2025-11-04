package com.checker.uikit3.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
internal fun Modifier.translateShimmer(
    shape: Shape,
    staticColor: Color,
    activeColor: Color,
    shimmerWidth: Float,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = InfiniteTransitionLabel)

    val transitionInitialValue = -2 * shimmerWidth
    val transitionTargetValue = 2 * shimmerWidth
    val translateAnimationValue by transition.animateFloat(
        initialValue = transitionInitialValue,
        targetValue = transitionTargetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = TranslateAnimationDurationMillis,
                delayMillis = TranslateAnimationDelayMillis,
                easing = LinearEasing
            )
        ),
        label = TranslateAnimationLabel
    )
    val brushColors = listOf(
        staticColor,
        activeColor,
        staticColor
    )
    val brushStart = Offset(
        x = translateAnimationValue,
        y = shimmerWidth
    )
    val brushEnd = Offset(
        x = translateAnimationValue + shimmerWidth,
        y = shimmerWidth
    )
    val brush = Brush.linearGradient(
        colors = brushColors,
        start = brushStart,
        end = brushEnd
    )
    Modifier.background(
        brush = brush,
        shape = shape
    )
}

private const val InfiniteTransitionLabel = "InfiniteTransitionLabel"
private const val TranslateAnimationLabel = "TranslateAnimationLabel"

private const val TranslateAnimationDurationMillis = 1200
private const val TranslateAnimationDelayMillis = 200