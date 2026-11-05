package by.rowing.sanbaiteam.training.presentation.add

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AddTrainingValidationTest {

    @Test
    fun `valid crew and pieces produce no errors`() {
        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 7
                )
            ),
            pieces = listOf(validPiece(localId = 1)),
        )

        assertTrue(validation.isValid)
        assertNull(validation.generalError)
        assertNull(validation.athleteError)
        assertTrue(validation.pieceErrors.isEmpty())
    }

    @Test
    fun `not selected athlete produces athlete error`() {
        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 0
                )
            ),
            pieces = listOf(validPiece(localId = 1)),
        )

        assertFalse(validation.isValid)
        assertNotNull(validation.athleteError)
        assertNull(validation.generalError)
        assertTrue(validation.pieceErrors.isEmpty())
    }

    @Test
    fun `duplicate athletes produce athlete error`() {
        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 7
                ),
                AddCrewAthlete(
                    crewId = 2,
                    rowerId = 7
                ),
            ),
            pieces = listOf(validPiece(localId = 1)),
        )

        assertFalse(validation.isValid)
        assertNotNull(validation.athleteError)
    }

    @Test
    fun `too big crew produces general error`() {
        val crew = (1..AddTrainingState.MAX_CREW_SIZE + 1).map { index ->
            AddCrewAthlete(
                crewId = index.toLong(),
                rowerId = index.toLong()
            )
        }

        val validation = validateAddTraining(
            crewAthletes = crew,
            pieces = listOf(validPiece(localId = 1)),
        )

        assertFalse(validation.isValid)
        assertNotNull(validation.generalError)
        assertTrue(validation.pieceErrors.isEmpty())
    }

    @Test
    fun `no pieces produces general error`() {
        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 7
                )
            ),
            pieces = emptyList(),
        )

        assertFalse(validation.isValid)
        assertNotNull(validation.generalError)
    }

    @Test
    fun `zero distance marks only distance field`() {
        val errors = onlyPieceErrors(
            validPiece(localId = 1).copy(
                distanceMeters = 0,
                distanceText = ""
            )
        )

        assertNotNull(errors.distance)
        assertNull(errors.time)
        assertNull(errors.strokeRate)
    }

    @Test
    fun `zero time marks only time field`() {
        val errors = onlyPieceErrors(
            validPiece(localId = 1).copy(
                timeMillis = 0,
                timeText = ""
            )
        )

        assertNotNull(errors.time)
        assertNull(errors.distance)
        assertNull(errors.strokeRate)
    }

    @Test
    fun `zero stroke rate marks only stroke rate field`() {
        val errors = onlyPieceErrors(
            validPiece(localId = 1).copy(
                strokeRate = 0.0,
                strokeRateText = ""
            )
        )

        assertNotNull(errors.strokeRate)
        assertNull(errors.distance)
        assertNull(errors.time)
    }

    @Test
    fun `invalid piece keeps every invalid field`() {
        val errors = onlyPieceErrors(
            validPiece(localId = 1).copy(
                distanceMeters = 0,
                distanceText = "",
                timeMillis = 0,
                timeText = "",
                strokeRate = 0.0,
                strokeRateText = "",
            )
        )

        assertNotNull(errors.distance)
        assertNotNull(errors.time)
        assertNotNull(errors.strokeRate)
        assertNotNull(errors.firstError)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun `piece errors keep piece order`() {
        val first = validPiece(localId = 10).copy(
            distanceMeters = 0,
            distanceText = ""
        )
        val second = validPiece(localId = 20).copy(
            strokeRate = 0.0,
            strokeRateText = ""
        )
        val valid = validPiece(localId = 30)

        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 7
                )
            ),
            pieces = listOf(first, second, valid),
        )

        assertEquals(listOf(10L, 20L), validation.pieceErrors.keys.toList())
        assertEquals(10L, validation.firstInvalidPieceId)
    }

    @Test
    fun `empty time text falls back to parsed millis`() {
        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 7
                )
            ),
            pieces = listOf(validPiece(localId = 1).copy(timeText = "")),
        )

        assertTrue(validation.isValid)
    }

    private fun onlyPieceErrors(piece: AddPieceDraft): AddPieceFieldErrors {
        val validation = validateAddTraining(
            crewAthletes = listOf(
                AddCrewAthlete(
                    crewId = 1,
                    rowerId = 7
                )
            ),
            pieces = listOf(piece),
        )
        return assertNotNull(validation.pieceErrors[piece.localId])
    }

    private fun validPiece(localId: Long): AddPieceDraft = AddPieceDraft(
        localId = localId,
        distanceMeters = 2000,
        timeMillis = 420_000L,
        strokeRate = 30.0,
        distanceText = "2000",
        timeText = "7:00.0",
        strokeRateText = "30.0",
    )
}