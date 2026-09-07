package by.rowing.sanbaiteam.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalBestPercentTest {

    @Test
    fun calculate_sameSpeed_returns100() {
        // 2000m in 7:00.0 vs piece 2000m in 7:00.0
        val pbTime = 7 * 60_000L
        val percent = PersonalBestPercent.calculate(
            pieceDistanceMeters = 2000,
            pieceTimeMillis = pbTime,
            pbDistanceMeters = 2000,
            pbTimeMillis = pbTime,
        )
        assertEquals(100.0, percent!!, 0.0)
    }

    @Test
    fun calculate_pieceShorterDistance_samePace_returns100() {
        // PB 2000m @ 7:00 → same speed for 1000m is 3:30
        val percent = PersonalBestPercent.calculate(
            pieceDistanceMeters = 1000,
            pieceTimeMillis = 3 * 60_000L + 30_000L,
            pbDistanceMeters = 2000,
            pbTimeMillis = 7 * 60_000L,
        )
        assertEquals(100.0, percent!!, 0.0)
    }

    @Test
    fun calculate_fasterThanPb_returnsAbove100() {
        // PB 2000m @ 7:00, piece 1000m @ 3:00 → speed ratio 3.5/3 ≈ 116.7%
        val percent = PersonalBestPercent.calculate(
            pieceDistanceMeters = 1000,
            pieceTimeMillis = 3 * 60_000L,
            pbDistanceMeters = 2000,
            pbTimeMillis = 7 * 60_000L,
        )
        assertEquals(116.7, percent!!, 0.0)
    }

    @Test
    fun calculate_invalidInputs_returnsNull() {
        assertNull(
            PersonalBestPercent.calculate(
                pieceDistanceMeters = 0,
                pieceTimeMillis = 1000,
                pbDistanceMeters = 2000,
                pbTimeMillis = 1000,
            )
        )
    }

    @Test
    fun average_empty_returnsNull() {
        assertNull(PersonalBestPercent.average(emptyList()))
    }

    @Test
    fun average_twoValues() {
        assertEquals(90.0, PersonalBestPercent.average(listOf(80.0, 100.0))!!, 0.0)
    }

    @Test
    fun format_null_isDash() {
        assertEquals("—", PersonalBestPercent.format(null))
    }

    @Test
    fun format_value() {
        assertEquals("87.3%", PersonalBestPercent.format(87.3))
    }

    @Test
    fun percentFromPaces_samePace_returns100() {
        val pace = 1 * 60_000L + 45_000L
        assertEquals(100.0, PersonalBestPercent.percentFromPaces(pace, pace)!!, 0.0)
    }

    @Test
    fun percentFromPaces_slowerPace_below100() {
        // record 1:45.0, pace 1:50.0 → 105/110 * 100 ≈ 95.5
        val record = 1 * 60_000L + 45_000L
        val pace = 1 * 60_000L + 50_000L
        assertEquals(95.5, PersonalBestPercent.percentFromPaces(record, pace)!!, 0.0)
    }

    @Test
    fun paceFromPercent_100_returnsSame() {
        val record = 1 * 60_000L + 45_000L
        assertEquals(record, PersonalBestPercent.paceFromPercent(record, 100.0))
    }

    @Test
    fun paceFromPercent_and_percentFromPaces_roundTrip() {
        val record = 1 * 60_000L + 42_000L
        val targetPace = PersonalBestPercent.paceFromPercent(record, 95.0)!!
        val percent = PersonalBestPercent.percentFromPaces(record, targetPace)!!
        assertEquals(95.0, percent, 0.2)
    }
}
