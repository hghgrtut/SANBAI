package by.rowing.sanbaiteam.athlete.domain.weight

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AthleteWeightFormatTest {

    @Test
    fun sanitizeWeightInput_keepsDigitsAndSingleDecimalSeparator() {
        assertEquals("75.3", sanitizeWeightInput(text = "75,3"))
        assertEquals("75.3", sanitizeWeightInput(text = "7a5.3b"))
        assertEquals("75.3", sanitizeWeightInput(text = "75.34"))
        assertEquals("984.5", sanitizeWeightInput(text = "98a4.5"))
    }

    @Test
    fun sanitizeWeightInput_limitsWholePartToThreeDigits() {
        assertEquals("300", sanitizeWeightInput(text = "3005"))
    }

    @Test
    fun parseWeightTenths_parsesWholeAndFractionalValues() {
        assertEquals(750, parseWeightTenths(text = "75"))
        assertEquals(753, parseWeightTenths(text = "75.3"))
        assertEquals(753, parseWeightTenths(text = "75,3"))
        assertEquals(750, parseWeightTenths(text = "75."))
    }

    @Test
    fun parseWeightTenths_respectsRange() {
        assertNull(parseWeightTenths(text = "19.9"))
        assertNull(parseWeightTenths(text = "300.1"))
        assertNull(parseWeightTenths(text = ""))
        assertNull(parseWeightTenths(text = "."))
    }

    @Test
    fun formatWeightTenths_usesSingleFractionDigit() {
        assertEquals("75.3", formatWeightTenths(tenths = 753))
        assertEquals("75.0", formatWeightTenths(tenths = 750))
    }
}