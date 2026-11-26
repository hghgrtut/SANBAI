package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TrainingPowerTest {

    @Test
    fun `estimated watts uses Concept2 RowErg formula`() {
        val watts = TrainingPower.estimatedWatts(
            pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
            distanceMeters = 500,
            timeMillis = 120_000L,
        )

        assertEquals(203, watts)
    }

    @Test
    fun `estimated watts uses Concept2 BikeErg formula`() {
        val watts = TrainingPower.estimatedWatts(
            pieceType = TrainingPieceType.CONCEPT_BIKE_ERG,
            distanceMeters = 3000,
            timeMillis = 360_000L,
        )

        assertEquals(203, watts)
    }

    @Test
    fun `estimated watts returns null for non Concept2 piece types`() {
        assertNull(
            TrainingPower.estimatedWatts(
                pieceType = TrainingPieceType.RP_3,
                distanceMeters = 2000,
                timeMillis = 420_000L,
            )
        )
        assertNull(
            TrainingPower.estimatedWatts(
                pieceType = TrainingPieceType.SINGLE,
                distanceMeters = 2000,
                timeMillis = 420_000L,
            )
        )
    }

    @Test
    fun `estimated watts returns null for invalid input`() {
        assertNull(
            TrainingPower.estimatedWatts(
                pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
                distanceMeters = 0,
                timeMillis = 420_000L,
            )
        )
        assertNull(
            TrainingPower.estimatedWatts(
                pieceType = TrainingPieceType.CONCEPT_ROW_ERG,
                distanceMeters = 2000,
                timeMillis = 0L,
            )
        )
    }
}