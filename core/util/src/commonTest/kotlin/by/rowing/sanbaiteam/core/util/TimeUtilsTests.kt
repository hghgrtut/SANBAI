package by.rowing.sanbaiteam.core.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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

    @Test
    fun startOfDayMillis_returnsStartOfLocalDay() {
        assertEquals(
            localMillis(
                day = 15,
                hour = 0,
                minute = 0
            ),
            TimeUtils.startOfDayMillis(
                year = 2024,
                month = 1,
                day = 15
            )
        )
    }

    @Test
    fun endOfDayMillis_returnsLastMillisOfLocalDay() {
        val start = TimeUtils.startOfDayMillis(
            year = 2024,
            month = 1,
            day = 15
        )
        val end = TimeUtils.endOfDayMillis(
            year = 2024,
            month = 1,
            day = 15
        )

        assertEquals(start!! + TimeUtils.MILLIS_PER_DAY - 1, end)
        assertTrue(
            TimeUtils.isSameCalendarDay(
                firstMillis = start,
                secondMillis = end!!
            )
        )
    }

    @Test
    fun startOfDayMillis_returnsNullForInvalidDate() {
        assertNull(
            TimeUtils.startOfDayMillis(
                year = 2024,
                month = 13,
                day = 1
            )
        )
    }

    @Test
    fun startOfDayMillis_normalizesTimeOfDay() {
        val expected = TimeUtils.startOfDayMillis(
            year = 2024,
            month = 1,
            day = 15
        )

        assertEquals(
            expected,
            TimeUtils.startOfDayMillis(
                timeInMillis = localMillis(
                    day = 15,
                    hour = 18,
                    minute = 42
                )
            )
        )
    }

    @Test
    fun noonOfDay_returnsLocalNoon() {
        val noon = TimeUtils.noonOfDay(
            timeInMillis = localMillis(
                day = 15,
                hour = 1,
                minute = 0
            )
        )

        assertEquals(
            localMillis(
                day = 15,
                hour = 12,
                minute = 0
            ),
            noon
        )
        assertTrue(
            TimeUtils.isSameCalendarDay(
                firstMillis = localMillis(
                    day = 15,
                    hour = 0,
                    minute = 0
                ),
                secondMillis = noon
            )
        )
    }

    @Test
    fun plusDaysMillis_movesToNextDayStart() {
        val start = TimeUtils.startOfDayMillis(
            year = 2024,
            month = 1,
            day = 15
        )!!

        assertEquals(
            TimeUtils.startOfDayMillis(
                year = 2024,
                month = 1,
                day = 16
            ),
            TimeUtils.plusDaysMillis(
                timeInMillis = start,
                days = 1
            )
        )
    }

    @Test
    fun daysBetween_countsCalendarDays() {
        assertEquals(
            2,
            TimeUtils.daysBetween(
                fromMillis = localMillis(
                    day = 15,
                    hour = 23,
                    minute = 59
                ),
                toMillis = localMillis(
                    day = 17,
                    hour = 0,
                    minute = 1
                )
            )
        )
    }

    @Test
    fun formatDateShort_usesTwoDigitYear() {
        assertEquals(
            "15.01.24",
            TimeUtils.formatDateShort(
                localMillis(
                    day = 15,
                    hour = 12,
                    minute = 0
                )
            )
        )
    }

    @Test
    fun formatDayMonth_omitsYear() {
        assertEquals(
            "15.01",
            TimeUtils.formatDayMonth(
                localMillis(
                    day = 15,
                    hour = 12,
                    minute = 0
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
        month = 1,
        day = day,
        hour = hour,
        minute = minute,
        second = 0,
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}