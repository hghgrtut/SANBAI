package by.rowing.sanbaiteam.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

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
    ): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}