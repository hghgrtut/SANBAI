package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@AllowDetektPublic
object RowingTimeFormat {

    private const val MILLIS_PER_TENTH = 100L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_HOUR = 3_600_000L
    private const val TENTH_PER_SECOND = 10L
    private const val TENTH_PER_MINUTE = 600L
    private const val TENTH_PER_HOUR = 36_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val MINUTES_PER_HOUR = 60L
    private const val PACE_DISTANCE_METERS = 500
    private const val MAX_MINUTE_DIGITS = 3
    private const val MAX_SECOND_DIGITS = 2
    private const val MAX_TENTH_DIGITS = 1

    /**
     * Formats elapsed duration as `m:ss.t` or `mm:ss.t` (tenths of a second).
     */
    fun formatDuration(timeMillis: Long): String {
        if (timeMillis < 0) return "0:00.0"
        val totalTenths = (timeMillis + MILLIS_PER_TENTH / 2) / MILLIS_PER_TENTH
        val minutes = totalTenths / TENTH_PER_MINUTE
        val seconds = (totalTenths % TENTH_PER_MINUTE) / TENTH_PER_SECOND
        val tenths = totalTenths % TENTH_PER_SECOND
        return "$minutes:${seconds.toString().padStart(2, '0')}.$tenths"
    }

    /**
     * Keeps only characters valid for an `m:ss.t` input field: digits, a single `:` and a single `.`.
     * Before `:` a `.` is allowed as well, in which case the value is treated as seconds (`ss.t`).
     * Segment lengths are capped (up to 3 minute digits, 2 seconds, 1 tenth).
     */
    fun sanitizeTimeInput(text: String): String {
        val result = StringBuilder()
        var hasColon = false
        var hasDot = false
        var minuteDigits = 0
        var secondDigits = 0
        var tenthDigits = 0
        text.forEach { symbol ->
            when {
                symbol.isDigit() && hasDot && tenthDigits < MAX_TENTH_DIGITS -> {
                    tenthDigits++
                    result.append(symbol)
                }

                symbol.isDigit() && hasColon && secondDigits < MAX_SECOND_DIGITS -> {
                    secondDigits++
                    result.append(symbol)
                }

                symbol.isDigit() && !hasColon && !hasDot && minuteDigits < MAX_MINUTE_DIGITS -> {
                    minuteDigits++
                    result.append(symbol)
                }

                symbol == ':' && !hasColon && !hasDot && minuteDigits > 0 -> {
                    hasColon = true
                    result.append(symbol)
                }

                symbol == '.' && !hasDot && minuteDigits > 0 -> {
                    hasDot = true
                    result.append(symbol)
                }
            }
        }
        return result.toString()
    }

    /**
     * Parses `m:ss.t` / `m:ss` or, when no `:` is present, a plain seconds value `ss.t` / `ss`.
     * Seconds must be below 60.
     */
    fun parseTimeInput(input: String): Long? = input.trim().ifEmpty { null }?.let { trimmed ->
        parseDurationMmSsTenths(input = trimmed) ?: parseDurationMmSsTenths(input = "0:$trimmed")
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
        if (minutes >= MINUTES_PER_HOUR || seconds >= SECONDS_PER_MINUTE) return null
        return hours * MILLIS_PER_HOUR + minutes * MILLIS_PER_MINUTE + seconds * TimeUtils.MILLIS_PER_SECOND +
                tenths * MILLIS_PER_TENTH
    }

    fun formatDurationHhMmSsTenths(timeMillis: Long): String {
        if (timeMillis < 0) return "00:00:00.0"
        val totalTenths = (timeMillis + MILLIS_PER_TENTH / 2) / MILLIS_PER_TENTH
        val hours = totalTenths / TENTH_PER_HOUR
        val minutes = (totalTenths % TENTH_PER_HOUR) / TENTH_PER_MINUTE
        val seconds = (totalTenths % TENTH_PER_MINUTE) / TENTH_PER_SECOND
        val tenths = totalTenths % TENTH_PER_SECOND
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:" +
                "${seconds.toString().padStart(2, '0')}.$tenths"
    }

    fun paceMillis(
        timeMillis: Long,
        distanceMeters: Int
    ): Long? {
        if (distanceMeters <= 0 || timeMillis <= 0) return null
        return (timeMillis.toDouble() * PACE_DISTANCE_METERS / distanceMeters).roundToLong().roundToTenths()
    }

    fun formatPace(
        timeMillis: Long,
        distanceMeters: Int
    ): String? {
        val pace = paceMillis(timeMillis, distanceMeters) ?: return null
        return "${formatDuration(pace)} /500м"
    }

    fun parseStrokeRate(input: String): Double? {
        val value = input.trim().replace(',', '.').toDoubleOrNull() ?: return null
        if (value < 0) return null
        return (value * 10).roundToInt() / 10.0
    }

    /**
     * Parses `m:ss.t` / `mm:ss.t` or `m:ss` into millis rounded to tenths.
     */
    private fun parseDurationMmSsTenths(input: String): Long? {
        val matchResult = Regex("""^(\d+):(\d{1,2})\.(\d)$""").matchEntire(input) // `m:ss.t`
            ?: Regex("""^(\d+):(\d{1,2})$""").matchEntire(input) // `m:ss`
            ?: return null
        val minutes = matchResult.groupValues[1].toLong()
        val seconds = matchResult.groupValues[2].toLong()
        val tenths = matchResult.groupValues.getOrNull(3)?.toLong() ?: 0L
        return if (seconds >= SECONDS_PER_MINUTE) {
            null
        } else {
            minutes * MILLIS_PER_MINUTE + seconds * TimeUtils.MILLIS_PER_SECOND + tenths * MILLIS_PER_TENTH
        }
    }

    private fun Long.roundToTenths(): Long =
        ((this + MILLIS_PER_TENTH / 2) / MILLIS_PER_TENTH) * MILLIS_PER_TENTH
}
