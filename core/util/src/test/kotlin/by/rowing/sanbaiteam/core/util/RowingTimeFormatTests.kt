package by.rowing.sanbaiteam.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
}