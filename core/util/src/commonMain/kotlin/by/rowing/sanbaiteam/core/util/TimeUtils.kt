package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@AllowDetektPublic
object TimeUtils {

    const val MILLIS_PER_SECOND = 1_000L
    private const val DATE_DIGITS_LENGTH = 8

    private val SHORT_MONTH_NAMES = listOf(
        "янв", "фев", "мар", "апр", "май", "июн",
        "июл", "авг", "сен", "окт", "ноя", "дек",
    )

    fun formatDate(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        return "$day.$month.${date.year}"
    }

    fun formatDateWithMonthName(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = SHORT_MONTH_NAMES[date.monthNumber - 1]
        return "$day $month ${date.year}"
    }

    fun formatDateDigits(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        return "$day$month${date.year}"
    }

    fun parseDateDigits(digits: String): Long? {
        if (digits.length != DATE_DIGITS_LENGTH || !digits.all { it.isDigit() }) return null
        val day = digits.substring(0, 2).toInt()
        val month = digits.substring(2, 4).toInt()
        val year = digits.substring(4, 8).toInt()
        return try {
            LocalDate(year, month, day)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

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

@AllowDetektPublic
expect fun nowInMillis(): Long
