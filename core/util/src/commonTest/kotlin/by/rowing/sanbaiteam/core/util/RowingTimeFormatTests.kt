package by.rowing.sanbaiteam.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RowingTimeFormatTests {

    @Test
    fun sanitizeTimeInput_keepsDigitsColonAndDot() {
        assertEquals("1:45.5", RowingTimeFormat.sanitizeTimeInput("1:45.5"))
        assertEquals("12:34.5", RowingTimeFormat.sanitizeTimeInput("12:34.5"))
    }

    @Test
    fun sanitizeTimeInput_dropsNonNumericSymbols() {
        assertEquals("1:45.0", RowingTimeFormat.sanitizeTimeInput("a1b:4c5.d0e"))
    }

    @Test
    fun sanitizeTimeInput_keepsOnlyFirstColon() {
        assertEquals("1:23", RowingTimeFormat.sanitizeTimeInput("1:2:3"))
    }

    @Test
    fun sanitizeTimeInput_keepsSingleDot() {
        assertEquals("45.5", RowingTimeFormat.sanitizeTimeInput("45.5"))
        assertEquals("45.5", RowingTimeFormat.sanitizeTimeInput("45..5"))
        assertEquals("45.5", RowingTimeFormat.sanitizeTimeInput("45.55"))
        assertEquals("1:2.", RowingTimeFormat.sanitizeTimeInput("1:2."))
    }

    @Test
    fun sanitizeTimeInput_capsSegmentLengths() {
        assertEquals("123", RowingTimeFormat.sanitizeTimeInput("1234"))
        assertEquals("123:45", RowingTimeFormat.sanitizeTimeInput("123:456"))
        assertEquals("1:23.4", RowingTimeFormat.sanitizeTimeInput("1:23.45"))
    }

    @Test
    fun sanitizeTimeInput_requiresMinuteBeforeColon() {
        assertEquals("45", RowingTimeFormat.sanitizeTimeInput(":45"))
        assertEquals("1:45", RowingTimeFormat.sanitizeTimeInput("1::45"))
    }

    @Test
    fun sanitizeTimeInput_emptyStaysEmpty() {
        assertEquals("", RowingTimeFormat.sanitizeTimeInput(""))
        assertEquals("", RowingTimeFormat.sanitizeTimeInput("abc"))
    }

    @Test
    fun parseTimeInput_parsesSecondsWithoutColon() {
        assertEquals(45_500L, RowingTimeFormat.parseTimeInput("45.5"))
        assertEquals(45_000L, RowingTimeFormat.parseTimeInput("45"))
        assertEquals(30_000L, RowingTimeFormat.parseTimeInput("30.0"))
        assertEquals(45_500L, RowingTimeFormat.parseTimeInput("0:45.5"))
        assertEquals(105_000L, RowingTimeFormat.parseTimeInput("1:45.0"))
    }

    @Test
    fun parseTimeInput_rejectsInvalidValues() {
        assertNull(RowingTimeFormat.parseTimeInput("75"))
        assertNull(RowingTimeFormat.parseTimeInput("75.5"))
        assertNull(RowingTimeFormat.parseTimeInput("1:75"))
        assertNull(RowingTimeFormat.parseTimeInput(""))
    }

    @Test
    fun parseTimeInput_parsesSanitizedSecondsInput() {
        val sanitized = RowingTimeFormat.sanitizeTimeInput("45.5")
        assertEquals(45_500L, RowingTimeFormat.parseTimeInput(sanitized))
    }

    @Test
    fun paceMillis_usesRequestedPaceDistance() {
        assertEquals(
            100_000L,
            RowingTimeFormat.paceMillis(
                timeMillis = 200_000L,
                distanceMeters = 1000
            )
        )
        assertEquals(
            200_000L,
            RowingTimeFormat.paceMillis(
                timeMillis = 200_000L,
                distanceMeters = 1000,
                paceDistanceMeters = 1000
            )
        )
        assertNull(
            RowingTimeFormat.paceMillis(
                timeMillis = 200_000L,
                distanceMeters = 1000,
                paceDistanceMeters = 0
            )
        )
    }

    @Test
    fun formatPace_appendsRequestedPaceDistance() {
        assertEquals(
            "1:40.0 /500м",
            RowingTimeFormat.formatPace(
                timeMillis = 200_000L,
                distanceMeters = 1000
            )
        )
        assertEquals(
            "1:38.0",
            RowingTimeFormat.formatDuration(timeMillis = 97_997L) // 97.997 seconds
        )
        assertEquals(
            "1:38.0 /500м",
            RowingTimeFormat.formatPace(
                timeMillis = 900_000L,
                distanceMeters = 4592
            )
        )
        assertEquals(
            "3:20.0 /1000м",
            RowingTimeFormat.formatPace(
                timeMillis = 200_000L,
                distanceMeters = 1000,
                paceDistanceMeters = 1000
            )
        )
    }
}