package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AllowDetektPublic
object TimeUtils {

    const val MILLIS_PER_SECOND = 1_000L

    private const val DATE_FORMAT = "dd.MM.yyyy"

    private val DEFAULT_TIME_ZONE: TimeZone by lazy { TimeZone.getDefault() }

    fun formatMillisToString(
        timeInMillis: Long,
        format: String? = DATE_FORMAT,
        timeZone: TimeZone = DEFAULT_TIME_ZONE,
    ): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = timeZone

        return dateFormat.format(timeInMillis)
    }

    fun isSameCalendarDay(
        firstMillis: Long,
        secondMillis: Long,
    ): Boolean {
        val first = Calendar.getInstance().apply { timeInMillis = firstMillis }
        val second = Calendar.getInstance().apply { timeInMillis = secondMillis }
        val fieldsToCompare = listOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
        return fieldsToCompare.all { field -> first.get(field) == second.get(field) }
    }
}