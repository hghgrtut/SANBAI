package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@AllowDetektPublic
object TimeUtils {

    const val MILLIS_PER_SECOND = 1_000L

    fun formatDate(timeInMillis: Long): String {
        val date = Instant.fromEpochMilliseconds(timeInMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        return "$day.$month.${date.year}"
    }

    fun isSameCalendarDay(
        firstMillis: Long,
        secondMillis: Long,
    ): Boolean {
        val first = Instant.fromEpochMilliseconds(firstMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        val second = Instant.fromEpochMilliseconds(secondMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return first == second
    }
}
