package by.rowing.sanbaiteam.training.presentation.detail.compose

import kotlin.math.abs
import kotlin.math.roundToInt

internal fun formatSignedInt(value: Int): String {
    if (value == 0) return "0"
    val sign = if (value > 0) "+" else "−"
    return "$sign${abs(value)}"
}

internal fun formatSignedTenths(value: Double): String {
    val scaled = (value * 10).roundToInt()
    if (scaled == 0) return "0"
    val sign = if (scaled > 0) "+" else "−"
    val magnitude = abs(scaled)
    return "$sign${magnitude / 10}.${magnitude % 10}"
}

internal fun formatSignedSeconds(deltaMillis: Long): String =
    formatSignedTenths(deltaMillis / 1000.0)
