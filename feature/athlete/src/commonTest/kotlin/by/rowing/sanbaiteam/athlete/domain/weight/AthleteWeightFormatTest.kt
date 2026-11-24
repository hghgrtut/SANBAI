package by.rowing.sanbaiteam.athlete.domain.weight

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AthleteWeightFormatTest {

    @Test
    fun sanitizeWeightInput_keepsDigitsAndSingleDecimalSeparator() {
        assertEquals("75.3", AthleteWeightInput.sanitize(text = "75,3"))
        assertEquals("75.3", AthleteWeightInput.sanitize(text = "7a5.3b"))
        assertEquals("75.3", AthleteWeightInput.sanitize(text = "75.34"))
        assertEquals("984.5", AthleteWeightInput.sanitize(text = "98a4.5"))
    }

    @Test
    fun sanitizeWeightInput_limitsWholePartToThreeDigits() {
        assertEquals("300", AthleteWeightInput.sanitize(text = "3005"))
    }

    @Test
    fun parseWeightTenths_parsesWholeAndFractionalValues() {
        assertEquals(750, AthleteWeightInput.parse(text = "75"))
        assertEquals(753, AthleteWeightInput.parse(text = "75.3"))
        assertEquals(753, AthleteWeightInput.parse(text = "75,3"))
        assertEquals(750, AthleteWeightInput.parse(text = "75."))
    }

    @Test
    fun parseWeightTenths_respectsRange() {
        assertNull(AthleteWeightInput.parse(text = "19.9"))
        assertNull(AthleteWeightInput.parse(text = "300.1"))
        assertNull(AthleteWeightInput.parse(text = ""))
        assertNull(AthleteWeightInput.parse(text = "."))
    }

    @Test
    fun formatWeightTenths_usesSingleFractionDigit() {
        assertEquals("75.3", AthleteWeightInput.formatWeight(tenths = 753))
        assertEquals("75.0", AthleteWeightInput.formatWeight(tenths = 750))
    }
}