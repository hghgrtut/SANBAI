package by.rowing.sanbaiteam.training.data.comparison

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TrainingPieceMatcherTest {

    @Test
    fun `matches shorter training by duration keeping order`() {
        val base = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 15
            ),
            key(
                order = 2,
                minutes = 15
            ),
            key(
                order = 3,
                minutes = 7
            ),
        )
        val compared = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 15
            ),
            key(
                order = 2,
                minutes = 7
            ),
        )

        val matches = TrainingPieceMatcher.match(
            base = base,
            compared = compared
        )

        assertEquals(
            mapOf(0 to 0, 1 to 1, 3 to 2),
            matches,
        )
    }

    @Test
    fun `ignores unmatched trailing piece`() {
        val base = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 15
            ),
            key(
                order = 2,
                minutes = 15
            ),
            key(
                order = 3,
                minutes = 7
            ),
        )

        val compared = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 15
            ),
            key(
                order = 2,
                minutes = 15
            ),
            key(
                order = 3,
                minutes = 7
            ),
            key(
                order = 4,
                minutes = 7
            ),
        )

        val matches = TrainingPieceMatcher.match(
            base = base,
            compared = compared
        )

        assertEquals(
            mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3),
            matches,
        )
    }

    @Test
    fun `ignores unmatched piece in the middle`() {
        val base = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 15
            ),
            key(
                order = 2,
                minutes = 15
            ),
            key(
                order = 3,
                minutes = 7
            ),
        )
        val compared = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 7
            ),
            key(
                order = 2,
                minutes = 15
            ),
            key(
                order = 3,
                minutes = 15
            ),
            key(
                order = 4,
                minutes = 7
            ),
        )

        val matches = TrainingPieceMatcher.match(
            base = base,
            compared = compared
        )

        assertEquals(
            mapOf(0 to 0, 1 to 2, 2 to 3, 3 to 4),
            matches,
        )
    }

    @Test
    fun `returns null when durations do not overlap`() {
        val base = listOf(
            key(
                order = 0,
                minutes = 15
            ),
            key(
                order = 1,
                minutes = 7
            ),
        )
        val compared = listOf(
            key(
                order = 0,
                minutes = 10
            ),
            key(
                order = 1,
                minutes = 5
            ),
            key(
                order = 2,
                minutes = 12
            ),
        )

        assertNull(
            TrainingPieceMatcher.match(
                base = base,
                compared = compared
            )
        )
    }

    @Test
    fun `time tolerance allows exact five seconds`() {
        val base = listOf(
            key(
                order = 0,
                timeMillis = 900_000L,
                distanceMeters = 3000
            )
        )
        val compared = listOf(
            key(
                order = 0,
                timeMillis = 905_000L,
                distanceMeters = 3100
            )
        )

        assertEquals(
            mapOf(0 to 0),
            TrainingPieceMatcher.match(
                base = base,
                compared = compared
            )
        )
    }

    @Test
    fun `time difference above tolerance does not match`() {
        val base = listOf(
            key(
                order = 0,
                timeMillis = 900_000L,
                distanceMeters = 3000
            )
        )
        val compared = listOf(
            key(
                order = 0,
                timeMillis = 905_001L,
                distanceMeters = 3100
            )
        )

        assertNull(
            TrainingPieceMatcher.match(
                base = base,
                compared = compared
            )
        )
    }

    @Test
    fun `distance tolerance allows exact five meters even with different time`() {
        val base = listOf(
            key(
                order = 0,
                timeMillis = 900_000L,
                distanceMeters = 3000
            )
        )
        val compared = listOf(
            key(
                order = 0,
                timeMillis = 1_200_000L,
                distanceMeters = 3005
            )
        )

        assertEquals(
            mapOf(0 to 0), TrainingPieceMatcher.match(
                base = base,
                compared = compared
            )
        )
    }

    @Test
    fun `distance difference above tolerance does not match`() {
        val base = listOf(
            key(
                order = 0,
                timeMillis = 900_000L,
                distanceMeters = 3000
            )
        )
        val compared = listOf(
            key(
                order = 0,
                timeMillis = 1_200_000L,
                distanceMeters = 3006
            )
        )

        assertNull(
            TrainingPieceMatcher.match(
                base = base,
                compared = compared
            )
        )
    }

    private fun key(
        order: Int,
        minutes: Int,
    ) = key(
        order = order,
        timeMillis = minutes * 60_000L,
        distanceMeters = minutes * 200,
    )

    private fun key(
        order: Int,
        timeMillis: Long,
        distanceMeters: Int,
    ) = PieceDurationKey(
        order = order,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
    )
}