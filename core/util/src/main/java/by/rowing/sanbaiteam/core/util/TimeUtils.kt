package by.rowing.sanbaiteam.core.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

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
}