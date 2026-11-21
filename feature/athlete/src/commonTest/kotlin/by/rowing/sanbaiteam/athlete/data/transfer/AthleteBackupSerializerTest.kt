package by.rowing.sanbaiteam.athlete.data.transfer

import by.rowing.sanbaiteam.athlete.data.transfer.models.AthleteBackup
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupAthlete
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupPersonalBest
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AthleteBackupSerializerTest {

    @Test
    fun `round trip keeps athletes records and weights`() {
        val backup = AthleteBackup(
            kind = AthleteBackup.KIND,
            formatVersion = AthleteBackup.FORMAT_VERSION,
            athletes = listOf(
                BackupAthlete(
                    name = "Иванов Иван",
                    dateOfBirthMillis = 1_000_000L,
                    isMale = true,
                    speedCoachSerial = "2693559",
                    personalBests = listOf(
                        BackupPersonalBest(
                            boatType = "CONCEPT_ROW_ERG",
                            distanceMeters = 2000,
                            timeMillis = 360_000L,
                        )
                    ),
                    weights = listOf(
                        BackupWeight(
                            dateMillis = 2_000_000L,
                            weightTenths = 753,
                        )
                    ),
                )
            ),
        )

        val decoded = AthleteBackupSerializer.decode(AthleteBackupSerializer.encode(backup))

        assertEquals(backup, decoded)
    }

    @Test
    fun `unknown fields are ignored`() {
        val json = """
            {
              "kind": "sanbai-athletes",
              "formatVersion": 1,
              "unknown": true,
              "athletes": []
            }
        """.trimIndent()

        val decoded = AthleteBackupSerializer.decode(json)

        assertEquals(AthleteBackup.KIND, decoded.kind)
        assertEquals(AthleteBackup.FORMAT_VERSION, decoded.formatVersion)
        assertEquals(emptyList(), decoded.athletes)
    }

    @Test
    fun `invalid json fails`() {
        assertFailsWith<Exception> {
            AthleteBackupSerializer.decode("not a json")
        }
    }
}
