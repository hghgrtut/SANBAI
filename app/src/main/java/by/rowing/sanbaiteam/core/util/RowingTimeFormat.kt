package by.rowing.sanbaiteam.core.util

import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object RowingTimeFormat {

    private const val MILLIS_PER_TENTH = 100L
    private const val MILLIS_PER_SECOND = 1_000L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_HOUR = 3_600_000L
    private const val TENTH_PER_SECOND = 10L
    private const val TENTH_PER_MINUTE = 600L
    private const val TENTH_PER_HOUR = 36_000L
    private const val PACE_DISTANCE_METERS = 500

    /**
     * Formats elapsed duration as `m:ss.t` or `mm:ss.t` (tenths of a second).
     */
    fun formatDuration(timeMillis: Long): String {
        if (timeMillis < 0) return "0:00.0"
        val totalTenths = (timeMillis + MILLIS_PER_TENTH / 2) / MILLIS_PER_TENTH
        val minutes = totalTenths / 600
        val seconds = (totalTenths % 600) / 10
        val tenths = totalTenths % 10
        return String.format(Locale.US, "%d:%02d.%d", minutes, seconds, tenths)
    }

    /**
     * Parses `m:ss.t` / `mm:ss.t` or `m:ss` into millis rounded to tenths.
     */
    fun parseDuration(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val withTenths = Regex("""^(\d+):(\d{1,2})\.(\d)$""").matchEntire(trimmed)
        if (withTenths != null) {
            val minutes = withTenths.groupValues[1].toLong()
            val seconds = withTenths.groupValues[2].toLong()
            val tenths = withTenths.groupValues[3].toLong()
            if (seconds >= 60) return null
            return minutes * MILLIS_PER_MINUTE + seconds * MILLIS_PER_SECOND + tenths * MILLIS_PER_TENTH
        }

        val withoutTenths = Regex("""^(\d+):(\d{1,2})$""").matchEntire(trimmed)
        if (withoutTenths != null) {
            val minutes = withoutTenths.groupValues[1].toLong()
            val seconds = withoutTenths.groupValues[2].toLong()
            if (seconds >= 60) return null
            return minutes * MILLIS_PER_MINUTE + seconds * MILLIS_PER_SECOND
        }

        return null
    }

    fun parseDurationHhMmSsTenths(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val match = Regex("""^(\d{1,2}):(\d{1,2}):(\d{1,2})(?:\.(\d))?$""").matchEntire(trimmed)
            ?: return null
        val hours = match.groupValues[1].toLong()
        val minutes = match.groupValues[2].toLong()
        val seconds = match.groupValues[3].toLong()
        val tenths = match.groupValues[4].takeIf { it.isNotEmpty() }?.toLong() ?: 0L
        if (minutes >= 60 || seconds >= 60) return null
        return hours * MILLIS_PER_HOUR + minutes * MILLIS_PER_MINUTE + seconds * MILLIS_PER_SECOND + tenths * MILLIS_PER_TENTH
    }

    fun paceMillis(timeMillis: Long, distanceMeters: Int): Long? {
        if (distanceMeters <= 0 || timeMillis <= 0) return null
        return (timeMillis.toDouble() * PACE_DISTANCE_METERS / distanceMeters).roundToLong().roundToTenths()
    }

    fun formatPace(timeMillis: Long, distanceMeters: Int): String? {
        val pace = paceMillis(timeMillis, distanceMeters) ?: return null
        return "${formatDuration(pace)} /500м"
    }

    fun formatStrokeRate(strokeRate: Double): String =
        String.format(Locale.US, "%.1f", strokeRate)

    fun parseStrokeRate(input: String): Double? {
        val value = input.trim().replace(',', '.').toDoubleOrNull() ?: return null
        if (value < 0) return null
        return (value * 10).roundToInt() / 10.0
    }

    private fun Long.roundToTenths(): Long =
        ((this + MILLIS_PER_TENTH / 2) / MILLIS_PER_TENTH) * MILLIS_PER_TENTH
}
