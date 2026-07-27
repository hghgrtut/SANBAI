package by.rowing.sanbaiteam.core.util

import android.text.format.DateUtils
import android.util.TimeFormatException
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

    const val MINUTES_SECONDS_PATTERN = "mm:ss"
    private const val RUSSIAN_LOCALE = "ru"
    private const val DATE_FORMAT = "dd.MM.yyyy"
    private const val FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val TIME_ZONE_ID_UTC = "UTC"
    private const val DATE_TIME_PATTERN = "yyyy.MM.dd HH:mm:ss"

    private const val LAST_HOUR = 23
    private const val LAST_MINUTE = 59
    private const val LAST_SECOND = 59
    private const val LAST_MILLISECOND = 999

    private const val EMPTY_TIME = 0L

    const val ONE_DAY_IN_MILLIS = 1000 * 60 * 60 * 24
    const val TIME_FORMAT = "HH:mm"
    const val DAY_MONTH_FORMAT = "d MMMM"
    const val ALARM_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    const val COMPLAINT_FORMAT = "от d MMMM yyyy"
    const val PHOTO_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss"

    private val UTC: TimeZone by lazy { TimeZone.getTimeZone(TIME_ZONE_ID_UTC) }
    private val DEFAULT_TIME_ZONE: TimeZone by lazy { TimeZone.getDefault() }

    private const val UTC_0 = "UTC+0:00"

    fun getTimeMillis(timeZoneId: String = UTC_0): Long {
        val timeZone = TimeZone.getTimeZone(timeZoneId)
        return Calendar.getInstance(timeZone).timeInMillis
    }

    fun getCurrentTimestampLocal() = Calendar.getInstance().timeInMillis

    fun toMillisecondsLocal(millisecondsUtc: Long): Long =
        toCalendarLocal(getCalendarUtcFromMillis(millisecondsUtc)).timeInMillis

    fun toTimestampUtc(timestampTo: Long): Long {
        val str = formatMillis(
            milliseconds = timestampTo,
            format = FULL_FORMAT,
            isUTC = false,
            isAllowZero = true
        )
        return parseStringDate(
            dateStr = str,
            isUTC = true
        )?.time ?: 0
    }

    fun getCurrentTimestampUtc(): Long = toTimestampUtc(getCurrentTimestampLocal())

    fun localMillisToUTC(localMillis: Long?): Long? {
        if (localMillis == null) return null
        try {
            val russian = Locale(RUSSIAN_LOCALE)
            val dateFormat = SimpleDateFormat(DATE_TIME_PATTERN, russian)
            val date = Date(localMillis)
            dateFormat.timeZone = TimeZone.getTimeZone(TIME_ZONE_ID_UTC)
            val strDate = dateFormat.format(date)
            val dateFormatLocal = SimpleDateFormat(DATE_TIME_PATTERN, russian)
            val utcDate = dateFormatLocal.parse(strDate)
            return utcDate?.time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return localMillis
    }

    fun getCalendar(isUtcToLocalTime: Boolean): Calendar {
        val calendar = Calendar.getInstance()
        if (isUtcToLocalTime) {
            calendar.timeZone = TimeZone.getTimeZone(TIME_ZONE_ID_UTC)
            calendar.timeInMillis = getCurrentTimestampUtc()
        }
        return calendar
    }

    fun isUtcTimePass(utcTimestampTo: Long) = getCurrentTimestampUtc() > utcTimestampTo
    fun isTimePass(timestampTo: Long) = getCurrentTimestampLocal() > timestampTo

    fun toTime(
        date: String,
        dateTimeInputFormat: String,
        timeOutputFormat: String
    ): String {
        val utcTimeMillis =
            parseStringDate(
                dateStr = date,
                format = dateTimeInputFormat,
                isUTC = true
            )?.time ?: return ""

        val localTimeMillis = toMillisecondsLocal(utcTimeMillis)

        return formatMillis(
            milliseconds = localTimeMillis,
            format = timeOutputFormat,
            isUTC = false
        )
            ?: ""
    }

    fun toStringDate(
        date: String?,
        dateTimeInputFormat: String = "yyyy-MM-dd",
        outputFormat: String = "d MMMM y",
        useToday: Boolean = false,
        useYesterday: Boolean = false
    ): String? {
        date ?: return null
        val russian = Locale(RUSSIAN_LOCALE)
        val newMonths = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        val dfs = DateFormatSymbols.getInstance(russian)
        dfs.months = newMonths
        val df = DateFormat.getDateInstance(DateFormat.LONG)
        val sdf = df as SimpleDateFormat
        sdf.dateFormatSymbols = dfs
        sdf.applyPattern(outputFormat)
        val jud = try {
            SimpleDateFormat(dateTimeInputFormat, russian).parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }

        return when {
            jud == null -> ""
            useToday && DateUtils.isToday(jud.time) -> "Сегодня"
            useYesterday && DateUtils.isToday(jud.time.plus(ONE_DAY_IN_MILLIS)) -> "Вчера"
            else -> sdf.format(jud)
        }
    }

    fun toEndOfDay(millis: Long): Long =
        Calendar.getInstance(UTC)
            .apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, LAST_HOUR)
                set(Calendar.MINUTE, LAST_MINUTE)
                set(Calendar.SECOND, LAST_SECOND)
                set(Calendar.MILLISECOND, LAST_MILLISECOND)
            }
            .timeInMillis

    fun formatMillis(
        milliseconds: Long?,
        format: String? = DATE_FORMAT,
        isUTC: Boolean = true,
        isAllowZero: Boolean = false
    ): String? {
        if (milliseconds == null || milliseconds == 0L && !isAllowZero) {
            return null
        }
        try {
            val sdf = SimpleDateFormat(format, Locale("ru"))
            if (isUTC) sdf.timeZone = UTC
            return sdf.format(milliseconds)
        } catch (tfe: TimeFormatException) {
            tfe.printStackTrace()
        }
        return null
    }

    fun formatMillisToString(
        timeInMillis: Long,
        format: String? = DATE_FORMAT,
        timeZone: TimeZone = DEFAULT_TIME_ZONE,
    ): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = timeZone

        return dateFormat.format(timeInMillis)
    }

    fun formatMillisToTime(
        milliseconds: Long,
        isUtc: Boolean = true
    ): String? {
        return formatMillis(milliseconds, TIME_FORMAT, isUtc, false)
    }

    fun formatMillis(
        milliseconds: Long?,
        isUTC: Boolean
    ): String? {
        return formatMillis(milliseconds, DATE_FORMAT, isUTC, false)
    }

    fun parseStringDate(
        dateStr: String?,
        format: String? = FULL_FORMAT,
        isUTC: Boolean = true
    ): Date? {
        if (dateStr == null) {
            return null
        }
        return ExceptionUtils.tryOrNull {
            val sourceFormat = SimpleDateFormat(format, Locale.getDefault())
            if (isUTC) sourceFormat.timeZone = UTC
            sourceFormat.parse(dateStr)
        }
    }

    fun formatStringToMillis(
        dateInStringFormat: String,
        format: String = FULL_FORMAT,
        timeZone: TimeZone = DEFAULT_TIME_ZONE,
    ): Long {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = timeZone

        val date = dateFormat.parse(dateInStringFormat)

        return date?.time ?: EMPTY_TIME
    }

    fun isToday(
        time: Long,
        isUtcToLocalTime: Boolean = true,
    ): Boolean {
        val calendar = getCalendar(isUtcToLocalTime)
        val day = calendar[Calendar.DAY_OF_YEAR]
        calendar.timeInMillis = time
        return calendar[Calendar.DAY_OF_YEAR] == day
    }

    fun isTomorrow(
        time: Long,
        isUtcToLocalTime: Boolean = true,
    ): Boolean {
        val calendar = getCalendar(isUtcToLocalTime)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val day = calendar[Calendar.DAY_OF_YEAR]
        calendar.timeInMillis = time
        return calendar[Calendar.DAY_OF_YEAR] == day
    }

    fun isYesterday(
        time: Long,
        isUtcToLocalTime: Boolean = true,
    ): Boolean {
        val calendar = getCalendar(isUtcToLocalTime)
        val yesterdayDay = calendar[Calendar.DAY_OF_YEAR] - 1
        calendar.timeInMillis = time
        return calendar[Calendar.DAY_OF_YEAR] == yesterdayDay
    }

    private fun getCalendarUtcFromMillis(milliseconds: Long): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        calendar.timeZone = UTC
        return calendar
    }

    private fun toCalendarLocal(calendarUtc: Calendar): Calendar = Calendar.getInstance().apply {
        timeInMillis = calendarUtc.timeInMillis
        set(Calendar.YEAR, calendarUtc.get(Calendar.YEAR))
        set(Calendar.MONTH, calendarUtc.get(Calendar.MONTH))
        set(Calendar.DAY_OF_YEAR, calendarUtc.get(Calendar.DAY_OF_YEAR))
        set(Calendar.HOUR_OF_DAY, calendarUtc.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calendarUtc.get(Calendar.MINUTE))
        set(Calendar.SECOND, calendarUtc.get(Calendar.SECOND))
        set(Calendar.MILLISECOND, calendarUtc.get(Calendar.MILLISECOND))
    }
}