package by.rowing.sanbaiteam.training.data.serializer

import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupAthlete
import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupPiece
import by.rowing.sanbaiteam.training.data.trainingtransfer.BackupTraining
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingBackup
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingBackupSerializer
import org.junit.Assert
import org.junit.Test

internal class TrainingBackupSerializerTest {

    @Test
    fun encodeDecode_roundTrip_preservesAllFields() {
        val backup = TrainingBackup(
            formatVersion = 1,
            athletes = listOf(
                BackupAthlete(
                    name = "Иван Иванов",
                    dateOfBirthMillis = 10L,
                    isMale = true
                ),
                BackupAthlete(
                    name = "Пётр Петров",
                    dateOfBirthMillis = 20L,
                    isMale = false
                ),
            ),
            trainings = listOf(
                BackupTraining(
                    dateMillis = 100L,
                    type = "SINGLE",
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
                        )
                    )
                )
            )
        )

        val decoded = TrainingBackupSerializer.decode(TrainingBackupSerializer.encode(backup))

        Assert.assertEquals(backup, decoded)
    }
}