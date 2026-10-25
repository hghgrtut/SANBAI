package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TENTHS_IN_ONE = 10.0

/** Formats a value as a decimal with a single fraction digit (locale-independent), e.g. `12.3`. */
@AllowDetektPublic
fun formatTenths(value: Double): String {
    val scaled = (value * 10).roundToInt()
    val sign = if (scaled < 0) "-" else ""
    val magnitude = abs(scaled)
    return "$sign${magnitude / 10}.${magnitude % 10}"
}

@AllowDetektPublic
fun Double.roundToTenths(): Double = (this * TENTHS_IN_ONE).roundToInt() / TENTHS_IN_ONE