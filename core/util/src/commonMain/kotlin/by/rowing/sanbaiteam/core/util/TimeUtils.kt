package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

@AllowDetektPublic
object TimeUtils {

    const val MILLIS_PER_SECOND = 1_000L
    private const val MILLIS_PER_DAY = 86_400_000L
    private const val DATE_DIGITS_LENGTH = 8

    private val SHORT_MONTH_NAMES = listOf(
        "янв", "фев", "мар", "апр", "май", "июн",
        "июл", "авг", "сен", "окт", "ноя", "дек",
    )

    fun formatDate(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.day.toString().padStart(2, '0')
        val month = date.month.number.toString().padStart(2, '0')
        return "$day.$month.${date.year}"
    }

    fun formatDateWithMonthName(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.day.toString().padStart(2, '0')
        val month = SHORT_MONTH_NAMES[date.month.number - 1]
        return "$day $month ${date.year}"
    }

    fun formatDateDigits(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.day.toString().padStart(2, '0')
        val month = date.month.number.toString().padStart(2, '0')
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

    fun isSameCalendarDay(
        firstMillis: Long,
        secondMillis: Long,
    ): Boolean = firstMillis.toLocalDate() == secondMillis.toLocalDate()

    /** Полных лет на текущий момент.
     */
    fun ageInYearsNow(birthTimeInMillis: Long): Int {
        val birth = birthTimeInMillis.toLocalDate()
        val now = nowInMillis().toLocalDate()
        val yearDiff = now.year - birth.year
        val hasHadBirthday = now.month.number > birth.month.number ||
                (now.month.number == birth.month.number && now.day >= birth.day)
        return if (hasHadBirthday) yearDiff else yearDiff - 1
    }

    /**
     * Локальная для epoch-миллисекунд.
     *
     * Намеренно не использует `Instant`: обращение к `Instant.Companion` не резолвится
     * в iOS-сборке. Вместо этого берём UTC-дату по epoch-дням и корректируем её на ±1 день,
     * сравнивая границы суток в нужной таймзоне.
     */
    private fun Long.toLocalDate(): LocalDate {
        val zone = TimeZone.currentSystemDefault()
        var date = LocalDate.fromEpochDays(floorDiv(MILLIS_PER_DAY).toInt())
        if (date.atStartOfDayIn(zone).toEpochMilliseconds() > this) {
            date = date.minus(1, DateTimeUnit.DAY)
        } else if (date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone).toEpochMilliseconds() <= this) {
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return date
    }
}

@AllowDetektPublic
expect fun nowInMillis(): Long
