package by.rowing.sanbaiteam.backup

import by.rowing.sanbaiteam.backup.data.model.BackupAthlete
import by.rowing.sanbaiteam.backup.data.model.BackupPiece
import by.rowing.sanbaiteam.backup.data.model.BackupTraining
import by.rowing.sanbaiteam.backup.data.model.BackupWeight
import by.rowing.sanbaiteam.backup.data.model.SanbaiBackup
import by.rowing.sanbaiteam.backup.data.serializer.BackupSerializer
import by.rowing.sanbaiteam.backup.data.serializer.UnsupportedBackupFormatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class BackupSerializerTest {

    private val serializer = BackupSerializer()

    @Test
    fun `round trip preserves backup`() {
        val backup = SanbaiBackup(
            exportedAtMillis = 100L,
            fromMillis = 200L,
            toMillis = 300L,
            athletes = listOf(
                BackupAthlete(
                    name = "Иванов Иван",
                    dateOfBirthMillis = 1_000L,
                    isMale = true,
                    weights = listOf(
                        BackupWeight(
                            dateMillis = 2_000L,
                            weightTenths = 750
                        )
                    ),
                )
            ),
            trainings = listOf(
                BackupTraining(
                    dateMillis = 3_000L,
                    type = "SINGLE",
                    source = "SPEED_COACH",
                    sourceFileBase64 = "AQID",
                    sourceFileExtension = "fit",
                    pieces = listOf(
                        BackupPiece(
                            athleteIndex = 0,
                            order = 0,
                            distanceMeters = 2000,
                            timeMillis = 420_000L,
                            strokeRate = 32.0,
                            avgHeartRate = 160,
                            maxHeartRate = 175,
                            recoveryHeartRate = 120,
                            lactate = 4.5,
                            sourceFileIndex = 0,
                            sourceIntervalIndex = 1,
                        )
                    ),
                )
            ),
        )

        val decoded = serializer.decode(serializer.encode(backup))

        assertEquals(backup, decoded)
    }

    @Test
    fun `encode always writes format header`() {
        val backup = SanbaiBackup()

        val encoded = serializer.encode(backup)

        assertTrue(encoded.contains("\"kind\":\"sanbai-backup\""))
        assertTrue(encoded.contains("\"formatVersion\":1"))
        assertEquals(backup, serializer.decode(encoded))
    }

    @Test
    fun `decode rejects legacy athlete backup`() {
        val legacyAthleteJson = """
            {
              "kind": "sanbai-athletes",
              "formatVersion": 1,
              "athletes": []
            }
        """.trimIndent()

        val error = assertFailsWith<UnsupportedBackupFormatException> {
            serializer.decode(legacyAthleteJson)
        }
        assertTrue(error.message.orEmpty().contains("Unsupported backup format"))
    }

    @Test
    fun `decode rejects legacy training backup`() {
        val legacyTrainingJson = """
            {
              "formatVersion": 2,
              "athletes": [],
              "trainings": []
            }
        """.trimIndent()

        assertFailsWith<UnsupportedBackupFormatException> {
            serializer.decode(legacyTrainingJson)
        }
    }

    @Test
    fun `decode rejects unknown kind`() {
        val json = """
            {
              "kind": "something-else",
              "formatVersion": 1
            }
        """.trimIndent()

        assertFailsWith<UnsupportedBackupFormatException> {
            serializer.decode(json)
        }
    }

    @Test
    fun `decode rejects unsupported version`() {
        val json = """
            {
              "kind": "sanbai-backup",
              "formatVersion": 2,
              "athletes": [],
              "trainings": []
            }
        """.trimIndent()

        assertFailsWith<UnsupportedBackupFormatException> {
            serializer.decode(json)
        }
    }

    @Test
    fun `decode rejects invalid json`() {
        assertFailsWith<UnsupportedBackupFormatException> {
            serializer.decode("not json")
        }
    }
}