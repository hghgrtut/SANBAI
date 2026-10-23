package by.rowing.sanbaiteam.training.presentation.detail.compose

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ComparisonFormatTest {

    @Test
    fun `signed int keeps sign and zero`() {
        assertEquals("+50", formatSignedInt(50))
        assertEquals("−50", formatSignedInt(-50))
        assertEquals("0", formatSignedInt(0))
    }

    @Test
    fun `signed tenths is rounded to one digit`() {
        assertEquals("+0.1", formatSignedTenths(0.05))
        assertEquals("−2.8", formatSignedTenths(-2.8))
        assertEquals("0", formatSignedTenths(-0.04))
    }

    @Test
    fun `signed seconds uses tenths of second`() {
        assertEquals("−2.8", formatSignedSeconds(-2_800L))
        assertEquals("+12.3", formatSignedSeconds(12_300L))
    }
}