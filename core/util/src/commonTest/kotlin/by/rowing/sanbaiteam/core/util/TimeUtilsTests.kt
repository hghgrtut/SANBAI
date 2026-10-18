package by.rowing.sanbaiteam.core.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TimeUtilsTests {

    @Test
    fun isSameCalendarDay_ignoresTimeOfDay() {
        assertTrue(
            TimeUtils.isSameCalendarDay(
                firstMillis = localMillis(
                    day = 15,
                    hour = 0,
                    minute = 1
                ),
                secondMillis = localMillis(
                    day = 15,
                    hour = 23,
                    minute = 59
                )
            )
        )
        assertFalse(
            TimeUtils.isSameCalendarDay(
                firstMillis = localMillis(
                    day = 15,
                    hour = 23,
                    minute = 59
                ),
                secondMillis = localMillis(
                    day = 16,
                    hour = 0,
                    minute = 1
                )
            )
        )
    }

    private fun localMillis(
        day: Int,
        hour: Int,
        minute: Int,
    ): Long = LocalDateTime(
        year = 2024,
        monthNumber = 1,
        dayOfMonth = day,
        hour = hour,
        minute = minute,
        second = 0,
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
