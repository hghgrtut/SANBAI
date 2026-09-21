package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

internal class TrainingTransferAthleteMatcherTest {

    @Test
    fun match_namesAreMatchedCaseInsensitiveAndSameCalendarDay() {
        // ФИО и дата рождения гребцов совпадают, время рождения не важно
        val existing = listOf(
            AthleteEntity(
                id = 7,
                name = "Иван Иванов",
                dateOfBirth = Date(
                    localMillis(
                        year = 1995,
                        month = 2,
                        day = 10,
                        hour = 10,
                        minute = 0
                    )
                ),
                isMale = true,
                speedCoachSerial = null
            )
        )
        val backupAthletes = listOf(
            BackupAthlete(
                name = "  иван иванов ",
                dateOfBirthMillis = localMillis(
                    year = 1995,
                    month = 2,
                    day = 10,
                    hour = 23,
                    minute = 30
                ),
                isMale = true
            )
        )

        assertEquals(mapOf(0 to 7L), matchBackupAthletesToExisting(backupAthletes, existing))
    }

    @Test
    fun match_differentBirthDate_isNotMatched() {
        val existing = listOf(
            AthleteEntity(
                id = 3,
                name = "Иван Иванов",
                dateOfBirth = Date(
                    localMillis(
                        year = 1995,
                        month = 2,
                        day = 9,
                        hour = 12,
                        minute = 0
                    )
                ),
                isMale = true,
                speedCoachSerial = null
            )
        )
        val backupAthletes = listOf(
            BackupAthlete(
                name = "Иван Иванов",
                dateOfBirthMillis = localMillis(
                    year = 1995,
                    month = 2,
                    day = 10,
                    hour = 12,
                    minute = 0
                ),
                isMale = true
            )
        )

        assertTrue(matchBackupAthletesToExisting(backupAthletes, existing).isEmpty())
    }

    @Test
    fun match_differentName_isNotMatched() {
        val existing = listOf(
            AthleteEntity(
                id = 5,
                name = "Пётр Петров",
                dateOfBirth = Date(localMillis(1990, 1, 1, 0, 0)),
                isMale = true,
                speedCoachSerial = null
            )
        )
        val backupAthletes = listOf(
            BackupAthlete(
                name = "Иван Иванов",
                dateOfBirthMillis = localMillis(1990, 1, 1, 0, 0),
                isMale = true
            )
        )

        assertTrue(matchBackupAthletesToExisting(backupAthletes, existing).isEmpty())
    }

    private fun localMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}