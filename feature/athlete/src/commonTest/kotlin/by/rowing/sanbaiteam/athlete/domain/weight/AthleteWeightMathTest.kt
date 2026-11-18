package by.rowing.sanbaiteam.athlete.domain.weight

import by.rowing.sanbaiteam.core.util.TimeUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AthleteWeightMathTest {

    @Test
    fun currentWeightTenths_returnsLastRecordNotAfterToday() {
        val records = listOf(
            record(
                day = 1,
                weightTenths = 700
            ),
            record(
                day = 5,
                weightTenths = 720
            ),
        )

        assertEquals(
            700,
            AthleteWeightMath.currentWeightTenths(
                records = records,
                todayMillis = day(day = 3)
            )
        )
        assertEquals(
            720,
            AthleteWeightMath.currentWeightTenths(
                records = records,
                todayMillis = day(day = 5)
            )
        )
        assertEquals(
            720,
            AthleteWeightMath.currentWeightTenths(
                records = records,
                todayMillis = day(day = 10)
            )
        )
    }

    @Test
    fun currentWeightTenths_returnsNullWithoutRecords() {
        assertNull(
            AthleteWeightMath.currentWeightTenths(
                records = emptyList(),
                todayMillis = day(day = 3)
            )
        )
    }

    @Test
    fun currentWeightTenths_ignoresFutureRecords() {
        assertNull(
            AthleteWeightMath.currentWeightTenths(
                records = listOf(
                    record(
                        day = 5,
                        weightTenths = 720
                    )
                ),
                todayMillis = day(day = 3)
            )
        )
    }

    @Test
    fun averageWeightTenths_singleRecordEqualsItsValue() {
        val records = listOf(
            record(
                day = 2,
                weightTenths = 703
            )
        )

        assertEquals(
            703,
            AthleteWeightMath.averageWeightTenths(
                records = records,
                fromMillis = day(day = 1),
                toMillis = day(day = 3),
            )
        )
    }

    @Test
    fun averageWeightTenths_usesFirstValueBeforeFirstRecord() {
        assertEquals(
            700,
            AthleteWeightMath.averageWeightTenths(
                records = listOf(
                    record(
                        day = 5,
                        weightTenths = 700
                    )
                ),
                fromMillis = day(day = 1),
                toMillis = day(day = 3),
            )
        )
    }

    @Test
    fun averageWeightTenths_usesLastValueAfterLastRecord() {
        assertEquals(
            700,
            AthleteWeightMath.averageWeightTenths(
                records = listOf(
                    record(
                        day = 1,
                        weightTenths = 700
                    )
                ),
                fromMillis = day(day = 6),
                toMillis = day(day = 8),
            )
        )
    }

    @Test
    fun averageWeightTenths_betweenRecordsUsesNeighborAverage() {
        val records = listOf(
            record(
                day = 1,
                weightTenths = 700
            ),
            record(
                day = 4,
                weightTenths = 760
            ),
        )

        // 700, 730, 730, 760
        assertEquals(
            730,
            AthleteWeightMath.averageWeightTenths(
                records = records,
                fromMillis = day(day = 1),
                toMillis = day(day = 4),
            )
        )
        assertEquals(
            730,
            AthleteWeightMath.averageWeightTenths(
                records = records,
                fromMillis = day(day = 2),
                toMillis = day(day = 3),
            )
        )
    }

    @Test
    fun averageWeightTenths_returnsNullForEmptyRecords() {
        assertNull(
            AthleteWeightMath.averageWeightTenths(
                records = emptyList(),
                fromMillis = day(day = 1),
                toMillis = day(day = 3),
            )
        )
    }

    @Test
    fun weightAtTenths_clampsToFirstAndLastRecord() {
        val records = listOf(
            record(
                day = 2,
                weightTenths = 700
            ),
            record(
                day = 5,
                weightTenths = 750
            ),
        )

        assertEquals(
            700,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 1)
            )
        )
        assertEquals(
            700,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 2)
            )
        )
        assertEquals(
            750,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 5)
            )
        )
        assertEquals(
            750,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 9)
            )
        )
    }

    @Test
    fun weightAtTenths_interpolatesLinearlyBetweenRecords() {
        val records = listOf(
            record(
                day = 1,
                weightTenths = 910
            ),
            record(
                day = 5,
                weightTenths = 950
            ),
        )

        // 91, 92, 93, 94, 95
        assertEquals(
            920,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 2)
            )
        )
        assertEquals(
            930,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 3)
            )
        )
        assertEquals(
            940,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 4)
            )
        )
    }

    @Test
    fun weightAtTenths_roundsInterpolatedValueToTenths() {
        val records = listOf(
            record(
                day = 1,
                weightTenths = 910
            ),
            record(
                day = 4,
                weightTenths = 950
            ),
        )

        // 910 + 40/3 = 923.33; 910 + 80/3 = 936.67
        assertEquals(
            923,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 2)
            )
        )
        assertEquals(
            937,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 3)
            )
        )
    }

    @Test
    fun weightAtTenths_interpolatesDecreasingTrend() {
        val records = listOf(
            record(
                day = 1,
                weightTenths = 950
            ),
            record(
                day = 5,
                weightTenths = 910
            ),
        )

        assertEquals(
            940,
            AthleteWeightMath.weightAtTenths(
                records = records,
                dateMillis = day(day = 2)
            )
        )
    }

    @Test
    fun chartPoints_keepsOnlyRecordsInsideRange() {
        val records = listOf(
            record(
                day = 1,
                weightTenths = 700
            ),
            record(
                day = 3,
                weightTenths = 710
            ),
            record(
                day = 6,
                weightTenths = 720
            ),
        )

        val points = AthleteWeightMath.chartPoints(
            records = records,
            fromMillis = day(day = 2),
            toMillis = day(day = 4),
        )

        assertEquals(listOf(710), points.map { it.weightTenths })
    }

    @Test
    fun dateTickOffsets_startAtZeroAndEndAtRangeEnd() {
        val from = day(day = 1)
        val to = day(day = 8)

        val offsets = AthleteWeightMath.dateTickOffsets(
            fromMillis = from,
            toMillis = to,
        )

        assertEquals(0L, offsets.first())
        assertEquals(to - from, offsets.last())
        assertEquals(offsets.sorted(), offsets)
    }

    @Test
    fun dateTickOffsets_singleDayReturnsZero() {
        assertEquals(
            listOf(0L),
            AthleteWeightMath.dateTickOffsets(
                fromMillis = day(day = 1),
                toMillis = day(day = 1),
            )
        )
    }

    private fun record(
        day: Int,
        weightTenths: Int,
    ): AthleteWeightRecord = AthleteWeightRecord(
        dateMillis = day(day = day),
        weightTenths = weightTenths,
    )

    private fun day(day: Int): Long = TimeUtils.noonMillis(
        year = 2024,
        month = 1,
        day = day,
    )!!
}