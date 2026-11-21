package by.rowing.sanbaiteam.athlete.domain.transfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class AthleteBackupMergeTest {

    private val birthday = TimeUtils.startOfDayMillis(timeInMillis = 1_700_000_000_000L)

    @Test
    fun `find match ignores case extra spaces and time of day`() {
        val athletes = listOf(
            athlete(
                name = "  Иванов   Иван  ",
                dateOfBirth = birthday
            )
        )

        val match = AthleteBackupMerge.findMatch(
            athletes = athletes,
            name = "иванов иван",
            dateOfBirthMillis = birthday + 12 * 60 * 60 * 1000L,
        )

        assertEquals(athletes.first(), match)
    }

    @Test
    fun `find match returns null for different birthday`() {
        val athletes = listOf(athlete(dateOfBirth = birthday))

        val match = AthleteBackupMerge.findMatch(
            athletes = athletes,
            name = "Иванов Иван",
            dateOfBirthMillis = birthday + TimeUtils.MILLIS_PER_DAY,
        )

        assertNull(match)
    }

    @Test
    fun `merge serial replaces value from file`() {
        val existing = athlete(speedCoachSerial = "111")

        val merged = AthleteBackupMerge.mergeSerial(
            existing = existing,
            fileSerial = " 222 ",
        )

        assertNotNull(merged)
        assertEquals("222", merged.speedCoachSerial)
        assertEquals(existing.name, merged.name)
        assertEquals(existing.dateOfBirth, merged.dateOfBirth)
        assertEquals(existing.isMale, merged.isMale)
    }

    @Test
    fun `blank serial does not clear device value`() {
        val existing = athlete(speedCoachSerial = "111")

        assertNull(
            AthleteBackupMerge.mergeSerial(
                existing = existing,
                fileSerial = null
            )
        )
        assertNull(
            AthleteBackupMerge.mergeSerial(
                existing = existing,
                fileSerial = "   "
            )
        )
    }

    @Test
    fun `existing best keeps id so conflict updates instead of duplicating`() {
        val existing = listOf(
            best(
                id = 7,
                distanceMeters = 2000,
                timeMillis = 400_000L
            )
        )
        val incoming = listOf(
            best(
                id = 0,
                distanceMeters = 2000,
                timeMillis = 380_000L
            ),
            best(
                id = 0,
                distanceMeters = 500,
                timeMillis = 90_000L
            ),
        )

        val result = AthleteBackupMerge.personalBestsToWrite(
            existing = existing,
            incoming = incoming
        )

        assertEquals(2, result.size)
        assertEquals(7, result.first { it.distanceMeters == 2000 }.id)
        assertEquals(0, result.first { it.distanceMeters == 500 }.id)
        assertEquals(380_000L, result.first { it.distanceMeters == 2000 }.timeMillis)
    }

    @Test
    fun `last weight for the same day wins`() {
        val incoming = listOf(weight(weightTenths = 700), weight(weightTenths = 720))

        val result = AthleteBackupMerge.weightsToWrite(incoming = incoming)

        assertEquals(1, result.size)
        assertEquals(720, result.single().weightTenths)
    }

    private fun athlete(
        name: String = "Иванов Иван",
        dateOfBirth: Long = birthday,
        speedCoachSerial: String? = null,
    ) = AthleteEntity(
        id = 1,
        name = name,
        dateOfBirth = dateOfBirth,
        isMale = true,
        speedCoachSerial = speedCoachSerial,
    )

    private fun best(
        id: Long,
        distanceMeters: Int,
        timeMillis: Long,
    ) = AthletePersonalBestEntity(
        id = id,
        athleteId = 1,
        boatType = TrainingPieceType.CONCEPT_ROW_ERG,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
    )

    private fun weight(weightTenths: Int) = AthleteWeightEntity(
        athleteId = 1,
        dateMillis = 100L,
        weightTenths = weightTenths,
    )
}