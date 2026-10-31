package by.rowing.sanbaiteam.training.data.filter

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SavedTrainingFilterSerializerTest {

    @Test
    fun `encode decode round trip preserves filter`() {
        val filter = TrainingFilter(
            athleteIds = setOf(1L, 2L),
            trainingTypes = setOf(TrainingPieceType.SINGLE, TrainingPieceType.DOUBLE),
            pieceTimeRange = 60_000L..120_000L,
            pieceDistanceRange = 500..2_000,
            lactateOnly = true,
            dateRange = 1_000L..2_000L,
        )

        val decoded = SavedTrainingFilterSerializer.decode(
            SavedTrainingFilterSerializer.encode(filter)
        )

        assertEquals(filter, decoded)
    }

    @Test
    fun `encode decode round trip preserves empty filter`() {
        val decoded = SavedTrainingFilterSerializer.decode(
            SavedTrainingFilterSerializer.encode(TrainingFilter())
        )

        assertEquals(TrainingFilter(), decoded)
    }

    @Test
    fun `unknown training types are ignored`() {
        val decoded = SavedTrainingFilterSerializer.decode(
            """{"trainingTypes":["UNKNOWN","SINGLE"]}"""
        )

        assertEquals(
            TrainingFilter(trainingTypes = setOf(TrainingPieceType.SINGLE)),
            decoded,
        )
    }

    @Test
    fun `incomplete range is ignored`() {
        val decoded = SavedTrainingFilterSerializer.decode("""{"pieceTimeFrom":1000}""")

        assertEquals(TrainingFilter(), decoded)
    }

    @Test
    fun `broken payload returns null`() {
        assertNull(SavedTrainingFilterSerializer.decode("not a json"))
    }
}