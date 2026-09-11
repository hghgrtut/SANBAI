package by.rowing.sanbaiteam.core.util

import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object PersonalBestPercent {

    const val DEFAULT_PB_DISTANCE_METERS = 2000

    /**
     * Percent of personal-best average speed:
     * `(pieceSpeed / pbSpeed) * 100`.
     */
    fun calculate(
        pieceDistanceMeters: Int,
        pieceTimeMillis: Long,
        pbDistanceMeters: Int,
        pbTimeMillis: Long,
    ): Double? {
        if (pieceDistanceMeters <= 0 || pieceTimeMillis <= 0 || pbDistanceMeters <= 0 || pbTimeMillis <= 0) return null
        val percent = pieceDistanceMeters.toDouble() * pbTimeMillis / (pieceTimeMillis * pbDistanceMeters) * 100.0
        return (percent * 10).roundToInt() / 10.0
    }

    /**
     * Percent of record speed when both values are paces (/500м).
     * Faster pace (smaller time) → higher percent: `(recordPace / pace) * 100`.
     */
    fun percentFromPaces(recordPaceMillis: Long, paceMillis: Long): Double? {
        if (recordPaceMillis <= 0 || paceMillis <= 0) return null
        val percent = recordPaceMillis.toDouble() / paceMillis.toDouble() * 100.0
        return (percent * 10).roundToInt() / 10.0
    }

    /**
     * Pace (/500м) that corresponds to [percent] of record pace speed.
     */
    fun paceFromPercent(recordPaceMillis: Long, percent: Double): Long? {
        if (recordPaceMillis <= 0 || percent <= 0) return null
        val pace = (recordPaceMillis.toDouble() * 100.0 / percent).roundToLong()
        return ((pace + 50) / 100) * 100
    }

    fun format(percent: Double?): String =
        percent?.let { String.format(Locale.US, "%.1f%%", it) } ?: "—"

    fun average(percents: Collection<Double>): Double? {
        if (percents.isEmpty()) return null
        val avg = percents.sum() / percents.size
        return (avg * 10).roundToInt() / 10.0
    }
}
