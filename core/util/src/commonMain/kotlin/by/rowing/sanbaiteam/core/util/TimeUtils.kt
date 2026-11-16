package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

@AllowDetektPublic
object TimeUtils {

    const val MILLIS_PER_TENTH = 100L
    const val MILLIS_PER_SECOND = 1_000L
    const val SECONDS_PER_MINUTE = 60L

    const val MILLIS_PER_DAY = 86_400_000L
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

    /** `dd.MM.yy` — компактный формат для таблиц. */
    fun formatDateShort(timeInMillis: Long): String {
        val year = (timeInMillis.toLocalDate().year % 100).toString().padStart(2, '0')
        return "${formatDayMonth(timeInMillis)}.$year"
    }

    /** `dd.MM` — подписи оси дат на графике. */
    fun formatDayMonth(timeInMillis: Long): String {
        val date = timeInMillis.toLocalDate()
        val day = date.day.toString().padStart(2, '0')
        val month = date.month.number.toString().padStart(2, '0')
        return "$day.$month"
    }

    fun parseDateDigits(digits: String): Long? {
        if (digits.length != DATE_DIGITS_LENGTH || !digits.all { it.isDigit() }) return null
        val day = digits.substring(0, 2).toInt()
        val month = digits.substring(2, 4).toInt()
        val year = digits.substring(4, 8).toInt()
        return startOfDayMillis(
            year = year,
            month = month,
            day = day
        )
    }

    fun startOfDayMillis(
        year: Int,
        month: Int,
        day: Int
    ): Long? = try {
        startOfDayMillis(
            localDate = LocalDate(
                year = year,
                month = month,
                day = day
            )
        )
    } catch (_: IllegalArgumentException) {
        null
    }

    fun endOfDayMillis(
        year: Int,
        month: Int,
        day: Int
    ): Long? = try {
        startOfDayMillis(
            localDate = LocalDate(
                year = year,
                month = month,
                day = day
            ).plus(
                value = 1,
                unit = DateTimeUnit.DAY
            )
        ) - 1
    } catch (_: IllegalArgumentException) {
        null
    }

    fun isSameCalendarDay(
        firstMillis: Long,
        secondMillis: Long,
    ): Boolean = firstMillis.toLocalDate() == secondMillis.toLocalDate()

    /** Полных лет на текущий момент. */
    fun ageInYearsNow(birthTimeInMillis: Long): Int {
        val birth = birthTimeInMillis.toLocalDate()
        val now = nowInMillis().toLocalDate()
        val yearDiff = now.year - birth.year
        val hasHadBirthday = now.month.number > birth.month.number ||
                (now.month.number == birth.month.number && now.day >= birth.day)
        return if (hasHadBirthday) yearDiff else yearDiff - 1
    }

    fun startOfDayMillis(timeInMillis: Long): Long = startOfDayMillis(localDate = timeInMillis.toLocalDate())

    /** Полдень локального дня — защита от сдвига даты при смене таймзоны. */
    fun noonOfDay(timeInMillis: Long): Long = noonOfLocalDate(timeInMillis.toLocalDate())

    fun noonMillis(
        year: Int,
        month: Int,
        day: Int,
    ): Long? = try {
        noonOfLocalDate(LocalDate(year, month, day))
    } catch (_: IllegalArgumentException) {
        null
    }

    fun plusDaysMillis(
        timeInMillis: Long,
        days: Int,
    ): Long = startOfDayMillis(
        localDate = timeInMillis.toLocalDate().plus(
            value = days,
            unit = DateTimeUnit.DAY
        )
    )

    fun daysBetween(
        fromMillis: Long,
        toMillis: Long,
    ): Int = (toMillis.toLocalDate().toEpochDays() - fromMillis.toLocalDate().toEpochDays()).toInt()

    private fun noonOfLocalDate(date: LocalDate): Long = LocalDateTime(
        year = date.year,
        month = date.month.number,
        day = date.day,
        hour = 12,
        minute = 0,
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    private fun startOfDayMillis(localDate: LocalDate): Long =
        localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    /**
     * Локальная для epoch-миллисекунд.
     *
     * Намеренно не использует `Instant`: обращение к `Instant.Companion` не резолвится
     * в iOS-сборке. Вместо этого берём UTC-дату по epoch-дням и корректируем её на ±1 день,
     * сравнивая границы суток в нужной таймзоне.
     */
    private fun Long.toLocalDate(): LocalDate {
        val date = LocalDate.fromEpochDays(floorDiv(MILLIS_PER_DAY).toInt())
        return when {
            startOfDayMillis(localDate = date) > this -> date.minus(1, DateTimeUnit.DAY)
            startOfDayMillis(localDate = date.plus(1, DateTimeUnit.DAY)) <= this ->
                date.plus(1, DateTimeUnit.DAY)
            else -> date
        }
    }
}

@AllowDetektPublic
expect fun nowInMillis(): Long