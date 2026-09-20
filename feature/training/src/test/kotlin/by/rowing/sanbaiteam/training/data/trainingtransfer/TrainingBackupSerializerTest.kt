package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class TrainingBackupSerializerTest {

    @Test
    fun encodeDecode_roundTrip_preservesData() {
        val backup = TrainingBackup(
            athletes = listOf(
                BackupAthlete(
                    name = "Иван Иванов",
                    dateOfBirthMillis = 1000L,
                    isMale = true
                ),
                BackupAthlete(
                    name = "Пётр Петров",
                    dateOfBirthMillis = 2000L,
                    isMale = false
                ),
            ),
            trainings = listOf(
                BackupTraining(
                    dateMillis = 500L,
                    type = TrainingPieceType.SINGLE.name,
                    sourceDeviceSerial = "2693559",
                    sourceSessionName = "Утренняя",
                    pieces = listOf(
                        BackupPiece(
                            athleteIndex = 0,
                            order = 0,
                            distanceMeters = 2000,
                            timeMillis = 400000,
                            strokeRate = 22.0,
                            avgHeartRate = 155,
                            maxHeartRate = 161,
                            recoveryHeartRate = 120,
                        ),
                        BackupPiece(
                            athleteIndex = 1,
                            order = 1,
                            distanceMeters = 2000,
                            timeMillis = 410000,
                            strokeRate = 21.5,
                        ),
                    )
                )
            )
        )

        val decoded = TrainingBackupSerializer.decode(TrainingBackupSerializer.encode(backup))

        assertEquals(backup, decoded)
    }

    @Test
    fun decode_nullableFields_defaultToNull() {
        val json = """
            {
              "formatVersion": 1,
              "exportedAtMillis": 1,
              "athletes": [{"name": "A", "dateOfBirthMillis": 10, "isMale": true}],
              "trainings": [{
                "dateMillis": 5,
                "type": "SINGLE",
                "pieces": [{
                  "athleteIndex": 0, "order": 0, "distanceMeters": 100,
                  "timeMillis": 1000, "strokeRate": 20.0
                }]
              }]
            }
        """.trimIndent()

        val decoded = TrainingBackupSerializer.decode(json)

        assertNull(decoded.trainings[0].sourceDeviceSerial)
        assertNull(decoded.trainings[0].sourceSessionName)
        assertNull(decoded.trainings[0].pieces[0].avgHeartRate)
        assertNull(decoded.trainings[0].pieces[0].maxHeartRate)
        assertNull(decoded.trainings[0].pieces[0].recoveryHeartRate)
    }

    @Test
    fun decode_ignoreUnknownKeys() {
        val json = """
            {
              "formatVersion": 1,
              "exportedAtMillis": 1,
              "unknownField": true,
              "athletes": [],
              "trainings": []
            }
        """.trimIndent()

        val decoded = TrainingBackupSerializer.decode(json)

        assertEquals(0, decoded.trainings.size)
        assertEquals(TrainingBackup.FORMAT_VERSION, decoded.formatVersion)
    }
}