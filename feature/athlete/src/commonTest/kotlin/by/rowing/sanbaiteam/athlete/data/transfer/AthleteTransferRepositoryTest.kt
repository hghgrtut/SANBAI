package by.rowing.sanbaiteam.athlete.data.transfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.transfer.models.AthleteBackup
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupAthlete
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupPersonalBest
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupWeight
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AthleteTransferRepositoryTest {

    private val birthday = TimeUtils.startOfDayMillis(timeInMillis = 1_700_000_000_000L)

    @Test
    fun `import adds new athlete with records and weights`() = runTest {
        val repository = FakeAthletesRepository()
        val transferRepository = AthleteTransferRepository(athletesRepository = repository)
        val weightDate = TimeUtils.startOfDayMillis(timeInMillis = 1_600_000_000_000L)

        val result = transferRepository.importBackup(
            backup = backup(
                athletes = listOf(
                    BackupAthlete(
                        name = " Петров Пётр ",
                        dateOfBirthMillis = birthday,
                        isMale = true,
                        speedCoachSerial = "123",
                        personalBests = listOf(
                            BackupPersonalBest(
                                boatType = TrainingPieceType.CONCEPT_ROW_ERG.name,
                                distanceMeters = 2000,
                                timeMillis = 360_000L,
                            )
                        ),
                        weights = listOf(
                            BackupWeight(
                                dateMillis = weightDate,
                                weightTenths = 750,
                            )
                        ),
                    )
                )
            )
        )

        val athlete = repository.athletes.single()
        assertEquals("Петров Пётр", athlete.name)
        assertEquals("123", athlete.speedCoachSerial)
        assertEquals(1, result.athletesCreated)
        assertEquals(1, result.personalBestsImported)
        assertEquals(1, result.weightsImported)
        assertEquals(TimeUtils.noonOfDay(weightDate), repository.weights.single().dateMillis)
    }

    @Test
    fun `import matches by name and birthday and keeps profile fields`() = runTest {
        val existing = AthleteEntity(
            id = 10,
            name = "Иванов Иван",
            dateOfBirth = birthday,
            isMale = false,
            speedCoachSerial = null,
        )
        val repository = FakeAthletesRepository(initial = listOf(existing))
        val transferRepository = AthleteTransferRepository(athletesRepository = repository)

        val result = transferRepository.importBackup(
            backup = backup(
                athletes = listOf(
                    BackupAthlete(
                        name = "иванов иван",
                        dateOfBirthMillis = birthday + 12 * 60 * 60 * 1000L,
                        isMale = true,
                        speedCoachSerial = "999",
                    )
                )
            )
        )

        val updated = repository.athletes.single()
        assertEquals("Иванов Иван", updated.name)
        assertEquals(birthday, updated.dateOfBirth)
        assertEquals(false, updated.isMale)
        assertEquals("999", updated.speedCoachSerial)
        assertEquals(1, result.athletesUpdated)
        assertEquals(1, repository.athletes.size)
    }

    @Test
    fun `blank serial in file does not clear device value`() = runTest {
        val existing = AthleteEntity(
            id = 10,
            name = "Иванов Иван",
            dateOfBirth = birthday,
            isMale = true,
            speedCoachSerial = "111",
        )
        val repository = FakeAthletesRepository(initial = listOf(existing))

        AthleteTransferRepository(athletesRepository = repository).importBackup(
            backup = backup(
                athletes = listOf(
                    BackupAthlete(
                        name = "Иванов Иван",
                        dateOfBirthMillis = birthday,
                        isMale = true,
                        speedCoachSerial = "  ",
                    )
                )
            )
        )

        assertEquals("111", repository.athletes.single().speedCoachSerial)
    }

    @Test
    fun `file best wins and invalid data is skipped`() = runTest {
        val existingBest = AthletePersonalBestEntity(
            id = 5,
            athleteId = 10,
            boatType = TrainingPieceType.CONCEPT_ROW_ERG,
            distanceMeters = 2000,
            timeMillis = 400_000L,
        )
        val repository = FakeAthletesRepository(
            initial = listOf(
                AthleteEntity(
                    id = 10,
                    name = "Иванов Иван",
                    dateOfBirth = birthday,
                    isMale = true,
                    speedCoachSerial = null,
                )
            ),
            initialBests = mutableListOf(existingBest),
        )
        val transferRepository = AthleteTransferRepository(athletesRepository = repository)

        val result = transferRepository.importBackup(
            backup = backup(
                athletes = listOf(
                    BackupAthlete(
                        name = "Иванов Иван",
                        dateOfBirthMillis = birthday,
                        isMale = true,
                        personalBests = listOf(
                            BackupPersonalBest(
                                boatType = TrainingPieceType.CONCEPT_ROW_ERG.name,
                                distanceMeters = 2000,
                                timeMillis = 350_000L,
                            ),
                            BackupPersonalBest(
                                boatType = "UNKNOWN",
                                distanceMeters = 1000,
                                timeMillis = 200_000L,
                            ),
                            BackupPersonalBest(
                                boatType = TrainingPieceType.CONCEPT_ROW_ERG.name,
                                distanceMeters = 0,
                                timeMillis = 200_000L,
                            ),
                        ),
                        weights = listOf(
                            BackupWeight(
                                dateMillis = FUTURE_MILLIS,
                                weightTenths = 700
                            ),
                            BackupWeight(
                                dateMillis = birthday,
                                weightTenths = 100
                            ),
                        ),
                    )
                )
            )
        )

        val best = repository.bests.single()
        assertEquals(5, best.id)
        assertEquals(350_000L, best.timeMillis)
        assertEquals(1, result.personalBestsImported)
        assertEquals(0, result.weightsImported)
        assertEquals(emptyList(), repository.weights)
    }

    @Test
    fun `import rejects training backup`() = runTest {
        val repository = FakeAthletesRepository()
        val transferRepository = AthleteTransferRepository(athletesRepository = repository)

        assertFailsWith<IllegalArgumentException> {
            transferRepository.importBackup(
                backup = AthleteBackup(
                    kind = AthleteBackup.KIND,
                    formatVersion = 2,
                    athletes = emptyList(),
                )
            )
        }
    }

    private fun backup(athletes: List<BackupAthlete>) = AthleteBackup(
        kind = AthleteBackup.KIND,
        formatVersion = AthleteBackup.FORMAT_VERSION,
        athletes = athletes,
    )

    private companion object {
        const val FUTURE_MILLIS = 4_000_000_000_000L
    }
}